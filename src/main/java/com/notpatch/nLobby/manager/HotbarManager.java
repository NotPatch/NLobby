package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.util.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HotbarManager {
    private final ConfigManager configManager;
    private final Map<String, HotbarItem> items = new HashMap<>();

    public HotbarManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadItems();
    }

    private void loadItems() {
        ConfigurationSection hotbarSection = configManager.getConfig().getConfigurationSection("hotbar.items");
        if (hotbarSection == null) return;

        for (String key : hotbarSection.getKeys(false)) {
            ConfigurationSection itemSection = hotbarSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            int slot = itemSection.getInt("slot");
            String material = itemSection.getString("material", "COMPASS");
            String name = itemSection.getString("name", "&aItem");
            java.util.List<String> lore = itemSection.getStringList("lore");
            String action = itemSection.getString("action", "NONE");

            items.put(action, new HotbarItem(key, slot, material, name, lore, action));
        }
    }

    public void giveHotbarItems(Player player) {
        player.getInventory().clear();

        for (HotbarItem item : items.values()) {
            try {
                ItemStack itemStack = ItemBuilder.of(org.bukkit.Material.valueOf(item.getMaterial()))
                        .name(item.getName())
                        .lore(item.getLore())
                        .build();

                player.getInventory().setItem(item.getSlot(), itemStack);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public HotbarItem getItem(String action) {
        return items.get(action);
    }

    public HotbarItem getItemBySlot(int slot) {
        for (HotbarItem item : items.values()) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public static class HotbarItem {
        private final String key;
        private final int slot;
        private final String material;
        private final String name;
        private final java.util.List<String> lore;
        private final String action;

        public HotbarItem(String key, int slot, String material, String name, java.util.List<String> lore, String action) {
            this.key = key;
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.action = action;
        }

        public String getKey() { return key; }
        public int getSlot() { return slot; }
        public String getMaterial() { return material; }
        public String getName() { return name; }
        public java.util.List<String> getLore() { return lore; }
        public String getAction() { return action; }
    }
}
