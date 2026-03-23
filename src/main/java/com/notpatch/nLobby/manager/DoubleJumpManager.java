package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.config.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DoubleJumpManager {
    private final ConfigManager configManager;
    private final Set<UUID> canJump = new HashSet<>();

    public DoubleJumpManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean canUseFeature(Player player) {
        if (!configManager.isDoubleJumpEnabled()) {
            return false;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        return !configManager.getConfig().getBoolean("double-jump.permission-required", false)
                || player.hasPermission("nlobby.doublejump");
    }

    public void allowJump(Player player) {
        if (canUseFeature(player)) {
            canJump.add(player.getUniqueId());
            player.setAllowFlight(true);
        }
    }

    public void resetJump(Player player) {
        canJump.remove(player.getUniqueId());
    }

    public boolean canDoubleJump(Player player) {
        return canJump.contains(player.getUniqueId()) && canUseFeature(player);
    }

    public void executeDoubleJump(Player player) {
        if (!canDoubleJump(player)) return;

        double multiplier = configManager.getDoubleJumpMultiplier();
        Vector velocity = player.getVelocity();
        velocity.setY(multiplier);
        player.setVelocity(velocity);
        player.setAllowFlight(false);

        String particleName = configManager.getConfig().getString("double-jump.particle", "CLOUD");
        try {
            Particle particle = Particle.valueOf(particleName);
            player.getWorld().spawnParticle(particle, player.getLocation(), 10);
        } catch (IllegalArgumentException e) {
        }

        String soundName = configManager.getConfig().getString("double-jump.sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException e) {
        }

        resetJump(player);
    }

    public void onPlayerQuit(Player player) {
        canJump.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.setAllowFlight(false);
        }
    }
}
