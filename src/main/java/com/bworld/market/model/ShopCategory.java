package com.bworld.market.model;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int order;
    private final List<ShopItem> items = new ArrayList<>();

    public ShopCategory(String id, String displayName, Material icon, int order) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getOrder() {
        return order;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public ShopItem getItem(Material material) {
        for (ShopItem item : items) {
            if (item.getMaterial() == material) {
                return item;
            }
        }
        return null;
    }

    public void removeItem(Material material) {
        items.removeIf(item -> item.getMaterial() == material);
    }
}
