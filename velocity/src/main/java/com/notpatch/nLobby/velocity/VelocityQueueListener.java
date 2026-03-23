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
     * Handle player server connection attempts - queue only for initial login
     * Lobby→Other servers use Paper queue system (QueueManager)
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String limboServerName = queueManager.getConfig().getQueueServer();

        // Get target server
        RegisteredServer targetServer;
        try {
            targetServer = event.getOriginalServer();
            if (targetServer == null) {
                return;
            }
        } catch (NoSuchMethodError e) {
            logger.warning("Unable to get target server from ServerPreConnectEvent");
            return;
        }

        // If target is the limbo server itself, always allow
        if (targetServer.getServerInfo().getName().equalsIgnoreCase(limboServerName)) {
            return;
        }

        // IMPORTANT: Only queue on INITIAL LOGIN (when player has no current server)
        // If player is coming from another server (e.g., lobby), let Paper queue handle it
        boolean isInitialLogin = player.getCurrentServer().isEmpty();
        if (!isInitialLogin) {
            // Player is switching from another server (e.g., lobby→survival)
            // Paper queue system will handle this - do NOT intercept
            return;
        }

        // ===== INITIAL LOGIN QUEUE LOGIC =====

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

        // Player needs to queue - redirect to limbo (INITIAL LOGIN ONLY)
        int position = queueManager.addToQueue(player.getUniqueId());
        int totalInQueue = queueManager.getQueueSize();

        logger.info("Player " + player.getUsername() + " queued for " + targetServer.getServerInfo().getName() +
                " (initial login) - Position: " + position + "/" + totalInQueue);

        // Redirect to limbo server
        RegisteredServer limbo = queueManager.getProxyServer()
                .getServer(limboServerName)
                .orElse(null);

        if (limbo != null) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(limbo));
        } else {
            logger.warning("Limbo server '" + limboServerName + "' not found!");
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
