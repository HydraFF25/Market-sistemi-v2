package com.bworld.market.commands;

import com.bworld.market.BworldMarket;
import com.bworld.market.gui.GUIManager;
import com.bworld.market.model.ShopCategory;
import com.bworld.market.model.ShopItem;
import com.bworld.market.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MarketCommand implements CommandExecutor, TabCompleter {

    private final BworldMarket plugin;

    public MarketCommand(BworldMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("bworldmarket.use")) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "sellhand":
                sellHand(player);
                return true;
            case "sellall":
                sellAll(player);
                return true;
            default:
                MessageUtil.send(player, plugin.getConfigManager().getMessages(), "help");
                return true;
        }
    }

    private void sellHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "not-enough-items");
            return;
        }
        ShopItem found = null;
        for (ShopCategory category : plugin.getConfigManager().getCategories()) {
            ShopItem item = category.getItem(hand.getType());
            if (item != null && item.isSellEnabled()) {
                found = item;
                break;
            }
        }
        if (found == null) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "item-not-sellable");
            return;
        }
        int amount = hand.getAmount();
        double unitPrice = plugin.getPriceEngine().getSellPrice(found);
        double total = unitPrice * amount;
        player.getInventory().setItemInMainHand(null);
        plugin.getPriceEngine().onSell(found, amount);
        plugin.getEconomyManager().deposit(player, total);
        plugin.getTransactionLogger().log(player.getName(), "SELL_HAND", found.getId(), amount, total);
        MessageUtil.send(player, plugin.getConfigManager().getMessages(), "sell-success",
                "%amount%", String.valueOf(amount),
                "%item%", GUIManager.formatMaterialName(found.getMaterial()),
                "%price%", plugin.getEconomyManager().format(total));
    }

    private void sellAll(Player player) {
        double[] totalOut = new double[1];
        Map<ShopItem, Integer> sellable = plugin.getGuiListener().collectSellableItems(player, totalOut);
        if (sellable.isEmpty()) {
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "not-enough-items");
            return;
        }
        if (plugin.getConfigManager().isConfirmSellAll()) {
            plugin.getGuiManager().openSellAllConfirm(player, sellable, totalOut[0]);
        } else {
            double total = 0;
            for (Map.Entry<ShopItem, Integer> entry : sellable.entrySet()) {
                total += sellDirect(player, entry.getKey(), entry.getValue());
            }
            plugin.getEconomyManager().deposit(player, total);
            MessageUtil.send(player, plugin.getConfigManager().getMessages(), "sell-all-success",
                    "%amount%", plugin.getEconomyManager().format(total));
        }
    }

    private double sellDirect(Player player, ShopItem item, int amount) {
        double unitPrice = plugin.getPriceEngine().getSellPrice(item);
        double total = unitPrice * amount;
        removeAll(player, item);
        plugin.getPriceEngine().onSell(item, amount);
        plugin.getTransactionLogger().log(player.getName(), "SELL", item.getId(), amount, total);
        return total;
    }

    private void removeAll(Player player, ShopItem item) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == item.getMaterial()) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("sellhand", "sellall");
            List<String> result = new ArrayList<>();
            for (String o : options) {
                if (o.startsWith(args[0].toLowerCase())) result.add(o);
            }
            return result;
        }
        return new ArrayList<>();
    }
}
