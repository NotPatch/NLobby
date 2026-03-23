package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashSet;
import java.util.Set;

public class ProtectionListener implements Listener {
    private final ConfigManager configManager;

    public ProtectionListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player && configManager.isAntiDamageEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (configManager.isAntiHungerEnabled()) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (configManager.isAntiBuildEnabled() && !event.getPlayer().hasPermission("nlobby.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LanguageLoader.getMessage("protection.no-build"));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (configManager.isAntiBuildEnabled() && !event.getPlayer().hasPermission("nlobby.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LanguageLoader.getMessage("protection.no-build"));
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player && configManager.isAntiItemPickupEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (configManager.isWeatherLocked() && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Set<Integer> protectedSlots = getProtectedHotbarSlots();
        if (protectedSlots.isEmpty()) {
            return;
        }

        if (event.getClickedInventory() != null
                && event.getClickedInventory().equals(event.getWhoClicked().getInventory())
                && protectedSlots.contains(event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        if ((event.getClick() == ClickType.NUMBER_KEY && protectedSlots.contains(event.getHotbarButton()))
                || (event.getAction() == InventoryAction.HOTBAR_SWAP && protectedSlots.contains(event.getHotbarButton()))
                || (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD && protectedSlots.contains(event.getHotbarButton()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Set<Integer> protectedSlots = getProtectedHotbarSlots();
        if (protectedSlots.isEmpty()) {
            return;
        }

        for (int inventorySlot : event.getInventorySlots()) {
            if (protectedSlots.contains(inventorySlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (getProtectedHotbarSlots().contains(event.getPlayer().getInventory().getHeldItemSlot())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (getProtectedHotbarSlots().contains(event.getPlayer().getInventory().getHeldItemSlot())) {
            event.setCancelled(true);
        }
    }

    private Set<Integer> getProtectedHotbarSlots() {
        Set<Integer> slots = new HashSet<>();
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("hotbar.items");
        if (section == null) {
            return slots;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }
            int slot = itemSection.getInt("slot", -1);
            if (slot >= 0 && slot <= 8) {
                slots.add(slot);
            }
        }
        return slots;
    }
}
