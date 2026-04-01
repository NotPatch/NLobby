package com.notpatch.nLobby.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
    id = "nlobby-velocity",
    name = "NLobby Velocity",
    version = "1.0-SNAPSHOT",
    authors = {"notpatch"},
    dependencies = {
        @Dependency(id = "limboapi")
    }
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
            Files.createDirectories(dataDirectory);

            Path configPath = dataDirectory.resolve("config.yml");
            if (!Files.exists(configPath)) {
                try (var defaultConfig = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (defaultConfig != null) {
                        Files.copy(defaultConfig, configPath);
                        logger.info("Created default config file at " + configPath);
                    } else {
                        logger.warning("Default config.yml not found in resources!");
                    }
                } catch (Exception e) {
                    logger.warning("Failed to copy default config: " + e.getMessage());
                }
            }

            queueConfig = new VelocityQueueConfig(configPath);
            queueManager = new VelocityQueueManager(proxyServer, logger, queueConfig);
            LimboFactory limboFactory = proxyServer.getPluginManager()
                    .getPlugin("limboapi")
                    .flatMap(PluginContainer::getInstance)
                    .map(LimboFactory.class::cast)
                    .orElseThrow(() -> new RuntimeException("LimboAPI plugin not found! Install LimboAPI on Velocity."));
            VirtualWorld world = limboFactory.createVirtualWorld(
                    Dimension.OVERWORLD,
                    0.0, 64.0, 0.0,
                    0.0f, 0.0f
            );
            Limbo limbo = limboFactory.createLimbo(world);
            queueManager.setLimbo(limbo);
            logger.info("LimboAPI virtual limbo created successfully!");

            proxyServer.getEventManager().register(this, new VelocityQueueListener(queueManager, logger));
            proxyServer.getCommandManager().register("nlqueue", new VelocityQueueCommand(queueManager));
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
