package jfbdev.jupgradableeconomy;

import jfbdev.jupgradableeconomy.commands.LimitsCommand;
import jfbdev.jupgradableeconomy.commands.ReloadCommand;
import jfbdev.jupgradableeconomy.commands.ResetCommand;
import jfbdev.jupgradableeconomy.commands.UpgradeCommand;
import jfbdev.jupgradableeconomy.placeholders.JUEPlaceholders;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JUpgradableEconomy extends JavaPlugin implements Economy {

    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    private static class PlayerData {
        double balance;
        int level;

        PlayerData(double balance, int level) {
            this.balance = balance;
            this.level = level;
        }
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        dataFile = new File(getDataFolder(), "players.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe(getMessage("vault-not-found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getServicesManager().register(Economy.class, this, this, ServicePriority.High);

        Objects.requireNonNull(getCommand("ecoupgrade")).setExecutor(new UpgradeCommand(this));
        Objects.requireNonNull(getCommand("ecoreload")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("ecoreset")).setExecutor(new ResetCommand(this));
        Objects.requireNonNull(getCommand("ecolimits")).setExecutor(new LimitsCommand(this));

        if (getConfig().getBoolean("auto-import", true)) {
            importExistingBalances();
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new JUEPlaceholders(this).register();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveAllData, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        saveAllData();
    }

    public String getMessage(String path) {
        String msg = getConfig().getString("messages." + path);
        if (msg == null || msg.isEmpty()) {
            return "§c[Сообщение не найдено: " + path + "]";
        }
        return ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.prefix", "") + msg);
    }

    private String getMessage(String path, Map<String, String> placeholders) {
        String msg = getMessage(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return msg;
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = dataConfig.getDouble(key + ".balance", getConfig().getDouble("starting-balance", 100.0));
                int level = dataConfig.getInt(key + ".level", 0);
                playerData.put(uuid, new PlayerData(balance, level));
            } catch (Exception ignored) {}
        }
    }

    public void saveAllData() {
        try {
            YamlConfiguration newConfig = new YamlConfiguration();
            for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
                String path = entry.getKey().toString();
                PlayerData pd = entry.getValue();
                newConfig.set(path + ".balance", pd.balance);
                newConfig.set(path + ".level", pd.level);
            }
            newConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().warning("Ошибка сохранения players.yml: " + e.getMessage());
        }
    }

    private PlayerData getOrCreateData(OfflinePlayer player) {
        return playerData.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerData(getConfig().getDouble("starting-balance", 0.0), 0));
    }

    public double getMaxBalance(OfflinePlayer player) {
        int level = getOrCreateData(player).level;
        return getConfig().getDouble(level == 0 ? "default-max-balance" : "upgrades." + level + ".max-balance", 1000.0);
    }

    private boolean canUpgrade(OfflinePlayer player) {
        int next = getOrCreateData(player).level + 1;
        return getConfig().contains("upgrades." + next);
    }

    private double getUpgradeCost(OfflinePlayer player) {
        int next = getOrCreateData(player).level + 1;
        return getConfig().getDouble("upgrades." + next + ".cost", -1);
    }

    private void upgradePlayer(OfflinePlayer player) {
        getOrCreateData(player).level++;
    }

    private void importExistingBalances() {
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == this) return;

        Economy old = rsp.getProvider();
        int count = 0;
        for (OfflinePlayer p : getServer().getOfflinePlayers()) {
            double bal = old.getBalance(p);
            if (bal > 0) {
                PlayerData pd = getOrCreateData(p);
                pd.balance = Math.min(bal, getMaxBalance(p));
                count++;
            }
        }
        getLogger().info(getMessage("import-complete", Map.of("players", String.valueOf(count))));
        getConfig().set("auto-import", false);
        saveConfig();
    }

    private String formatAmount(double amount) {
        String mode = getConfig().getString("currency-format.number-format", "formatted").toLowerCase();

        if ("commas".equals(mode)) {
            DecimalFormat df = new DecimalFormat("#,###.##");
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
            return df.format(amount);
        } else if ("formatted".equals(mode)) {
            return shortenNumber(amount);
        } else {
            return String.format("%.2f", amount).replaceAll("\\.00$", "");
        }
    }

    private String shortenNumber(double value) {
        if (value < 1_000) return String.format("%.2f", value).replaceAll("\\.00$", "");
        if (value < 1_000_000) return String.format("%.2f", value / 1_000).replaceAll("\\.00$", "") + "k";
        if (value < 1_000_000_000) return String.format("%.2f", value / 1_000_000).replaceAll("\\.00$", "") + "M";
        return String.format("%.2f", value / 1_000_000_000).replaceAll("\\.00$", "") + "B";
    }

    @Override
    public String format(double amount) {
        String amountStr = formatAmount(Math.abs(amount));
        String symbol = getConfig().getString("currency-symbol", "$");
        String template = getConfig().getString("currency-format.display-format", "{currency}{amount}");
        return template.replace("{amount}", amountStr).replace("{currency}", symbol);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        PlayerData pd = getOrCreateData(player);
        double max = getMaxBalance(player);
        double newBal = pd.balance + amount;

        if (newBal > max) {
            double added = max - pd.balance;
            pd.balance = max;
            return new EconomyResponse(added, max, EconomyResponse.ResponseType.SUCCESS,
                    getMessage("deposit-limit-reached"));
        }

        pd.balance = newBal;
        return new EconomyResponse(amount, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        PlayerData pd = getOrCreateData(player);
        if (pd.balance < amount) {
            return new EconomyResponse(0, pd.balance, EconomyResponse.ResponseType.FAILURE, "Недостаточно средств");
        }
        pd.balance -= amount;
        return new EconomyResponse(amount, pd.balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getOrCreateData(player).balance;
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName); // миры не разделяем
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return 0;
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        getOrCreateData(player);
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String currencyNamePlural() { return ""; }
    @Override public String currencyNameSingular() { return ""; }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Не поддерживается");
    }

    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse createBank(String name, String playerName) { return notImplemented(); }
    @Override public EconomyResponse deleteBank(String name) { return notImplemented(); }
    @Override public EconomyResponse bankBalance(String name) { return notImplemented(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return notImplemented(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return notImplemented(); }
    @Override public List<String> getBanks() { return Collections.emptyList(); }

    @Override public boolean hasAccount(OfflinePlayer player) { return true; }
    @Override public boolean hasAccount(String playerName) { return true; }
    @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return true; }
    @Override public boolean hasAccount(String playerName, String worldName) { return true; }

    public boolean canUpgradePlayer(OfflinePlayer player) {
        return canUpgrade(player);
    }

    public double getUpgradeCostForPlayer(OfflinePlayer player) {
        return getUpgradeCost(player);
    }

    public void performUpgrade(OfflinePlayer player) {
        upgradePlayer(player);
    }

    public String formatBalance(double amount) {
        return format(amount);
    }

    public double getPlayerBalance(OfflinePlayer player) {
        return getBalance(player);
    }

    public int getPlayerLevel(OfflinePlayer player) {
        return getOrCreateData(player).level;
    }

    public String getMsg(String path) {
        return getMessage(path);
    }

    public String getMsg(String path, Map<String, String> placeholders) {
        return getMessage(path, placeholders);
    }

    public void resetPlayerLevel(OfflinePlayer player) {
        getOrCreateData(player).level = 0;
    }

    public String getPlayerName(OfflinePlayer player) {
        String name = player.getName();
        return name != null ? name : "Неизвестный";
    }

    public void playSound(Player player, String soundKey) {
        if (!getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        String soundPath = "sounds." + soundKey;
        String soundName = getConfig().getString(soundPath + ".sound");
        if (soundName == null || soundName.isEmpty()) {
            return;
        }

        float volume = (float) getConfig().getDouble(soundPath + ".volume", 1.0);
        float pitch = (float) getConfig().getDouble(soundPath + ".pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Неверное имя звука в конфиге: " + soundName);
        }
    }
}
