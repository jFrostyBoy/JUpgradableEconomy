package jfbdev.jupgradableeconomy.commands;

import jfbdev.jupgradableeconomy.JUpgradableEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UpgradeCommand implements CommandExecutor, TabCompleter {

    private final JUpgradableEconomy plugin;
    private final Map<UUID, Long> giftCooldowns = new ConcurrentHashMap<>();

    public UpgradeCommand(JUpgradableEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMsg("player-only-command"));
            return true;
        }

        if (args.length == 0) {
            performSelfUpgrade(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "info" -> handleInfo(player, args);
            case "gift" -> handleGift(player, args);
            default -> {
                player.sendMessage(plugin.getMsg("unknown-subcommand"));
                player.sendMessage(plugin.getMsg("usage-main"));
                yield true;
            }
        };
    }

    private void performSelfUpgrade(Player player) {
        if (plugin.canUpgradePlayer(player)) {
            player.sendMessage(plugin.getMsg("upgrade-no-more"));
            plugin.playSound(player, "max-level-reached");
            return;
        }

        double cost = plugin.getUpgradeCostForPlayer(player);
        if (plugin.getPlayerBalance(player) < cost) {
            player.sendMessage(plugin.getMsg("upgrade-not-enough-money",
                    Map.of("cost", plugin.formatBalance(cost))));
            plugin.playSound(player, "not-enough-money");
            return;
        }

        int nextLevel = plugin.getPlayerLevel(player) + 1;
        double newMax = plugin.getConfig().getDouble("upgrades." + nextLevel + ".max-balance");

        plugin.withdrawPlayer(player, cost);
        plugin.performUpgrade(player);

        player.sendMessage(plugin.getMsg("upgrade-success"));
        player.sendMessage(plugin.getMsg("upgrade-new-limit",
                Map.of("new_max", plugin.formatBalance(newMax))));
        player.sendMessage(plugin.getMsg("upgrade-current-balance",
                Map.of("balance", plugin.formatBalance(plugin.getPlayerBalance(player)))));
        plugin.playSound(player, "upgrade-success");
    }

    private boolean handleInfo(Player sender, String[] args) {
        boolean success = false;

        if (args.length < 2) {
            sender.sendMessage(plugin.getMsg("usage-info"));
        } else {
            String targetName = args[1];
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(plugin.getMsg("info-player-never-joined",
                        Map.of("player", targetName)));
            } else {
                int level = plugin.getPlayerLevel(target);
                double balance = plugin.getPlayerBalance(target);
                double currentLimit = plugin.getMaxBalance(target);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", target.getName() != null ? target.getName() : targetName);
                placeholders.put("balance", plugin.formatBalance(balance));
                placeholders.put("current_limit", plugin.formatBalance(currentLimit));
                placeholders.put("level", String.valueOf(level));

                List<String> lines;
                if (plugin.getConfig().contains("upgrades." + (level + 1))) {
                    int nextLevel = level + 1;
                    double cost = plugin.getConfig().getDouble("upgrades." + nextLevel + ".cost");
                    double nextLimit = plugin.getConfig().getDouble("upgrades." + nextLevel + ".max-balance");

                    placeholders.put("next_level", String.valueOf(nextLevel));
                    placeholders.put("next_limit", plugin.formatBalance(nextLimit));
                    placeholders.put("cost", plugin.formatBalance(cost));

                    lines = plugin.getConfig().getStringList("messages.info-limits");
                } else {
                    lines = plugin.getConfig().getStringList("messages.info-limits-max");
                }

                for (String line : lines) {
                    String formatted = line;
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                    sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', formatted));
                }

                plugin.playSound(sender, "message");
                success = true;
            }
        }

        return success;
    }

    private boolean handleGift(Player sender, String[] args) {
        boolean success = false;

        if (args.length < 2) {
            sender.sendMessage(plugin.getMsg("usage-gift"));
        } else {
            long cooldownSeconds = plugin.getConfig().getLong("gift-cooldown", 300);
            long lastUse = giftCooldowns.getOrDefault(sender.getUniqueId(), 0L);
            long timeLeft = (lastUse + cooldownSeconds * 1000) - System.currentTimeMillis();

            if (timeLeft > 0) {
                sender.sendMessage(plugin.getMsg("gift-on-cooldown",
                        Map.of("time", String.valueOf(timeLeft / 1000 + 1))));
            } else {
                String targetName = args[1];
                if (targetName.equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage(plugin.getMsg("gift-self-forbidden"));
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        sender.sendMessage(plugin.getMsg("gift-invalid-player",
                                Map.of("player", targetName)));
                    } else if (plugin.canUpgradePlayer(target)) {
                        sender.sendMessage(plugin.getMsg("gift-max-level",
                                Map.of("player", target.getName() != null ? target.getName() : targetName)));
                    } else {
                        double cost = plugin.getConfig().getDouble("upgrades." + (plugin.getPlayerLevel(target) + 1) + ".cost");

                        if (plugin.getPlayerBalance(sender) < cost) {
                            sender.sendMessage(plugin.getMsg("upgrade-not-enough-money",
                                    Map.of("cost", plugin.formatBalance(cost))));
                            plugin.playSound(sender, "not-enough-money");
                        } else {
                            plugin.withdrawPlayer(sender, cost);
                            plugin.performUpgrade(target);
                            plugin.saveAllData();

                            Map<String, String> ph = new HashMap<>();
                            ph.put("player", target.getName() != null ? target.getName() : targetName);
                            ph.put("sender", sender.getName());

                            sender.sendMessage(plugin.getMsg("gift-success-sender", ph));

                            if (target.isOnline() && target.getPlayer() != null) {
                                target.getPlayer().sendMessage(plugin.getMsg("gift-success-receiver", ph));
                                plugin.playSound(target.getPlayer(), "upgrade-success");
                            }

                            plugin.playSound(sender, "upgrade-success");
                            giftCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
                            success = true;
                        }
                    }
                }
            }
        }

        return success;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("info", "gift"), new ArrayList<>());
        }

        if (args.length == 2 && ("info".equalsIgnoreCase(args[0]) || "gift".equalsIgnoreCase(args[0]))) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    playerNames.add(p.getName());
                }
            }
            return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
        }

        return new ArrayList<>();
    }
}
