package com.bworld.market.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtil {

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void send(CommandSender sender, FileConfiguration messages, String path, String... placeholders) {
        if (messages == null) return;
        String msg = messages.getString(path);
        if (msg == null) {
            return;
        }
        msg = apply(msg, placeholders);
        sender.sendMessage(color(msg));
    }

    public static String get(FileConfiguration messages, String path, String... placeholders) {
        String msg = messages.getString(path, path);
        return color(apply(msg, placeholders));
    }

    private static String apply(String msg, String... placeholders) {
        if (placeholders == null) return msg;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }
}
