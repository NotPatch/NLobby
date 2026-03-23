package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.gui.NavigatorGUI;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.manager.HotbarManager;
import com.notpatch.nLobby.manager.VisibilityManager;
import com.notpatch.nLobby.manager.QueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    private final ConfigManager configManager;
    private final HotbarManager hotbarManager;
    private final VisibilityManager visibilityManager;
    private final QueueManager queueManager;
    private final Map<UUID, Long> jetCooldowns = new HashMap<>();

    public PlayerInteractListener(ConfigManager configManager, HotbarManager hotbarManager,
                                   VisibilityManager visibilityManager, QueueManager queueManager) {
        this.configManager = configManager;
        this.hotbarManager = hotbarManager;
        this.visibilityManager = visibilityManager;
        this.queueManager = queueManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isLeftClick() && !event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        int slot = player.getInventory().getHeldItemSlot();
        HotbarManager.HotbarItem hotbarItem = hotbarManager.getItemBySlot(slot);
        if (hotbarItem == null) return;

        Action action = event.getAction();
        boolean isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean handled = handleHotbarAction(player, hotbarItem, isRightClick);
        if (handled) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    private boolean handleHotbarAction(Player player, HotbarManager.HotbarItem hotbarItem, boolean isRightClick) {
        String action = hotbarItem.getAction();
        if (action == null) {
            return false;
        }

        switch (action.toUpperCase()) {
            case "OPEN_NAVIGATOR":
                if (isRightClick) {
                    openNavigator(player);
                    return true;
                }
                break;
            case "TOGGLE_VISIBILITY":
                if (isRightClick) {
                    visibilityManager.toggle(player);
                    return true;
                }
                break;
            case "JET_BOOST":
                applyJetBoost(player, isRightClick);
                return true;
            default:
                break;
        }

        return false;
    }

    private void openNavigator(Player player) {
        NavigatorGUI gui = new NavigatorGUI(configManager, queueManager);
        gui.open(player);
    }

    private void applyJetBoost(Player player, boolean isRightClick) {
        if (!configManager.getConfig().getBoolean("jet.enabled", true)) {
            return;
        }

        if (configManager.getConfig().getBoolean("jet.permission-required", false)) {
            String permission = configManager.getConfig().getString("jet.permission", "nlobby.jet");
            if (!player.hasPermission(permission)) {
                player.sendMessage(LanguageLoader.getMessage("general.no-permission"));
                return;
            }
        }

        long cooldownMs = configManager.getConfig().getLong("jet.cooldown-ms", 1250L);
        long now = System.currentTimeMillis();
        Long nextUse = jetCooldowns.get(player.getUniqueId());
        if (nextUse != null && nextUse > now) {
            long left = (nextUse - now + 999) / 1000;
            String message = LanguageLoader.getMessage("jet.cooldown")
                    .replace("%seconds%", String.valueOf(left));
            player.sendMessage(message);
            return;
        }

        if (isRightClick) {
            double upward = configManager.getConfig().getDouble("jet.upward-power", 1.2);
            Vector velocity = player.getVelocity();
            velocity.setY(upward);
            player.setVelocity(velocity);
        } else {
            double forward = configManager.getConfig().getDouble("jet.forward-power", 1.6);
            Vector direction = player.getLocation().getDirection().normalize().multiply(forward);
            direction.setY(Math.max(direction.getY(), 0.2));
            player.setVelocity(direction);
        }

        player.setFallDistance(0f);
        jetCooldowns.put(player.getUniqueId(), now + cooldownMs);
    }
}
