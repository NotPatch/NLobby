package com.notpatch.nLobby.manager;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.cache.PlayerCache;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.model.LobbyPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatManager {
    private final ConfigManager configManager;
    private final PlayerCache playerCache;

    public ChatManager(ConfigManager configManager, PlayerCache playerCache) {
        this.configManager = configManager;
        this.playerCache = playerCache;
    }

    public String formatMessage(Player player, String message) {
        if (!configManager.isChatEnabled()) {
            return message;
        }

        String format = configManager.getChatFormat();
        LobbyPlayer lp = playerCache.get(player.getUniqueId());

        if (lp != null) {
            format = format.replace("%level%", String.valueOf(lp.getLevel()));
            format = format.replace("%coins%", String.valueOf(lp.getCoins()));
        }

        format = format.replace("%player%", player.getName());
        format = format.replace("%message%", message);

        return colorize(format);
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
