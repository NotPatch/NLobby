package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.LanguageLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VisibilityManager {
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public void toggle(Player player) {
        if (hiddenPlayers.contains(player.getUniqueId())) {
            show(player);
        } else {
            hide(player);
        }
    }

    public void hide(Player player) {
        hiddenPlayers.add(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.hidePlayer(null, other);
            }
        }
        player.sendMessage(LanguageLoader.getMessage("visibility.hidden"));
    }

    public void show(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.showPlayer(null, other);
            }
        }
        player.sendMessage(LanguageLoader.getMessage("visibility.shown"));
    }

    public void onPlayerJoin(Player newPlayer) {
        for (UUID uuid : hiddenPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.hidePlayer(null, newPlayer);
            }
        }
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUniqueId());
    }
}
