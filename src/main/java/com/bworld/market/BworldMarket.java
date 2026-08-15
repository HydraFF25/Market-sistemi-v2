package com.bworld.market;

import com.bworld.market.commands.AdminCommand;
import com.bworld.market.commands.MarketCommand;
import com.bworld.market.config.ConfigManager;
import com.bworld.market.economy.EconomyManager;
import com.bworld.market.economy.PriceEngine;
import com.bworld.market.gui.GUIListener;
import com.bworld.market.gui.GUIManager;
import com.bworld.market.log.TransactionLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class BworldMarket extends JavaPlugin {

    private static BworldMarket instance;

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private PriceEngine priceEngine;
    private GUIManager guiManager;
    private GUIListener guiListener;
    private TransactionLogger transactionLogger;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.economyManager = new EconomyManager();
        if (!this.economyManager.setup(this)) {
            getLogger().severe("Vault bulunamadi veya bir ekonomi eklentisi kurulu degil!");
            getLogger().severe("BworldMarket devre disi birakiliyor. Lutfen Vault ve EssentialsX gibi bir ekonomi eklentisi kurun.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.transactionLogger = new TransactionLogger(this);
        this.priceEngine = new PriceEngine(this);
        this.guiManager = new GUIManager(this);
        this.guiListener = new GUIListener(this);

        getServer().getPluginManager().registerEvents(guiListener, this);

        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("bworldmarket").setExecutor(new AdminCommand(this));

        getLogger().info("BworldMarket basariyla etkinlestirildi!");
        getLogger().info(configManager.getCategories().size() + " kategori yuklendi.");
    }

    @Override
    public void onDisable() {
        if (priceEngine != null) {
            priceEngine.save();
        }
        getLogger().info("BworldMarket devre disi birakildi.");
    }

    public static BworldMarket getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PriceEngine getPriceEngine() {
        return priceEngine;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUIListener getGuiListener() {
        return guiListener;
    }

    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
    }
}
