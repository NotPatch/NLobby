package com.notpatch.nLobby.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class VelocityQueueManager {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final VelocityQueueConfig config;

    private final ConcurrentLinkedQueue<UUID> pendingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<UUID, Boolean> allowedPlayers = new ConcurrentHashMap<>();
    private ScheduledTask processingTask;
    private RegisteredServer limboServer;

    public VelocityQueueManager(ProxyServer proxyServer, Logger logger, VelocityQueueConfig config) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
    }

    public void start(Object plugin) {
        if (!config.isEnabled()) {
            logger.info("NLobby Velocity Queue is disabled.");
            return;
        }

        String limboServerName = config.getQueueServer();

        // Find virtual limbo server (created by LimboAPI Velocity plugin)
        this.limboServer = proxyServer.getServer(limboServerName).orElse(null);

        if (this.limboServer != null) {
            logger.info("✅ Limbo server found: " + limboServerName);
        } else {
            logger.severe("❌ Limbo server '" + limboServerName + "' not found!");
            logger.severe("Make sure LimboAPI Velocity plugin is installed and configured!");
            logger.severe("Download from: https://github.com/Elytrium/LimboAPI/releases");
        }

        // Start the queue processing scheduler
        long tickIntervalMs = config.getTickInterval() * 1000; // Convert seconds to milliseconds
        processingTask = proxyServer.getScheduler()
                .buildTask(plugin, this::processTick)
                .delay(tickIntervalMs, TimeUnit.MILLISECONDS)
                .repeat(tickIntervalMs, TimeUnit.MILLISECONDS)
                .schedule();

        logger.info("NLobby Velocity Queue started. Max slots: " + config.getMaxSlots() +
                ", Slots per tick: " + config.getSlotsPerTick());
    }

    public void stop() {
        if (processingTask != null) {
            processingTask.cancel();
            logger.info("NLobby Velocity Queue stopped.");
        }
    }

    /**
     * Check if a player is allowed to join
     */
    public boolean isAllowed(UUID uuid) {
        return allowedPlayers.containsKey(uuid);
    }

    /**
     * Allow a player to join (add to allowed list)
     * No TTL - player stays until they login or disconnect
     */
    public void allow(UUID uuid) {
        allowedPlayers.put(uuid, true);
    }

    /**
     * Remove a player from allowed list (after they join)
     */
    public void removeAllowed(UUID uuid) {
        allowedPlayers.remove(uuid);
    }

    /**
     * Add a player to the queue
     * Returns the position in queue (1-indexed)
     */
    public int addToQueue(UUID uuid) {
        if (pendingQueue.contains(uuid)) {
            return getPosition(uuid);
        }

        pendingQueue.add(uuid);
        return getPosition(uuid);
    }

    /**
     * Get player's position in queue (1-indexed, -1 if not in queue)
     */
    public int getPosition(UUID uuid) {
        int position = 1;
        for (UUID queued : pendingQueue) {
            if (queued.equals(uuid)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * Get total queue size
     */
    public int getQueueSize() {
        return pendingQueue.size();
    }

    /**
     * Remove a player from queue (e.g., on disconnect)
     */
    public void removeFromQueue(UUID uuid) {
        pendingQueue.remove(uuid);
        removeAllowed(uuid);
    }

    /**
     * Process one tick: move N players from queue to allowed
     * Players stay in allowed list until they login or disconnect
     */
    private void processTick() {
        // Calculate available slots
        int currentlyAllowed = allowedPlayers.size();
        int availableSlots = config.getMaxSlots() - currentlyAllowed;

        if (availableSlots <= 0) {
            return; // No slots available
        }

        // Move up to slotsPerTick players from queue to allowed
        int toMove = Math.min(availableSlots, config.getSlotsPerTick());
        for (int i = 0; i < toMove; i++) {
            UUID nextPlayer = pendingQueue.poll();
            if (nextPlayer == null) {
                break;
            }
            allow(nextPlayer);
        }
    }
}
