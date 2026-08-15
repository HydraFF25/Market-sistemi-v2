package com.bworld.market.gui;

import com.bworld.market.BworldMarket;
import com.bworld.market.gui.holder.CategoryHolder;
import com.bworld.market.gui.holder.ConfirmHolder;
import com.bworld.market.gui.holder.MainMenuHolder;
import com.bworld.market.model.ShopCategory;
import com.bworld.market.model.ShopItem;
import com.bworld.market.util.ItemBuilder;
import com.bworld.market.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIManager {

    public static final int ITEMS_PER_PAGE = 45;

    private final BworldMarket plugin;

    public GUIManager(BworldMarket plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        List<ShopCategory> categories = plugin.getConfigManager().getCategories();
        int rows = Math.max(3, (int) Math.ceil((categories.size()) / 9.0) + 1);
        rows = Math.min(rows, 6);

        MainMenuHolder holder = new MainMenuHolder();
        Inventory inv = plugin.getServer().createInventory(holder, rows * 9,
                MessageUtil.color(plugin.getConfigManager().getGuiTitle()));
        holder.setInventory(inv);

        int slot = 0;
        for (ShopCategory category : categories) {
            if (slot >= inv.getSize()) break;
            List<String> lore = new ArrayList<>();
            lore.add("&7" + category.getItems().size() + " urun");
            lore.add("");
            lore.add("&eGoruntulemek icin tikla!");
            ItemStack icon = new ItemBuilder(category.getIcon())
                    .name("&b&l" + category.getDisplayName())
                    .lore(lore)
                    .build();
            inv.setItem(slot, icon);
            slot++;
        }

        player.openInventory(inv);
    }

    public void openCategory(Player player, ShopCategory category, int page) {
        int totalPages = Math.max(1, (int) Math.ceil(category.getItems().size() / (double) ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        CategoryHolder holder = new CategoryHolder(category, page);
        Inventory inv = plugin.getServer().createInventory(holder, 54,
                MessageUtil.color("&8&l" + category.getDisplayName()));
        holder.setInventory(inv);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, category.getItems().size());

        for (int i = start; i < end; i++) {
            ShopItem item = category.getItems().get(i);
            inv.setItem(i - start, buildShopItemIcon(item));
        }

        if (page > 0) {
            inv.setItem(45, new ItemBuilder(Material.ARROW).name("&e« Onceki Sayfa").build());
        }
        inv.setItem(49, new ItemBuilder(Material.BARRIER).name("&c« Ana Menu").build());
        if (page < totalPages - 1) {
            inv.setItem(53, new ItemBuilder(Material.ARROW).name("&eSonraki Sayfa »").build());
        }

        player.openInventory(inv);
    }

    private ItemStack buildShopItemIcon(ShopItem item) {
        double buyPrice = plugin.getPriceEngine().getBuyPrice(item);
        double sellPrice = plugin.getPriceEngine().getSellPrice(item);

        List<String> lore = new ArrayList<>();
        if (item.getLore() != null && !item.getLore().isEmpty()) {
            lore.addAll(item.getLore());
            lore.add("");
        }
        if (item.isBuyEnabled()) {
            lore.add("&aSatin Al: &f" + plugin.getEconomyManager().format(buyPrice) + " &7(1 adet)");
            lore.add("&aShift + Sol Tik: &f64 adet satin al");
        } else {
            lore.add("&7Satin alinamaz");
        }
        if (item.isSellEnabled()) {
            lore.add("&cSat: &f" + plugin.getEconomyManager().format(sellPrice) + " &7(1 adet)");
            lore.add("&cShift + Sag Tik: &fEnvanterdeki tumunu sat");
        } else {
            lore.add("&7Satilamaz");
        }

        String name = item.getDisplayName() != null
                ? item.getDisplayName()
                : "&f" + formatMaterialName(item.getMaterial());

        return new ItemBuilder(item.getMaterial())
                .name("&b" + org.bukkit.ChatColor.stripColor(MessageUtil.color(name)))
                .lore(lore)
                .customModelData(item.getCustomModelData())
                .build();
    }

    public static String formatMaterialName(Material material) {
        String[] parts = material.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    public void openSellAllConfirm(Player player, Map<ShopItem, Integer> sellMap, double total) {
        ConfirmHolder holder = new ConfirmHolder(sellMap, total);
        Inventory inv = plugin.getServer().createInventory(holder, 27, MessageUtil.color("&8Satisi Onayla"));
        holder.setInventory(inv);

        List<String> infoLore = new ArrayList<>();
        int totalItems = 0;
        for (int amount : sellMap.values()) totalItems += amount;
        infoLore.add("&7Toplam esya: &f" + totalItems);
        infoLore.add("&7Toplam kazanc: &a" + plugin.getEconomyManager().format(total));
        inv.setItem(13, new ItemBuilder(Material.GOLD_INGOT).name("&e&lSatis Ozeti").lore(infoLore).build());

        inv.setItem(11, new ItemBuilder(Material.LIME_WOOL).name("&a&lONAYLA").build());
        inv.setItem(15, new ItemBuilder(Material.RED_WOOL).name("&c&lIPTAL").build());

        player.openInventory(inv);
    }
}
