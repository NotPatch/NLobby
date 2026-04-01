package com.notpatch.nLobby.command;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.config.ConfigManager;
import com.notpatch.nLobby.manager.QueueManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QueueCommand implements BasicCommand {
    private final QueueManager queueManager;
    private final ConfigManager configManager;

    public QueueCommand(QueueManager queueManager, ConfigManager configManager) {
        this.queueManager = queueManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("nlobby.admin")) {
            sender.sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            String usage = LanguageLoader.getMessage("queue.command.usage");
            sender.sendMessage(usage);
            return;
        }

        String playerName = args[0];
        String serverName = args[1].toLowerCase();

        List<String> validServers = configManager.getServers();
        if (!validServers.contains(serverName)) {
            String message = LanguageLoader.getMessage("queue.command.invalid-server");
            message = message.replace("%server%", serverName);
            sender.sendMessage(message);
            return;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            String message = LanguageLoader.getMessage("queue.command.player-not-found");
            message = message.replace("%player%", playerName);
            sender.sendMessage(message);
            return;
        }

        if (!player.isOnline()) {
            String message = LanguageLoader.getMessage("queue.command.player-offline");
            message = message.replace("%player%", playerName);
            sender.sendMessage(message);
            return;
        }

        boolean joined = queueManager.joinQueue(player, serverName);
        if (joined) {
            String message = LanguageLoader.getMessage("queue.command.success");
            message = message.replace("%player%", player.getName());
            message = message.replace("%server%", serverName);
            sender.sendMessage(message);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!stack.getSender().hasPermission("nlobby.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 0) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            return configManager.getServers().stream()
                    .filter(server -> server.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.hasPermission("nlobby.admin");
    }

    @Override
    public @Nullable String permission() {
        return "nlobby.admin";
    }
}


