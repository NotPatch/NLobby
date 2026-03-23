package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.config.ConfigManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class SpawnManager {
    private final ConfigManager configManager;
    private Location spawnLocation;

    public SpawnManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.spawnLocation = configManager.getSpawnLocation();
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location;
        configManager.setSpawnLocation(location);
    }

    public void teleportToSpawn(Player player) {
        if (spawnLocation == null) {
            player.sendMessage(LanguageLoader.getMessage("spawn.not-set"));
            return;
        }
        player.teleport(spawnLocation);
        player.sendMessage(LanguageLoader.getMessage("spawn.teleported"));
    }

    public Location getSpawn() {
        return spawnLocation;
    }

    public boolean isSpawnSet() {
        return spawnLocation != null;
    }
}
