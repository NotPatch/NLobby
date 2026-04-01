package com.notpatch.nLobby.cache;

import com.notpatch.nLobby.model.LobbyPlayer;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PlayerCache {
    private final Map<UUID, LobbyPlayer> cache = new ConcurrentHashMap<>();

    public void put(UUID uuid, LobbyPlayer lobbyPlayer) {
        cache.put(uuid, lobbyPlayer);
    }

    public LobbyPlayer get(UUID uuid) {
        return cache.get(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public Collection<LobbyPlayer> getAll() {
        return cache.values();
    }

    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void clear() {
        cache.clear();
    }
}
