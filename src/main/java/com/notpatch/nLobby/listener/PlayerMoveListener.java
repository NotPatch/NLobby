package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.manager.DoubleJumpManager;
import com.notpatch.nLobby.manager.SpawnManager;
import com.notpatch.nLobby.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final DoubleJumpManager doubleJumpManager;
    private final SpawnManager spawnManager;
    private final ConfigManager configManager;

    public PlayerMoveListener(DoubleJumpManager doubleJumpManager, SpawnManager spawnManager, ConfigManager configManager) {
        this.doubleJumpManager = doubleJumpManager;
        this.spawnManager = spawnManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (configManager.isVoidTeleportEnabled()
                && player.getLocation().getY() < configManager.getVoidTeleportMinY()
                && spawnManager.isSpawnSet()) {
            player.setFallDistance(0f);
            spawnManager.teleportToSpawn(player);
            return;
        }

        if (player.isOnGround()) {
            doubleJumpManager.allowJump(player);
        } else if (!doubleJumpManager.canUseFeature(player)) {
            player.setAllowFlight(false);
        }
    }
}
