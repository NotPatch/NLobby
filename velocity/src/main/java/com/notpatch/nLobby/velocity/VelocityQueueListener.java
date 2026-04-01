package com.notpatch.nLobby.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.limboapi.api.Limbo;

import java.util.logging.Logger;

public class VelocityQueueListener {
    private final VelocityQueueManager queueManager;
    private final Logger logger;

    public VelocityQueueListener(VelocityQueueManager queueManager, Logger logger) {
        this.queueManager = queueManager;
        this.logger = logger;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getOriginalServer();
        if (targetServer == null) {
            return;
        }

        boolean shouldCheck = queueManager.getConfig().getTargetServers().isEmpty() ||
                queueManager.getConfig().getTargetServers().contains(targetServer.getServerInfo().getName());
        if (!shouldCheck) {
            return;
        }

        if (queueManager.isAllowed(player.getUniqueId())) {
            queueManager.removeAllowed(player.getUniqueId());
            return;
        }

        Limbo limbo = queueManager.getLimbo();
        if (limbo == null) {
            logger.warning("LimboAPI limbo is not initialized! Cannot queue player " + player.getUsername());
            return;
        }

        int position = queueManager.addToQueue(player.getUniqueId(), targetServer);
        int totalInQueue = queueManager.getQueueSize();

        logger.info("Player " + player.getUsername() + " queued for " + targetServer.getServerInfo().getName() +
                " — Position: " + position + "/" + totalInQueue);
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        limbo.spawnPlayer(player, new LimboQueueSessionHandler(player.getUniqueId(), queueManager));
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }
        queueManager.removeFromQueue(event.getPlayer().getUniqueId());
    }
}
