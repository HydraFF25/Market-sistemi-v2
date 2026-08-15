package com.bworld.market.commands;

import com.bworld.market.BworldMarket;
import com.bworld.market.model.ShopCategory;
import com.bworld.market.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final BworldMarket plugin;

    public AdminCommand(BworldMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bworldmarket.admin")) {
            sender.sendMessage(MessageUtil.color("&cBu komutu kullanma yetkin yok."));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage(MessageUtil.color("&aBworldMarket ayarlari yeniden yuklendi."));
                return true;

            case "additem":
                if (args.length < 5) {
                    sender.sendMessage(MessageUtil.color("&cKullanim: /bworldmarket additem <kategori> <materyal> <alis-fiyati> <satis-fiyati>"));
                    return true;
                }
                return handleAddItem(sender, args);

            case "removeitem":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtil.color("&cKullanim: /bworldmarket removeitem <kategori> <materyal>"));
                    return true;
                }
                return handleRemoveItem(sender, args);

            case "setbuyprice":
            case "setsellprice":
                if (args.length < 4) {
                    sender.sendMessage(MessageUtil.color("&cKullanim: /bworldmarket " + args[0] + " <kategori> <materyal> <fiyat>"));
                    return true;
                }
                return handleSetPrice(sender, args, args[0].equalsIgnoreCase("setbuyprice"));

            case "list":
                handleList(sender);
                return true;

            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleAddItem(CommandSender sender, String[] args) {
        String categoryId = args[1];
        Material material = parseMaterial(sender, args[2]);
        if (material == null) return true;

        double buy;
        double sell;
        try {
            buy = Double.parseDouble(args[3]);
            sell = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.color("&cGecersiz fiyat degeri."));
            return true;
        }

        plugin.getConfigManager().addItem(categoryId, material, buy, sell);
        sender.sendMessage(MessageUtil.color("&a" + material.name() + " urunu '" + categoryId + "' kategorisine eklendi."));
        return true;
    }

    private boolean handleRemoveItem(CommandSender sender, String[] args) {
        String categoryId = args[1];
        Material material = parseMaterial(sender, args[2]);
        if (material == null) return true;

        boolean removed = plugin.getConfigManager().removeItem(categoryId, material);
        if (removed) {
            sender.sendMessage(MessageUtil.color("&a" + material.name() + " urunu kaldirildi."));
        } else {
            sender.sendMessage(MessageUtil.color("&cUrun bulunamadi."));
        }
        return true;
    }

    private boolean handleSetPrice(CommandSender sender, String[] args, boolean buy) {
        String categoryId = args[1];
        Material material = parseMaterial(sender, args[2]);
        if (material == null) return true;

        double price;
        try {
            price = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.color("&cGecersiz fiyat degeri."));
            return true;
        }

        boolean ok = plugin.getConfigManager().setPrice(categoryId, material, buy, price);
        if (ok) {
            sender.sendMessage(MessageUtil.color("&aFiyat guncellendi."));
        } else {
            sender.sendMessage(MessageUtil.color("&cUrun veya kategori bulunamadi."));
        }
        return true;
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&l--- BworldMarket Kategorileri ---"));
        for (ShopCategory category : plugin.getConfigManager().getCategories()) {
            sender.sendMessage(MessageUtil.color("&b" + category.getId() + " &7(" + category.getItems().size() + " urun)"));
        }
    }

    private Material parseMaterial(CommandSender sender, String s) {
        try {
            return Material.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageUtil.color("&cGecersiz materyal: " + s));
            return null;
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&l--- BworldMarket Admin ---"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket reload &7- Ayarlari yeniden yukler"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket additem <kategori> <materyal> <alis> <satis>"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket removeitem <kategori> <materyal>"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket setbuyprice <kategori> <materyal> <fiyat>"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket setsellprice <kategori> <materyal> <fiyat>"));
        sender.sendMessage(MessageUtil.color("&e/bworldmarket list &7- Kategorileri listeler"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Arrays.asList("reload", "additem", "removeitem", "setbuyprice", "setsellprice", "list")) {
                if (s.startsWith(args[0].toLowerCase())) result.add(s);
            }
        } else if (args.length == 2
                && Arrays.asList("additem", "removeitem", "setbuyprice", "setsellprice").contains(args[0].toLowerCase())) {
            for (ShopCategory c : plugin.getConfigManager().getCategories()) {
                if (c.getId().startsWith(args[1].toLowerCase())) result.add(c.getId());
            }
        }
        return result;
    }
}
