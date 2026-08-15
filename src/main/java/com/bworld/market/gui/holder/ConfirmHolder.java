package com.bworld.market.gui.holder;

import com.bworld.market.model.ShopItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public class ConfirmHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<ShopItem, Integer> sellMap;
    private final double total;

    public ConfirmHolder(Map<ShopItem, Integer> sellMap, double total) {
        this.sellMap = sellMap;
        this.total = total;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Map<ShopItem, Integer> getSellMap() {
        return sellMap;
    }

    public double getTotal() {
        return total;
    }
}
