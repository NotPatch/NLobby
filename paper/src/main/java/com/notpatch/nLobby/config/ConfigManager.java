package com.notpatch.nLobby.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.notpatch.nLobby.util.LocationSerializer;

import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getDatabaseType() {
        return getConfig().getString("database.type", "SQLITE");
    }

    public String getDatabaseHost() {
        return getConfig().getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return getConfig().getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return getConfig().getString("database.name", "nlobby");
    }

    public String getDatabaseUsername() {
        return getConfig().getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return getConfig().getString("database.password", "");
    }

    public int getDatabasePoolSize() {
        return getConfig().getInt("database.pool-size", 5);
    }

    public Location getSpawnLocation() {
        String locStr = getConfig().getString("spawn.location", "world,0.5,64,0.5,0,0");
        return LocationSerializer.deserialize(locStr);
    }

    public void setSpawnLocation(Location loc) {
        String locStr = LocationSerializer.serialize(loc);
        getConfig().set("spawn.location", locStr);
        plugin.saveConfig();
    }

    public boolean teleportOnJoin() {
        return getConfig().getBoolean("spawn.teleport-on-join", true);
    }

    public boolean teleportOnDeath() {
        return getConfig().getBoolean("spawn.teleport-on-death", true);
    }

    public boolean isAntiDamageEnabled() {
        return getConfig().getBoolean("protection.anti-damage", true);
    }

    public boolean isAntiHungerEnabled() {
        return getConfig().getBoolean("protection.anti-hunger", true);
    }

    public boolean isAntiBuildEnabled() {
        return getConfig().getBoolean("protection.anti-build", true);
    }

    public boolean isAntiItemPickupEnabled() {
        return getConfig().getBoolean("protection.anti-item-pickup", true);
    }

    public boolean isWeatherLocked() {
        return getConfig().getBoolean("protection.lock-weather", true);
    }

    public boolean isTimeLocked() {
        return getConfig().getBoolean("protection.lock-time", true);
    }

    public int getLockedTime() {
        return getConfig().getInt("protection.locked-time", 6000);
    }

    public boolean isDoubleJumpEnabled() {
        return getConfig().getBoolean("double-jump.enabled", true);
    }

    public double getDoubleJumpMultiplier() {
        return getConfig().getDouble("double-jump.velocity-multiplier", 1.5);
    }

    public boolean isScoreboardEnabled() {
        return getConfig().getBoolean("scoreboard.enabled", true);
    }

    public String getScoreboardTitle() {
        return getConfig().getString("scoreboard.title", "&6&lNLobby");
    }

    public int getScoreboardUpdateInterval() {
        return getConfig().getInt("scoreboard.update-interval", 20);
    }

    public boolean isTabListEnabled() {
        return getConfig().getBoolean("tab-list.enabled", true);
    }

    public boolean isChatEnabled() {
        return getConfig().getBoolean("chat.enabled", true);
    }

    public String getChatFormat() {
        return getConfig().getString("chat.format", "&7[&f%level%&7] &f%player% &8» &f%message%");
    }

    public boolean isJoinWelcomeEnabled() {
        return getConfig().getBoolean("join-message.welcome.enabled", true);
    }

    public boolean isJoinBroadcastEnabled() {
        return getConfig().getBoolean("join-message.broadcast.enabled", true);
    }


    public boolean isBossBarEnabled() {
        return getConfig().getBoolean("bossbar.enabled", true);
    }

    public String getBossBarTitle() {
        return getConfig().getString("bossbar.title", "&6NLobby &7| &fOyuncular: &a%online%");
    }

    public int getQueueTickInterval() {
        return getConfig().getInt("queue.tick-interval", 3);
    }

    public boolean isQueueShowPositionActionBar() {
        return getConfig().getBoolean("queue.show-position-actionbar", true);
    }

    public String getQueueActionBarText() {
        return getConfig().getString("queue.actionbar-text", "&6Sıra: &f%position% &7/ &f%total% &8| &7Sunucu: &f%server%");
    }

    public int getQueueConnectTimeoutSeconds() {
        return getConfig().getInt("queue.connect-timeout-seconds", 4);
    }

    public boolean isVoidTeleportEnabled() {
        return getConfig().getBoolean("void-teleport.enabled", true);
    }

    public double getVoidTeleportMinY() {
        return getConfig().getDouble("void-teleport.min-y", 0.0D);
    }


    public List<String> getServers() {
        return getConfig().getStringList("queue.servers");
    }
}
