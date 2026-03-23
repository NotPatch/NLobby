package com.notpatch.nLobby.command;

import com.notpatch.nLobby.LanguageLoader;
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
import java.util.stream.Collectors;

public class QueueCommand implements BasicCommand {
    private final QueueManager queueManager;

    public QueueCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("nlobby.admin")) {
            sender.sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§c/queue <oyuncu> <sunucu>");
            return;
        }

        String playerName = args[0];
        String serverName = args[1].toLowerCase();

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("§c" + playerName + " adlı oyuncu bulunamadı.");
            return;
        }

        if (!player.isOnline()) {
            sender.sendMessage("§c" + playerName + " adlı oyuncu çevrimdışı.");
            return;
        }

        boolean joined = queueManager.joinQueue(player, serverName);
        if (joined) {
            sender.sendMessage("§a" + player.getName() + " başarıyla " + serverName + " kuyruğuna eklendi.");
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!stack.getSender().hasPermission("nlobby.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            return java.util.Arrays.asList("survival", "skywars", "creative", "pvp").stream()
                    .filter(server -> server.startsWith(input))
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

