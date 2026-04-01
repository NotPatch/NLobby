package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.model.QueueEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class QueueManager {
    private final NLobby plugin;
    private final ConfigManager configManager;
    private static final String BUNGEE_CHANNEL = "BungeeCord";
    private final Map<String, LinkedList<QueueEntry>> queues = new HashMap<>();
    private final Map<UUID, String> playerQueues = new HashMap<>();
    private final Map<UUID, Integer> pendingTransferTasks = new HashMap<>();
    private int tickTaskId = -1;
    private int actionBarTaskId = -1;

    public QueueManager(NLobby plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void start() {
        int interval = configManager.getQueueTickInterval() * 20;
        tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::processTick, interval, interval);
        actionBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAllActionBars, 20, 20);
    }

    public void stop() {
        if (tickTaskId != -1) Bukkit.getScheduler().cancelTask(tickTaskId);
        if (actionBarTaskId != -1) Bukkit.getScheduler().cancelTask(actionBarTaskId);
        for (Integer taskId : pendingTransferTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        pendingTransferTasks.clear();
    }

    public boolean joinQueue(Player player, String serverName) {
        if (playerQueues.containsKey(player.getUniqueId())) {
            player.sendMessage(mm("<red>✖ Zaten bir sunucu sırasındasın."));
            return false;
        }

        QueueEntry entry = new QueueEntry(player.getUniqueId(), serverName);
        queues.computeIfAbsent(serverName, k -> new LinkedList<>()).add(entry);
        playerQueues.put(player.getUniqueId(), serverName);

        int position = queues.get(serverName).size();
        int total = queues.get(serverName).size();
        int eta = (int) Math.ceil((double) position) * configManager.getQueueTickInterval();

        player.sendMessage(mm("<gray>                                        "));
        player.sendMessage(mm("<yellow>⌛ <white><bold>" + serverName + "</bold></white> <yellow>sırasına girdin!"));
        player.sendMessage(mm("   <dark_gray>▸ <gray>Pozisyon: <white>" + position + "<gray>/" + total
                + "  <dark_gray>|  <gray>Tahmini: <white>~" + eta + "sn"));
        player.sendMessage(mm("<gray>                                        "));

        return true;
    }

    public void leaveQueue(Player player) {
        String serverName = playerQueues.remove(player.getUniqueId());
        if (serverName == null) return;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue != null) {
            queue.removeIf(e -> e.getUuid().equals(player.getUniqueId()));
        }

        player.sendMessage(mm("<red>✖ <gray>Sıradan çıktın."));
    }

    private void processTick() {
        for (Map.Entry<String, LinkedList<QueueEntry>> entry : queues.entrySet()) {
            String serverName = entry.getKey();
            LinkedList<QueueEntry> queue = entry.getValue();

            if (queue.isEmpty()) continue;

            QueueEntry qEntry = queue.poll();
            Player player = Bukkit.getPlayer(qEntry.getUuid());

            if (player != null && player.isOnline()) {
                playerQueues.remove(player.getUniqueId());
                sendConnecting(player, serverName);
                connectPlayer(player, serverName);
            }
        }
    }

    private void sendConnecting(Player player, String serverName) {
        player.sendActionBar(mm("<green>✔ <white>Bağlanılıyor → <green><bold>" + serverName + "</bold></green>..."));
        player.sendMessage(mm("<green>✔ <gray>Sıran geldi! <white><bold>" + serverName + "</bold></white> <gray>sunucusuna aktarılıyorsun..."));
    }

    private void connectPlayer(Player player, String serverName) {
        scheduleTransferFailCheck(player, serverName);
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteStream)) {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, BUNGEE_CHANNEL, byteStream.toByteArray());
        } catch (IOException e) {
            clearPendingTransfer(player.getUniqueId());
            plugin.getLogger().warning("Failed to send proxy connect message for " + player.getName() + ": " + e.getMessage());
            sendTransferFailed(player, serverName);
        } catch (Exception e) {
            clearPendingTransfer(player.getUniqueId());
            plugin.getLogger().log(Level.SEVERE, "Proxy connect error: " + player.getName() + " -> " + serverName, e);
            sendTransferFailed(player, serverName);
        }
    }

    public void connectPlayerDirect(Player player, String serverName) {
        leaveQueue(player);
        sendConnecting(player, serverName);
        connectPlayer(player, serverName);
    }

    public void updateActionBar(Player player) {
        if (!configManager.isQueueShowPositionActionBar()) return;

        String serverName = playerQueues.get(player.getUniqueId());
        if (serverName == null) return;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue == null) return;

        int position = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUuid().equals(player.getUniqueId())) {
                position = i + 1;
                break;
            }
        }
        if (position < 0) return;

        int total = queue.size();
        int eta = (int) Math.ceil((double) position) * configManager.getQueueTickInterval();
        String bar = buildProgressBar(position, total);

        player.sendActionBar(mm(
                "<yellow>⌛ " + bar + " <gray>Sıra: <white>" + position + "<gray>/" + total
                        + " <dark_gray>| <gray>~<white>" + eta + "sn"
                        + " <dark_gray>| <aqua>" + serverName
        ));
    }

    public void updateAllActionBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateActionBar(player);
        }
    }

    public void onPlayerQuit(Player player) {
        clearPendingTransfer(player.getUniqueId());
        leaveQueue(player);
    }

    private void scheduleTransferFailCheck(Player player, String serverName) {
        UUID uuid = player.getUniqueId();
        clearPendingTransfer(uuid);
        long delayTicks = Math.max(20L, configManager.getQueueConnectTimeoutSeconds() * 20L);
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            pendingTransferTasks.remove(uuid);
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                sendTransferFailed(onlinePlayer, serverName);
            }
        }, delayTicks);
        pendingTransferTasks.put(uuid, taskId);
    }

    private void clearPendingTransfer(UUID uuid) {
        Integer taskId = pendingTransferTasks.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void sendTransferFailed(Player player, String serverName) {
        String message = LanguageLoader.getMessage("queue.transfer-failed")
                .replace("%server%", serverName);
        player.sendMessage(message);
    }

    public int getQueuePosition(Player player) {
        String serverName = playerQueues.get(player.getUniqueId());
        if (serverName == null) return -1;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue == null) return -1;

        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUuid().equals(player.getUniqueId())) return i + 1;
        }
        return -1;
    }

    public String getPlayerQueue(Player player) {
        return playerQueues.get(player.getUniqueId());
    }

    private String buildProgressBar(int position, int total) {
        int barLength = 10;
        int filled = barLength - (int) Math.ceil(((double) position / total) * barLength);
        filled = Math.max(0, Math.min(barLength, filled));
        return "<green>" + "█".repeat(filled) + "</green><dark_gray>" + "█".repeat(barLength - filled) + "</dark_gray>";
    }

    private Component mm(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
