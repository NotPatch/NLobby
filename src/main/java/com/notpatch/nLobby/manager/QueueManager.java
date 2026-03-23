package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.model.QueueEntry;
import net.kyori.adventure.text.Component;
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
    private int taskId = -1;

    public QueueManager(NLobby plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void start() {
        int interval = configManager.getQueueTickInterval() * 20;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::processTick, 0, interval);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public boolean joinQueue(Player player, String serverName) {
        if (playerQueues.containsKey(player.getUniqueId())) {
            player.sendMessage(LanguageLoader.getMessage("queue.already-in-queue"));
            return false;
        }

        QueueEntry entry = new QueueEntry(player.getUniqueId(), serverName);
        queues.computeIfAbsent(serverName, k -> new LinkedList<>()).add(entry);
        playerQueues.put(player.getUniqueId(), serverName);

        int position = queues.get(serverName).indexOf(entry) + 1;
        String msg = LanguageLoader.getMessage("queue.joined");
        msg = msg.replace("%server%", serverName).replace("%position%", String.valueOf(position));
        player.sendMessage(msg);

        updateActionBar(player);
        return true;
    }

    public void leaveQueue(Player player) {
        String serverName = playerQueues.remove(player.getUniqueId());
        if (serverName == null) return;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue != null) {
            queue.removeIf(e -> e.getUuid().equals(player.getUniqueId()));
        }

        player.sendMessage(LanguageLoader.getMessage("queue.left"));
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
                connectPlayer(player, serverName);
            }
        }
    }

    private void connectPlayer(Player player, String serverName) {
        String msg = LanguageLoader.getMessage("queue.sending");
        msg = msg.replace("%server%", serverName);
        player.sendMessage(msg);

        boolean registered = Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, BUNGEE_CHANNEL);

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteStream)) {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            byte[] payload = byteStream.toByteArray();
            player.sendPluginMessage(plugin, BUNGEE_CHANNEL, payload);

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send proxy connect message for " + player.getName() + ": " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Proxy connect message threw exception for " + player.getName() + " -> " + serverName, e);
        }
    }

    public void connectPlayerDirect(Player player, String serverName) {
        String msg = LanguageLoader.getMessage("queue.sending");
        msg = msg.replace("%server%", serverName);
        player.sendMessage(msg);

        leaveQueue(player);

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteStream)) {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            byte[] payload = byteStream.toByteArray();
            player.sendPluginMessage(plugin, BUNGEE_CHANNEL, payload);

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send direct proxy connect message for " + player.getName() + ": " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Direct proxy connect message threw exception for " + player.getName() + " -> " + serverName, e);
        }
    }


    public void updateActionBar(Player player) {
        if (!configManager.isQueueShowPositionActionBar()) return;

        String serverName = playerQueues.get(player.getUniqueId());
        if (serverName == null) return;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue == null) return;

        int position = 0;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUuid().equals(player.getUniqueId())) {
                position = i + 1;
                break;
            }
        }

        if (position > 0) {
            String actionbar = configManager.getQueueActionBarText();
            actionbar = actionbar.replace("%position%", String.valueOf(position));
            actionbar = actionbar.replace("%total%", String.valueOf(queue.size()));
            actionbar = actionbar.replace("%server%", serverName);

            player.sendActionBar(Component.text(colorize(actionbar)));
        }
    }

    public void updateAllActionBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateActionBar(player);
        }
    }

    public void onPlayerQuit(Player player) {
        leaveQueue(player);
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }

    public int getQueuePosition(Player player) {
        String serverName = playerQueues.get(player.getUniqueId());
        if (serverName == null) return -1;

        LinkedList<QueueEntry> queue = queues.get(serverName);
        if (queue == null) return -1;

        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUuid().equals(player.getUniqueId())) {
                return i + 1;
            }
        }
        return -1;
    }

    public String getPlayerQueue(Player player) {
        return playerQueues.get(player.getUniqueId());
    }
}
