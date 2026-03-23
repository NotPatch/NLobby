package com.notpatch.nLobby.command;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.manager.SpawnManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class SpawnCommand implements BasicCommand {
    private final SpawnManager spawnManager;

    public SpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getExecutor() instanceof Player)) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.player-only"));
            return;
        }

        Player player = (Player) stack.getExecutor();

        if (!spawnManager.isSpawnSet()) {
            player.sendMessage(LanguageLoader.getMessage("spawn.not-set"));
            return;
        }

        spawnManager.teleportToSpawn(player);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return true;
    }

    @Override
    public @Nullable String permission() {
        return null;
    }
}
