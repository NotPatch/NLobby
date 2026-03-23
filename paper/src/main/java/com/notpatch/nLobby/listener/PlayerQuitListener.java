package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.cache.PlayerCache;
import com.notpatch.nLobby.database.PlayerDAO;
import com.notpatch.nLobby.manager.QueueManager;
import com.notpatch.nLobby.model.LobbyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerQuitListener implements Listener {
    private final NLobby plugin;
    private final PlayerDAO playerDAO;
    private final PlayerCache playerCache;
    private QueueManager queueManager;

    public PlayerQuitListener(NLobby plugin) {
        this.plugin = plugin;
        this.playerDAO = plugin.getPlayerDAO();
        this.playerCache = plugin.getPlayerCache();
    }

    public void setQueueManager(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        LobbyPlayer lobbyPlayer = playerCache.get(player.getUniqueId());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (lobbyPlayer != null) {
                    playerDAO.savePlayer(lobbyPlayer);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save player " + player.getName() + ": " + e.getMessage());
            }
        });

        playerCache.remove(player.getUniqueId());

        if (queueManager != null) {
            queueManager.onPlayerQuit(player);
        }

        String quitMsg = LanguageLoader.getMessage("quit.broadcast");
        event.setQuitMessage(quitMsg.replace("%player%", player.getName()));
    }
}
