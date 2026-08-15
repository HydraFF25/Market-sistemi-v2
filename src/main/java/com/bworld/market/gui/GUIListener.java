package com.bworld.market.gui;

import com.bworld.market.BworldMarket;
import com.bworld.market.gui.holder.CategoryHolder;
import com.bworld.market.gui.holder.ConfirmHolder;
import com.bworld.market.gui.holder.MainMenuHolder;
import com.bworld.market.model.ShopCategory;
import com.bworld.market.model.ShopItem;
import com.bworld.market.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {

    private final BworldMarket plugin;

    public GUIListener(BworldMarket plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        boolean ours = holder instanceof MainMenuHolder || holder instanceof CategoryHolder || holder instanceof ConfirmHolder;
        if (!ours) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (holder instanceof MainMenuHolder) {
            handleMainMenuClick(player, event.getSlot());
        } else if (holder instanceof CategoryHolder) {
            handleCategoryClick(player, (CategoryHolder) holder, event);
        } else if (holder instanceof ConfirmHolder) {
            handleConfirmClick(player, (ConfirmHolder) holder, event.getSlot());
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        List<ShopCategory> categories = plugin.getConfigManager().getCategories();
        if (slot < 0 || slot >= categories.size()) return;
        ShopCategory category = categories.get(slot);
        playClick(player);
        plugin.getGuiManager().openCategory(player, category, 0);
    }

    private void handleCategoryClick(Player player, CategoryHolder holder, InventoryClickEvent event) {
        int slot = event.getSlot();
        ShopCategory category = holder.getCategory();
        int page = holder.getPage();

        if (slot == 45) {
            plugin.getGuiManager().openCategory(player, category, page - 1);
            return;
        }
        if (slot == 49) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }
        if (slot == 53) {
            plugin.getGuiManager().openCategory(player, category, page + 1);
            return;
        }
        if (slot >= GUIManager.ITEMS_PER_PAGE) return;

        int index = page * GUIManager.ITEMS_PER_PAGE + slot;
        if (index < 0 || index >= category.getItems().size()) return;
        ShopItem item = category.getItems().get(index);

        ClickType clickType = event.getClick();

        if (clickType.isLeftClick() && item.isBuyEnabled()) {
            int amount = clickType.isShiftClick() ? item.getMaterial().getMaxStackSize() : 1;
            buyItem(player, item, amount);
        } else if (clickType.isRightClick() && item.isSellEnabled()) {
            if (clickType.isShiftClick()) {
                sellAllOfItem(player, item);
            } else {
                sellItem(player, item, 1);
            }
        }
        plugin.getGuiManager().openCategory(player, category, page);
    }

    private void handleConfirmClick(Player player, ConfirmHolder holder, int slot) {
        if (slot == 11) {
            Map<ShopItem, Integer> sellMap = holder.getSellMap();
            double total = 0;
            for (Map.Entry<ShopItem, Integer> entry : sellMap.entrySet()) {
                total += processSellFromInventory(player, entry.getKey(), entry.getValue());
            }
            plugin.getEconomyManager().deposit(player, total);
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "sell-all-success",
                    "%amount%", plugin.getEconomyManager().format(total));
            playSuccess(player);
            player.closeInventory();
        } else if (slot == 15) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "sell-all-cancelled");
            player.closeInventory();
        }
    }

    public void buyItem(Player player, ShopItem item, int amount) {
        double price = plugin.getPriceEngine().getBuyPrice(item) * amount;
        if (!plugin.getEconomyManager().has(player, price)) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "not-enough-money",
                    "%price%", plugin.getEconomyManager().format(price));
            playFail(player);
            return;
        }
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(item.getMaterial(), amount));
        if (!overflow.isEmpty()) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "inventory-full");
            playFail(player);
            return;
        }
        plugin.getEconomyManager().withdraw(player, price);
        plugin.getPriceEngine().onBuy(item, amount);
        plugin.getTransactionLogger().log(player.getName(), "BUY", item.getId(), amount, price);
        MessageUtil.send(player, plugin.getConfigManager().getMessages(), "buy-success",
                "%amount%", String.valueOf(amount),
                "%item%", GUIManager.formatMaterialName(item.getMaterial()),
                "%price%", plugin.getEconomyManager().format(price));
        playSuccess(player);
    }

    public void sellItem(Player player, ShopItem item, int amount) {
        int owned = countItem(player, item.getMaterial());
        if (owned < amount) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "not-enough-items");
            playFail(player);
            return;
        }
        double total = processSellFromInventory(player, item, amount);
        plugin.getEconomyManager().deposit(player, total);
        MessageUtil.send(player, plugin.getConfigManager().getMessages(), "sell-success",
                "%amount%", String.valueOf(amount),
                "%item%", GUIManager.formatMaterialName(item.getMaterial()),
                "%price%", plugin.getEconomyManager().format(total));
        playSuccess(player);
    }

    private void sellAllOfItem(Player player, ShopItem item) {
        int owned = countItem(player, item.getMaterial());
        if (owned <= 0) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "not-enough-items");
            playFail(player);
            return;
        }
        sellItem(player, item, owned);
    }

    private double processSellFromInventory(Player player, ShopItem item, int amount) {
        double unitPrice = plugin.getPriceEngine().getSellPrice(item);
        double total = unitPrice * amount;
        removeItems(player, item.getMaterial(), amount);
        plugin.getPriceEngine().onSell(item, amount);
        plugin.getTransactionLogger().log(player.getName(), "SELL", item.getId(), amount, total);
        return total;
    }

    private int countItem(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) count += stack.getAmount();
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) continue;
            int take = Math.min(amount, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            amount -= take;
            if (stack.getAmount() <= 0) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    public Map<ShopItem, Integer> collectSellableItems(Player player, double[] totalOut) {
        Map<ShopItem, Integer> result = new HashMap<>();
        double total = 0;
        for (ShopCategory category : plugin.getConfigManager().getCategories()) {
            for (ShopItem item : category.getItems()) {
                if (!item.isSellEnabled()) continue;
                int owned = countItem(player, item.getMaterial());
                if (owned > 0) {
                    result.put(item, owned);
                    total += plugin.getPriceEngine().getSellPrice(item) * owned;
                }
            }
        }
        totalOut[0] = total;
        return result;
    }

    private void playClick(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }

    private void playSuccess(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private void playFail(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }
}
