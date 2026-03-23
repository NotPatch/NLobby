package com.notpatch.nLobby.command;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.manager.QueueManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ServerCommand implements BasicCommand {
    private final QueueManager queueManager;
    private final ConfigManager configManager;

    public ServerCommand(QueueManager queueManager, ConfigManager configManager) {
        this.queueManager = queueManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getExecutor() instanceof Player)) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.player-only"));
            return;
        }

        Player player = (Player) stack.getExecutor();

        if (args.length < 1) {
            String usage = LanguageLoader.getMessage("queue.command.usage");
            player.sendMessage(usage);
            return;
        }

        String serverName = args[0].toLowerCase();

        if (player.isOp()) {
            queueManager.connectPlayerDirect(player, serverName);
        } else {
            queueManager.joinQueue(player, serverName);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return configManager.getServers().stream()
                    .filter(server -> server.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
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
