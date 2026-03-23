package com.notpatch.nLobby.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LobbyPlayer {
    private UUID uuid;
    private String name;
    private long coins;
    private int level;
    private long playtime;
    private long firstJoin;
    private long lastJoin;

    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
