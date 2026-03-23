package com.notpatch.nLobby.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
    id = "nlobby-velocity",
    name = "NLobby Velocity",
    version = "1.0-SNAPSHOT",
    authors = {"notpatch"}
)
@Getter
public class NLobbyVelocity {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    private VelocityQueueConfig queueConfig;
    private VelocityQueueManager queueManager;

    @Inject
    public NLobbyVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("NLobby Velocity plugin is initializing...");

        try {
            // Create data directory if it doesn't exist
            Files.createDirectories(dataDirectory);

            // Load configuration
            Path configPath = dataDirectory.resolve("config.yml");
            if (!Files.exists(configPath)) {
                logger.warning("Config file not found at " + configPath + ". Using defaults.");
            }

            queueConfig = new VelocityQueueConfig(configPath);

            // Initialize queue manager
            queueManager = new VelocityQueueManager(proxyServer, logger, queueConfig);

            // Register event listener
            proxyServer.getEventManager().register(this, new VelocityQueueListener(queueManager, logger));

            // Start the queue processing task
            queueManager.start(this);

            logger.info("NLobby Velocity plugin loaded successfully!");

        } catch (Exception e) {
            logger.severe("Failed to initialize NLobby Velocity plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onProxyShutdown() {
        if (queueManager != null) {
            queueManager.stop();
            logger.info("NLobby Velocity plugin disabled.");
        }
    }
}
