package com.bworld.market.log;

import com.bworld.market.BworldMarket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class TransactionLogger {

    private final BworldMarket plugin;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionLogger(BworldMarket plugin) {
        this.plugin = plugin;
    }

    public void log(String playerName, String action, String itemId, int amount, double total) {
        if (!plugin.getConfigManager().isLogTransactions()) return;
        String line = String.format("[%s] %s | %s | %s x%d | %.2f",
                LocalDateTime.now().format(formatter), playerName, action, itemId, amount, total);
        try (FileWriter writer = new FileWriter(new File(plugin.getDataFolder(), "transactions.log"), true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "transaction loglanamadi", e);
        }
    }
}
