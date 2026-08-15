package com.bworld.market.config;

import com.bworld.market.BworldMarket;
import com.bworld.market.model.ShopCategory;
import com.bworld.market.model.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private final BworldMarket plugin;
    private FileConfiguration config;
    private FileConfiguration itemsConfig;
    private FileConfiguration messagesConfig;
    private File itemsFile;

    private final List<ShopCategory> categories = new ArrayList<>();

    public ConfigManager(BworldMarket plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        saveResourceIfMissing("items.yml");
        saveResourceIfMissing("messages.yml");

        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        loadCategories();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        categories.clear();
        loadCategories();
    }

    private void saveResourceIfMissing(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    private void loadCategories() {
        ConfigurationSection catsSection = itemsConfig.getConfigurationSection("categories");
        if (catsSection == null) {
            plugin.getLogger().warning("items.yml icinde 'categories' bolumu bulunamadi!");
            return;
        }
        for (String catKey : catsSection.getKeys(false)) {
            ConfigurationSection catSec = catsSection.getConfigurationSection(catKey);
            if (catSec == null) continue;

            String displayName = catSec.getString("display-name", catKey);
            Material icon = parseMaterial(catSec.getString("icon", "CHEST"), Material.CHEST);
            int order = catSec.getInt("order", 0);

            ShopCategory category = new ShopCategory(catKey, displayName, icon, order);

            ConfigurationSection itemsSec = catSec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String itemKey : itemsSec.getKeys(false)) {
                    ConfigurationSection itemSec = itemsSec.getConfigurationSection(itemKey);
                    if (itemSec == null) continue;

                    Material material = parseMaterial(itemSec.getString("material", itemKey), null);
                    if (material == null) {
                        plugin.getLogger().warning("Gecersiz materyal: " + itemKey + " (" + catKey + ")");
                        continue;
                    }

                    String name = itemSec.getString("name", null);
                    List<String> lore = itemSec.getStringList("lore");
                    double buyPrice = itemSec.getDouble("buy-price", -1);
                    double sellPrice = itemSec.getDouble("sell-price", -1);
                    int cmd = itemSec.getInt("custom-model-data", 0);

                    ShopItem shopItem = new ShopItem(itemKey, material, name, lore, buyPrice, sellPrice, cmd);
                    category.addItem(shopItem);
                }
            }

            categories.add(category);
        }
        categories.sort(Comparator.comparingInt(ShopCategory::getOrder));
    }

    private Material parseMaterial(String s, Material def) {
        if (s == null) return def;
        try {
            return Material.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public void addItem(String categoryId, Material material, double buyPrice, double sellPrice) {
        ConfigurationSection catsSection = itemsConfig.getConfigurationSection("categories");
        if (catsSection == null) {
            catsSection = itemsConfig.createSection("categories");
        }
        ConfigurationSection catSec = catsSection.getConfigurationSection(categoryId);
        if (catSec == null) {
            catSec = catsSection.createSection(categoryId);
            catSec.set("display-name", categoryId);
            catSec.set("icon", material.name());
            catSec.set("order", catsSection.getKeys(false).size());
        }
        ConfigurationSection itemsSec = catSec.getConfigurationSection("items");
        if (itemsSec == null) {
            itemsSec = catSec.createSection("items");
        }

        String itemKey = material.name().toLowerCase();
        ConfigurationSection itemSec = itemsSec.createSection(itemKey);
        itemSec.set("material", material.name());
        itemSec.set("buy-price", buyPrice);
        itemSec.set("sell-price", sellPrice);

        saveItemsConfig();
        reload();
    }

    public boolean removeItem(String categoryId, Material material) {
        ConfigurationSection catsSection = itemsConfig.getConfigurationSection("categories");
        if (catsSection == null) return false;
        ConfigurationSection catSec = catsSection.getConfigurationSection(categoryId);
        if (catSec == null) return false;
        ConfigurationSection itemsSec = catSec.getConfigurationSection("items");
        if (itemsSec == null) return false;

        String itemKey = material.name().toLowerCase();
        if (!itemsSec.contains(itemKey)) return false;
        itemsSec.set(itemKey, null);

        saveItemsConfig();
        reload();
        return true;
    }

    public boolean setPrice(String categoryId, Material material, boolean buy, double price) {
        ConfigurationSection catsSection = itemsConfig.getConfigurationSection("categories");
        if (catsSection == null) return false;
        ConfigurationSection catSec = catsSection.getConfigurationSection(categoryId);
        if (catSec == null) return false;
        ConfigurationSection itemsSec = catSec.getConfigurationSection("items");
        if (itemsSec == null) return false;

        String itemKey = material.name().toLowerCase();
        ConfigurationSection itemSec = itemsSec.getConfigurationSection(itemKey);
        if (itemSec == null) return false;

        itemSec.set(buy ? "buy-price" : "sell-price", price);
        saveItemsConfig();
        reload();
        return true;
    }

    private void saveItemsConfig() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "items.yml kaydedilemedi!", e);
        }
    }

    public List<ShopCategory> getCategories() {
        return categories;
    }

    public ShopCategory getCategory(String id) {
        for (ShopCategory c : categories) {
            if (c.getId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    public FileConfiguration getMessages() {
        return messagesConfig;
    }

    public String getGuiTitle() {
        return config.getString("gui.main-title", "&8&lBworld Market");
    }

    public int getGuiRows() {
        return config.getInt("gui.category-rows", 6);
    }

    public boolean isDynamicPricingEnabled() {
        return config.getBoolean("dynamic-pricing.enabled", true);
    }

    public double getDynamicStep() {
        return config.getDouble("dynamic-pricing.step-percent", 1.0) / 100.0;
    }

    public double getDynamicMin() {
        return config.getDouble("dynamic-pricing.min-multiplier", 0.5);
    }

    public double getDynamicMax() {
        return config.getDouble("dynamic-pricing.max-multiplier", 2.0);
    }

    public int getDynamicDecayMinutes() {
        return config.getInt("dynamic-pricing.decay-interval-minutes", 5);
    }

    public double getSellTaxPercent() {
        return config.getDouble("sell-tax-percent", 0.0);
    }

    public boolean isLogTransactions() {
        return config.getBoolean("log-transactions", true);
    }

    public boolean isConfirmSellAll() {
        return config.getBoolean("confirm-sell-all", true);
    }

    public boolean isSoundsEnabled() {
        return config.getBoolean("sounds.enabled", true);
    }
}
