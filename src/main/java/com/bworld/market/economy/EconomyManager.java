package com.bworld.market.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private Economy economy;

    public boolean setup(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isReady() {
        return economy != null;
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    public void withdraw(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    public void deposit(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public String format(double amount) {
        if (economy == null) return String.format("%.2f", amount);
        return economy.format(amount);
    }
}
