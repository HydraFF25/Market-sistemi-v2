package com.bworld.market.gui.holder;

import com.bworld.market.model.ShopCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CategoryHolder implements InventoryHolder {

    private Inventory inventory;
    private final ShopCategory category;
    private final int page;

    public CategoryHolder(ShopCategory category, int page) {
        this.category = category;
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public int getPage() {
        return page;
    }
}
