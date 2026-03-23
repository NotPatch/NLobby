package com.notpatch.nLobby.velocity;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Getter
public class VelocityQueueConfig {
    private boolean enabled;
    private int maxSlots;
    private int slotsPerTick;
    private int tickInterval;
    private int allowedTtlMinutes;
    private String kickMessage;

    public VelocityQueueConfig(Path configPath) {
        loadConfig(configPath);
    }

    private void loadConfig(Path configPath) {
        try {
            if (!Files.exists(configPath)) {
                // Use defaults if file doesn't exist
                loadDefaults();
                return;
            }

            Yaml yaml = new Yaml();
            try (InputStream input = Files.newInputStream(configPath)) {
                Map<String, Object> data = yaml.load(input);
                if (data == null) {
                    loadDefaults();
                    return;
                }

                Map<String, Object> queueConfig = (Map<String, Object>) data.get("queue");
                if (queueConfig == null) {
                    loadDefaults();
                    return;
                }

                this.enabled = getBoolean(queueConfig, "enabled", true);
                this.maxSlots = getInt(queueConfig, "max-slots", 5);
                this.slotsPerTick = getInt(queueConfig, "slots-per-tick", 2);
                this.tickInterval = getInt(queueConfig, "tick-interval", 5);
                this.allowedTtlMinutes = getInt(queueConfig, "allowed-ttl-minutes", 10);
                this.kickMessage = getString(queueConfig, "kick-message",
                        "&cSunucu dolu!\n&6Sıranız: &f%position% / %total%\n&7Birkaç saniye sonra tekrar bağlanın.");
            }
        } catch (Exception e) {
            loadDefaults();
        }
    }

    private void loadDefaults() {
        this.enabled = true;
        this.maxSlots = 5;
        this.slotsPerTick = 2;
        this.tickInterval = 5;
        this.allowedTtlMinutes = 10;
        this.kickMessage = "&cSunucu dolu!\n&6Sıranız: &f%position% / %total%\n&7Birkaç saniye sonra tekrar bağlanın.";
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    private static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
}
