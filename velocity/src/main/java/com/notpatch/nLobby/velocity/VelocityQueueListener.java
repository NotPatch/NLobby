package com.notpatch.nLobby.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.logging.Logger;

public class VelocityQueueListener {
    private final VelocityQueueManager queueManager;
    private final Logger logger;
    private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacy('&');

    public VelocityQueueListener(VelocityQueueManager queueManager, Logger logger) {
        this.queueManager = queueManager;
        this.logger = logger;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is in allowed list
        if (queueManager.isAllowed(player.getUniqueId())) {
            queueManager.removeAllowed(player.getUniqueId());
            return; // Allow login
        }

        // Check current queue size and max slots
        int currentlyAllowed = queueManager.getAllowedPlayers().size();

        // If there are open slots, allow this player
        if (currentlyAllowed < queueManager.getConfig().getMaxSlots()) {
            queueManager.allow(player.getUniqueId());
            return;
        }

        // Player must queue
        int position = queueManager.addToQueue(player.getUniqueId());
        int totalInQueue = queueManager.getQueueSize();

        String kickMessage = queueManager.getConfig().getKickMessage()
                .replace("%position%", String.valueOf(position))
                .replace("%total%", String.valueOf(totalInQueue));

        Component message = serializer.deserialize(kickMessage);
        event.setResult(LoginEvent.ComponentResult.denied(message));
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        if (!queueManager.getConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        queueManager.removeFromQueue(player.getUniqueId());
    }
}
