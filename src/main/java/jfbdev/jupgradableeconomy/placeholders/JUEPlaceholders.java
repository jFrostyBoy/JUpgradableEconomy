package jfbdev.jupgradableeconomy.placeholders;

import jfbdev.jupgradableeconomy.JUpgradableEconomy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class JUEPlaceholders extends PlaceholderExpansion {

    private final JUpgradableEconomy plugin;

    public JUEPlaceholders(JUpgradableEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "jue";
    }

    @Override
    public @NotNull String getAuthor() {
        return "jFrostyBoy";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        int level = plugin.getPlayerLevel(player);
        double balance = plugin.getPlayerBalance(player);
        double currentLimit = plugin.getMaxBalance(player);

        return switch (params.toLowerCase()) {
            case "balance" -> plugin.formatBalance(balance);
            case "current_limit" -> plugin.formatBalance(currentLimit);
            case "level" -> String.valueOf(level);

            case "next_cost" -> {
                int next = level + 1;
                if (plugin.getConfig().contains("upgrades." + next)) {
                    double cost = plugin.getConfig().getDouble("upgrades." + next + ".cost");
                    yield plugin.formatBalance(cost);
                }
                yield "0";
            }

            case "next_limit" -> {
                int next = level + 1;
                if (plugin.getConfig().contains("upgrades." + next)) {
                    double nextLimit = plugin.getConfig().getDouble("upgrades." + next + ".max-balance");
                    yield plugin.formatBalance(nextLimit);
                }
                yield plugin.formatBalance(currentLimit);
            }

            default -> null;
        };
    }
}