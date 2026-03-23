package com.notpatch.nLobby.velocity;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class VelocityQueueConfig {
    private boolean enabled;
    private List<String> targetServers;
    private String queueServer;
    private int maxSlots;
    private int slotsPerTick;
    private int tickInterval;

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
                this.queueServer = getString(queueConfig, "queue-server", "nlobby-limbo");
                this.targetServers = getList(queueConfig, "target-servers", new ArrayList<>());
                this.maxSlots = getInt(queueConfig, "max-slots", 5);
                this.slotsPerTick = getInt(queueConfig, "slots-per-tick", 2);
                this.tickInterval = getInt(queueConfig, "tick-interval", 5);
            }
        } catch (Exception e) {
            loadDefaults();
        }
    }

    private void loadDefaults() {
        this.enabled = true;
        this.queueServer = "nlobby-limbo";
        this.targetServers = new ArrayList<>();
        this.maxSlots = 5;
        this.slotsPerTick = 2;
        this.tickInterval = 5;
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

    @SuppressWarnings("unchecked")
    private static List<String> getList(Map<String, Object> map, String key, List<String> defaultValue) {
        Object value = map.get(key);
        if (value instanceof List) {
            try {
                return (List<String>) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
