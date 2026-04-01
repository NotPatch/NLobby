package com.notpatch.nLobby.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import net.elytrium.limboapi.api.Limbo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
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
    private final ConcurrentHashMap<UUID, LimboQueueSessionHandler> sessionHandlers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RegisteredServer> queuedTargets = new ConcurrentHashMap<>();

    private ScheduledTask processingTask;
    private ScheduledTask actionBarTask;
    private Limbo limbo;
    private Object plugin;

    public VelocityQueueManager(ProxyServer proxyServer, Logger logger, VelocityQueueConfig config) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
    }

    public void setLimbo(Limbo limbo) {
        this.limbo = limbo;
    }

    public void start(Object plugin) {
        if (!config.isEnabled()) {
            logger.info("NLobby Velocity Queue is disabled.");
            return;
        }

        this.plugin = plugin;
        long tickIntervalMs = config.getTickInterval() * 1000L;

        processingTask = proxyServer.getScheduler()
                .buildTask(plugin, this::processTick)
                .delay(tickIntervalMs, TimeUnit.MILLISECONDS)
                .repeat(tickIntervalMs, TimeUnit.MILLISECONDS)
                .schedule();

        actionBarTask = proxyServer.getScheduler()
                .buildTask(plugin, this::sendActionBars)
                .delay(1, TimeUnit.SECONDS)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();

        logger.info("NLobby Velocity Queue started. Slots per tick: " + config.getSlotsPerTick()
                + ", Tick interval: " + config.getTickInterval() + "s");
    }

    public void stop() {
        if (processingTask != null) processingTask.cancel();
        if (actionBarTask != null) actionBarTask.cancel();
        logger.info("NLobby Velocity Queue stopped.");
    }

    public boolean isAllowed(UUID uuid) {
        return allowedPlayers.containsKey(uuid);
    }

    public void allow(UUID uuid) {
        allowedPlayers.put(uuid, true);
    }

    public void removeAllowed(UUID uuid) {
        allowedPlayers.remove(uuid);
    }

    public int addToQueue(UUID uuid, RegisteredServer targetServer) {
        queuedTargets.put(uuid, targetServer);
        if (pendingQueue.contains(uuid)) {
            return getPosition(uuid);
        }
        pendingQueue.add(uuid);
        return getPosition(uuid);
    }

    public void registerSessionHandler(UUID uuid, LimboQueueSessionHandler handler) {
        sessionHandlers.put(uuid, handler);
    }

    public void unregisterSessionHandler(UUID uuid) {
        sessionHandlers.remove(uuid);
    }

    public int getPosition(UUID uuid) {
        int position = 1;
        for (UUID queued : pendingQueue) {
            if (queued.equals(uuid)) return position;
            position++;
        }
        return -1;
    }

    public int getQueueSize() {
        return pendingQueue.size();
    }

    public void removeFromQueue(UUID uuid) {
        pendingQueue.remove(uuid);
        removeAllowed(uuid);
        sessionHandlers.remove(uuid);
        queuedTargets.remove(uuid);
    }

    private void processTick() {
        for (int i = 0; i < config.getSlotsPerTick(); i++) {
            UUID nextPlayer = pendingQueue.poll();
            if (nextPlayer == null) break;

            LimboQueueSessionHandler handler = sessionHandlers.remove(nextPlayer);
            RegisteredServer target = queuedTargets.remove(nextPlayer);

            if (handler == null || target == null) {
                removeAllowed(nextPlayer);
                continue;
            }

            allow(nextPlayer);
            logger.info("Released " + nextPlayer + " from queue → " + target.getServerInfo().getName());
            proxyServer.getPlayer(nextPlayer).ifPresent(p ->
                    p.sendActionBar(MiniMessage.miniMessage().deserialize(
                            "<green>✔ Bağlanılıyor → <white>" + target.getServerInfo().getName() + "</white>..."
                    ))
            );

            handler.releaseToServer(target);
        }
    }

    private void sendActionBars() {
        if (pendingQueue.isEmpty()) return;

        int total = pendingQueue.size();
        List<UUID> snapshot = new ArrayList<>(pendingQueue);

        for (int i = 0; i < snapshot.size(); i++) {
            UUID uuid = snapshot.get(i);
            int position = i + 1;
            int ticksNeeded = (int) Math.ceil((double) position / config.getSlotsPerTick());
            int secondsLeft = ticksNeeded * config.getTickInterval();

            RegisteredServer target = queuedTargets.get(uuid);
            String serverName = target != null ? target.getServerInfo().getName() : "?";

            String bar = buildProgressBar(position, total);

            Component actionBar = MiniMessage.miniMessage().deserialize(
                    "<yellow>⌛ <white>" + bar + " <gray>Sıra: <white>" + position + "<gray>/" + total
                            + " <dark_gray>| <gray>~<white>" + secondsLeft + "sn"
                            + " <dark_gray>| <aqua>" + serverName
            );

            proxyServer.getPlayer(uuid).ifPresent(p -> p.sendActionBar(actionBar));
        }
    }

    private String buildProgressBar(int position, int total) {
        int barLength = 10;
        int filled = barLength - (int) Math.ceil(((double) position / total) * barLength);
        filled = Math.max(0, Math.min(barLength, filled));

        String green = "<green>" + "█".repeat(filled) + "</green>";
        String gray = "<dark_gray>" + "█".repeat(barLength - filled) + "</dark_gray>";
        return green + gray;
    }
}
