package com.notpatch.nLobby.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class QueueEntry {
    private final UUID uuid;
    private final String serverName;
    private final long joinTime;

    public QueueEntry(UUID uuid, String serverName) {
        this.uuid = uuid;
        this.serverName = serverName;
        this.joinTime = System.currentTimeMillis();
    }
}
