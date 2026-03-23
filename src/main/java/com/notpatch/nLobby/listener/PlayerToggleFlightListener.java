package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.manager.DoubleJumpManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerToggleFlightListener implements Listener {
    private final DoubleJumpManager doubleJumpManager;

    public PlayerToggleFlightListener(DoubleJumpManager doubleJumpManager) {
        this.doubleJumpManager = doubleJumpManager;
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!doubleJumpManager.canDoubleJump(player)) {
            return;
        }

        event.setCancelled(true);
        player.setFlying(false);
        doubleJumpManager.executeDoubleJump(player);
    }
}

