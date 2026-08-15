package com.bworld.market.model;

import org.bukkit.Material;

import java.util.List;

public class ShopItem {

    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private double buyPrice;
    private double sellPrice;
    private final int customModelData;

    public ShopItem(String id, Material material, String displayName, List<String> lore,
                     double buyPrice, double sellPrice, int customModelData) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.customModelData = customModelData;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public boolean isBuyEnabled() {
        return buyPrice >= 0;
    }

    public boolean isSellEnabled() {
        return sellPrice >= 0;
    }

    public int getCustomModelData() {
        return customModelData;
    }
}
