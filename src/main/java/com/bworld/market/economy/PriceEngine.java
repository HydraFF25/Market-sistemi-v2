package com.bworld.market.economy;

import com.bworld.market.BworldMarket;
import com.bworld.market.model.ShopItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Arz/talep bazli dinamik fiyatlandirma motoru.
 * Bir urun cok satin alinirsa fiyati yukselir, cok satilirsa fiyati duser.
 * Zamanla (decay-interval-minutes) taban fiyata geri doner.
 */
public class PriceEngine {

    private final BworldMarket plugin;
    private final Map<String, Double> multipliers = new HashMap<>();
    private File file;
    private FileConfiguration data;

    public PriceEngine(BworldMarket plugin) {
        this.plugin = plugin;
        load();
        startDecayTask();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "prices.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "prices.yml olusturulamadi", e);
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            multipliers.put(key, data.getDouble(key, 1.0));
        }
    }

    public void save() {
        for (Map.Entry<String, Double> entry : multipliers.entrySet()) {
            data.set(entry.getKey(), entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "prices.yml kaydedilemedi", e);
        }
    }

    private double getMultiplier(ShopItem item) {
        return multipliers.getOrDefault(item.getId(), 1.0);
    }

    public double getBuyPrice(ShopItem item) {
        if (!plugin.getConfigManager().isDynamicPricingEnabled()) {
            return round(item.getBuyPrice());
        }
        return round(item.getBuyPrice() * getMultiplier(item));
    }

    public double getSellPrice(ShopItem item) {
        double base;
        if (!plugin.getConfigManager().isDynamicPricingEnabled()) {
            base = item.getSellPrice();
        } else {
            base = item.getSellPrice() * getMultiplier(item);
        }
        double tax = plugin.getConfigManager().getSellTaxPercent() / 100.0;
        return round(base * (1 - tax));
    }

    public void onBuy(ShopItem item, int amount) {
        if (!plugin.getConfigManager().isDynamicPricingEnabled()) return;
        double step = plugin.getConfigManager().getDynamicStep();
        double max = plugin.getConfigManager().getDynamicMax();
        double mult = getMultiplier(item);
        mult = Math.min(max, mult + step * amount);
        multipliers.put(item.getId(), mult);
    }

    public void onSell(ShopItem item, int amount) {
        if (!plugin.getConfigManager().isDynamicPricingEnabled()) return;
        double step = plugin.getConfigManager().getDynamicStep();
        double min = plugin.getConfigManager().getDynamicMin();
        double mult = getMultiplier(item);
        mult = Math.max(min, mult - step * amount);
        multipliers.put(item.getId(), mult);
    }

    private void startDecayTask() {
        int minutes = Math.max(1, plugin.getConfigManager().getDynamicDecayMinutes());
        long ticks = minutes * 60L * 20L;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfigManager().isDynamicPricingEnabled()) return;
            for (String key : new HashMap<>(multipliers).keySet()) {
                double mult = multipliers.get(key);
                if (mult > 1.0) {
                    mult = Math.max(1.0, mult - 0.02);
                } else if (mult < 1.0) {
                    mult = Math.min(1.0, mult + 0.02);
                }
                multipliers.put(key, mult);
            }
            save();
        }, ticks, ticks);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
