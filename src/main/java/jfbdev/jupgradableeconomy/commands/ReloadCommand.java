package jfbdev.jupgradableeconomy.commands;

import jfbdev.jupgradableeconomy.JUpgradableEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final JUpgradableEconomy plugin;

    public ReloadCommand(JUpgradableEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("jupgradableeconomy.admin")) {
            sender.sendMessage(plugin.getMessage("reload-permission"));
            plugin.playSound((Player) sender, "message");
            return true;
        }

        plugin.reloadConfig();
        plugin.saveAllData();
        sender.sendMessage(plugin.getMessage("reload-success"));
        plugin.playSound((Player) sender, "message");
        return true;
    }
}
