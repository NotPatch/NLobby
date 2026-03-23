package com.notpatch.nLobby.gui;

import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.manager.QueueManager;
import com.notpatch.nLobby.util.ItemBuilder;
import com.notpatch.nlib.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;

public class NavigatorGUI extends FastInv {
    private final ConfigManager configManager;
    private final QueueManager queueManager;

    public NavigatorGUI(ConfigManager configManager, QueueManager queueManager) {
        super(configManager.getConfig().getInt("navigator.gui-size", 27),
                colorize(configManager.getConfig().getString("navigator.gui-title", "&8Navigator")));
        this.configManager = configManager;
        this.queueManager = queueManager;
        build();
    }

    public void build() {
        List<Map<?, ?>> configuredServers = configManager.getConfig().getMapList("navigator.servers");
        if (!configuredServers.isEmpty()) {
            for (Map<?, ?> serverMap : configuredServers) {
                addServerButton(serverMap);
            }
            return;
        }

        ConfigurationSection navSection = configManager.getConfig().getConfigurationSection("navigator.servers");
        if (navSection == null) {
            return;
        }

        for (String key : navSection.getKeys(false)) {
            ConfigurationSection serverSection = navSection.getConfigurationSection(key);
            if (serverSection == null) continue;

            addServerButton(Map.of(
                    "name", serverSection.getString("name", "&aServer"),
                    "material", serverSection.getString("material", "COMPASS"),
                    "lore", serverSection.getStringList("lore"),
                    "slot", serverSection.getInt("slot"),
                    "server", serverSection.getString("server", "lobby")
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private void addServerButton(Map<?, ?> serverMap) {
        String name = readString(serverMap, "name", "&aServer");
        String materialName = readString(serverMap, "material", "COMPASS");
        String server = readString(serverMap, "server", "lobby");
        List<String> lore = serverMap.get("lore") instanceof List<?> loreList
                ? (List<String>) loreList
                : List.of();

        int slot = 0;
        Object slotObj = serverMap.get("slot");
        if (slotObj instanceof Number number) {
            slot = number.intValue();
        } else if (slotObj instanceof String text) {
            try {
                slot = Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return;
            }
        }

        try {
            var itemStack = ItemBuilder.of(Material.valueOf(materialName.toUpperCase()))
                    .name(name)
                    .lore(lore)
                    .build();
            setItem(slot, itemStack, event -> {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
                if (player.isOp()) {
                    queueManager.connectPlayerDirect(player, server);
                } else {
                    queueManager.joinQueue(player, server);
                }
            });
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String readString(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static String colorize(String text) {
        return text.replace('&', '§');
    }
}
