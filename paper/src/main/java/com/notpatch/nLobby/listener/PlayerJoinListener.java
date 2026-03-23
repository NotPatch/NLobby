package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.cache.PlayerCache;
import com.notpatch.nLobby.database.PlayerDAO;
import com.notpatch.nLobby.manager.HotbarManager;
import com.notpatch.nLobby.manager.SpawnManager;
import com.notpatch.nLobby.manager.VisibilityManager;
import com.notpatch.nLobby.model.LobbyPlayer;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final NLobby plugin;
    private final SpawnManager spawnManager;
    private final HotbarManager hotbarManager;
    private final PlayerDAO playerDAO;
    private final PlayerCache playerCache;

    public PlayerJoinListener(NLobby plugin, SpawnManager spawnManager, HotbarManager hotbarManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
        this.hotbarManager = hotbarManager;
        this.playerDAO = plugin.getPlayerDAO();
        this.playerCache = plugin.getPlayerCache();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setFoodLevel(20);
        player.setHealth(20);
        long joinTime = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                LobbyPlayer existing = playerDAO.loadPlayer(player.getUniqueId());
                if (existing == null) {
                    existing = new LobbyPlayer(
                            player.getUniqueId(),
                            player.getName(),
                            0,
                            1,
                            0,
                            joinTime,
                            joinTime
                    );
                } else {
                    existing.setLastJoin(joinTime);
                }

                LobbyPlayer finalLobbyPlayer = existing;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerCache.put(player.getUniqueId(), finalLobbyPlayer);

                    if (plugin.getConfigManager().teleportOnJoin() && spawnManager.isSpawnSet()) {
                        spawnManager.teleportToSpawn(player);
                    }

                    hotbarManager.giveHotbarItems(player);

                    String welcomeMsg = LanguageLoader.getMessage("join.welcome");
                    player.sendMessage(welcomeMsg.replace("%player%", player.getName()));

                    String broadcastMsg = LanguageLoader.getMessage("join.broadcast");
                    Bukkit.broadcast(Component.text(broadcastMsg.replace("%player%", player.getName())));

                    if (visibilityManager != null) {
                        visibilityManager.onPlayerJoin(player);
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load player " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    @Setter
    private VisibilityManager visibilityManager;
}
