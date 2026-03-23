package com.notpatch.nLobby.listener;

import com.notpatch.nLobby.manager.ChatManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final ChatManager chatManager;

    public ChatListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("nlobby.chat")) {
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();
        String formatted = chatManager.formatMessage(event.getPlayer(), message);
        event.setMessage(formatted);
    }
}
