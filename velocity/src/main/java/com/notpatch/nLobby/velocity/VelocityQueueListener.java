package com.notpatch.nLobby.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.logging.Logger;

public class VelocityQueueListener {
    private final VelocityQueueManager queueManager;
    private final Logger logger;

    public VelocityQueueListener(VelocityQueueManager queueManager, Logger logger) {
        this.queueManager = queueManager;
        this.logger = logger;
    }

    /**
     * Handle player server connection attempts - redirect to queue server if needed
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // In Velocity 3.x, the target server is in the event
        // We need to check the result or use getOriginalServer()
        RegisteredServer targetServer;
        try {
            // Try to get the server that was resolved
            targetServer = event.getOriginalServer();
            if (targetServer == null) {
                return;
            }
        } catch (NoSuchMethodError e) {
            // If the method doesn't exist, we can't process this event
            logger.warning("Unable to get target server from ServerPreConnectEvent");
            return;
        }

        // If this is a connection to the queue server itself, allow it
        if (targetServer.getServerInfo().getName().equalsIgnoreCase(queueManager.getConfig().getQueueServer())) {
            return;
        }

        // Check if we should queue this connection
        boolean shouldCheck = queueManager.getConfig().getTargetServers().isEmpty() ||
                queueManager.getConfig().getTargetServers().contains(targetServer.getServerInfo().getName());

        if (!shouldCheck) {
            return;
        }

        // Check if player is allowed (in allowed list from previous tick)
        if (queueManager.isAllowed(player.getUniqueId())) {
            queueManager.removeAllowed(player.getUniqueId());
            return; // Allow connection
        }

        // Check current queue
        int currentlyAllowed = queueManager.getAllowedPlayers().size();

        // If there are open slots, allow this player
        if (currentlyAllowed < queueManager.getConfig().getMaxSlots()) {
            queueManager.allow(player.getUniqueId());
            return;
        }

        // Player needs to queue - redirect to queue server
        int position = queueManager.addToQueue(player.getUniqueId());
        int totalInQueue = queueManager.getQueueSize();

        logger.info("Player " + player.getUsername() + " queued for " + targetServer.getServerInfo().getName() +
                " - Position: " + position + "/" + totalInQueue);

        // Redirect to queue server
        RegisteredServer queueServer = queueManager.getProxyServer()
                .getServer(queueManager.getConfig().getQueueServer())
                .orElse(null);

        if (queueServer != null) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(queueServer));
        } else {
            logger.warning("Queue server '" + queueManager.getConfig().getQueueServer() + "' not found!");
        }
    }

    /**
     * Clean up when player disconnects
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        queueManager.removeFromQueue(player.getUniqueId());
    }
}
