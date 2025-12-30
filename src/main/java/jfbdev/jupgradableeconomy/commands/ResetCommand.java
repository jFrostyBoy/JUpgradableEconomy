package jfbdev.jupgradableeconomy.commands;

import jfbdev.jupgradableeconomy.JUpgradableEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ResetCommand implements CommandExecutor {

    private final JUpgradableEconomy plugin;

    public ResetCommand(JUpgradableEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMsg("reset-no-target"));
            plugin.playSound((Player) sender, "message");
            return true;
        }

        String target = args[0];

        if (target.equalsIgnoreCase("*")) {
            int count = 0;
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (plugin.getPlayerLevel(offlinePlayer) > 0) {
                    plugin.resetPlayerLevel(offlinePlayer);
                    count++;
                }
            }
            if (count > 0) {
                plugin.saveAllData();
            }
            sender.sendMessage(plugin.getMsg("reset-all-success"));
            sender.sendMessage("§7Затронуто игроков: §e" + count);
            plugin.playSound((Player) sender, "reset-success");
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(target);

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            sender.sendMessage(plugin.getMsg("reset-player-not-found",
                    Map.of("player", target)));
            plugin.playSound((Player) sender, "message");
            return true;
        }

        int oldLevel = plugin.getPlayerLevel(player);
        if (oldLevel == 0) {
            sender.sendMessage("§e" + plugin.getPlayerName(player) + " §7уже на базовом уровне (0).");
            plugin.playSound((Player) sender, "message");
            return true;
        }

        plugin.resetPlayerLevel(player);
        plugin.saveAllData();

        sender.sendMessage(plugin.getMsg("reset-success",
                Map.of("player", plugin.getPlayerName(player))));
        plugin.playSound((Player) sender, "reset-success");

        return true;
    }
}
