package jfbdev.jupgradableeconomy.commands;

import jfbdev.jupgradableeconomy.JUpgradableEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class UpgradeCommand implements CommandExecutor {

    private final JUpgradableEconomy plugin;

    public UpgradeCommand(JUpgradableEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMsg("player-only-command"));
            return true;
        }

        if (!plugin.canUpgradePlayer(player)) {
            player.sendMessage(plugin.getMsg("upgrade-no-more"));
            plugin.playSound(player, "max-level-reached");
            return true;
        }

        double cost = plugin.getUpgradeCostForPlayer(player);
        if (plugin.getPlayerBalance(player) < cost) {
            player.sendMessage(plugin.getMsg("upgrade-not-enough-money",
                    Map.of("cost", plugin.formatBalance(cost))));
            plugin.playSound(player, "not-enough-money");
            return true;
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

        return true;
    }
}
