package com.notpatch.nLobby;

import com.notpatch.nlib.NLib;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.database.DatabaseManager;
import com.notpatch.nLobby.database.PlayerDAO;
import com.notpatch.nLobby.cache.PlayerCache;
import com.notpatch.nLobby.manager.SpawnManager;
import com.notpatch.nLobby.manager.HotbarManager;
import com.notpatch.nLobby.manager.VisibilityManager;
import com.notpatch.nLobby.manager.DoubleJumpManager;
import com.notpatch.nLobby.manager.QueueManager;
import com.notpatch.nLobby.manager.ChatManager;
import com.notpatch.nLobby.listener.PlayerJoinListener;
import com.notpatch.nLobby.listener.PlayerQuitListener;
import com.notpatch.nLobby.listener.PlayerDeathListener;
import com.notpatch.nLobby.listener.ProtectionListener;
import com.notpatch.nLobby.listener.PlayerInteractListener;
import com.notpatch.nLobby.listener.PlayerMoveListener;
import com.notpatch.nLobby.listener.PlayerToggleFlightListener;
import com.notpatch.nLobby.command.SpawnCommand;
import com.notpatch.nLobby.command.NLobbyCommand;
import com.notpatch.nLobby.command.CoinsCommand;
import com.notpatch.nLobby.command.LevelCommand;
import com.notpatch.nLobby.command.ServerCommand;
import com.notpatch.nLobby.command.QueueCommand;
import com.notpatch.nLobby.command.DirectCommand;

public final class NLobby extends JavaPlugin {

    @Getter
    private static NLobby instance;

    @Getter
    private LanguageLoader languageLoader;

    @Getter
    private ConfigManager configManager;

    @Getter
    private DatabaseManager databaseManager;

    @Getter
    private PlayerCache playerCache;

    @Getter
    private PlayerDAO playerDAO;

    @Getter
    private SpawnManager spawnManager;

    @Getter
    private HotbarManager hotbarManager;

    @Getter
    private VisibilityManager visibilityManager;

    @Getter
    private DoubleJumpManager doubleJumpManager;

    @Getter
    private QueueManager queueManager;

    @Getter
    private ChatManager chatManager;

    private PlayerQuitListener playerQuitListener;

    @Override
    public void onEnable() {
        instance = this;

        NLib.initialize(this);

        saveDefaultConfig();
        saveConfig();

        languageLoader = new LanguageLoader();
        languageLoader.loadLangs();

        configManager = new ConfigManager(this);

        try {
            databaseManager = new DatabaseManager(configManager);
            databaseManager.init();
            playerDAO = new PlayerDAO(databaseManager);
            playerCache = new PlayerCache();
            getLogger().info("Database initialized successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        spawnManager = new SpawnManager(configManager);
        hotbarManager = new HotbarManager(configManager);
        visibilityManager = new VisibilityManager();
        doubleJumpManager = new DoubleJumpManager(configManager);
        queueManager = new QueueManager(this, configManager);
        chatManager = new ChatManager(configManager, playerCache);

        PlayerJoinListener joinListener = new PlayerJoinListener(this, spawnManager, hotbarManager);
        joinListener.setVisibilityManager(visibilityManager);
        getServer().getPluginManager().registerEvents(joinListener, this);

        playerQuitListener = new PlayerQuitListener(this);
        playerQuitListener.setQueueManager(queueManager);
        getServer().getPluginManager().registerEvents(playerQuitListener, this);

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(configManager), this);

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(configManager, hotbarManager, visibilityManager, queueManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(doubleJumpManager, spawnManager, configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleFlightListener(doubleJumpManager), this);

        registerCommand("nlobby", new NLobbyCommand(this, spawnManager));
        registerCommand("coins", new CoinsCommand(this));
        registerCommand("level", new LevelCommand(this));
        registerCommand("server", new ServerCommand(queueManager, configManager));
        registerCommand("queue", new QueueCommand(queueManager, configManager));
        registerCommand("direct", new DirectCommand(queueManager, configManager));

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        queueManager.start();

    }


    @Override
    public void onDisable() {
        if (queueManager != null) {
            queueManager.stop();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
