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

public class DirectCommand implements BasicCommand {
    private final QueueManager queueManager;
    private final ConfigManager configManager;

    public DirectCommand(QueueManager queueManager, ConfigManager configManager) {
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
            String usage = LanguageLoader.getMessage("direct.command.usage");
            sender.sendMessage(usage);
            return;
        }

        String playerName = args[0];
        String serverName = args[1].toLowerCase();

        List<String> validServers = configManager.getServers();
        if (!validServers.contains(serverName)) {
            String message = LanguageLoader.getMessage("direct.command.invalid-server");
            message = message.replace("%server%", serverName);
            sender.sendMessage(message);
            return;
        }

        if ("*".equals(playerName)) {
            directAllPlayers(sender, serverName);
        } else {
            directPlayer(sender, playerName, serverName);
        }
    }

    private void directPlayer(@NotNull CommandSender sender, String playerName, String serverName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            String message = LanguageLoader.getMessage("direct.command.player-not-found");
            message = message.replace("%player%", playerName);
            sender.sendMessage(message);
            return;
        }

        if (!player.isOnline()) {
            String message = LanguageLoader.getMessage("direct.command.player-offline");
            message = message.replace("%player%", playerName);
            sender.sendMessage(message);
            return;
        }

        queueManager.connectPlayerDirect(player, serverName);
        String message = LanguageLoader.getMessage("direct.command.success");
        message = message.replace("%player%", player.getName());
        message = message.replace("%server%", serverName);
        sender.sendMessage(message);
    }

    private void directAllPlayers(@NotNull CommandSender sender, String serverName) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        int count = 0;
        for (Player player : players) {
            queueManager.connectPlayerDirect(player, serverName);
            count++;
        }

        String message = LanguageLoader.getMessage("direct.command.bulk-success");
        message = message.replace("%count%", String.valueOf(count));
        message = message.replace("%server%", serverName);
        sender.sendMessage(message);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!stack.getSender().hasPermission("nlobby.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());

            if ("*".startsWith(input)) {
                suggestions.add("*");
            }
            return suggestions;
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

