package com.notpatch.nLobby.command;

import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.cache.PlayerCache;
import com.notpatch.nLobby.database.PlayerDAO;
import com.notpatch.nLobby.model.LobbyPlayer;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LevelCommand implements BasicCommand {
    private final NLobby plugin;
    private final PlayerDAO playerDAO;
    private final PlayerCache playerCache;

    public LevelCommand(NLobby plugin) {
        this.plugin = plugin;
        this.playerDAO = plugin.getPlayerDAO();
        this.playerCache = plugin.getPlayerCache();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length < 1) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("level.usage"));
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "set":
                handleSet(stack, args);
                break;
            case "check":
                handleCheck(stack, args);
                break;
            default:
                stack.getSender().sendMessage(LanguageLoader.getMessage("nlobby-command.unknown-command"));
                break;
        }
    }

    private void handleSet(CommandSourceStack stack, String[] args) {
        if (!stack.getSender().hasPermission("nlobby.level.set")) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("level.usage"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.unknown-player"));
            return;
        }

        try {
            int level = Integer.parseInt(args[2]);
            if (level < 1) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("level.level-positive"));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    playerDAO.setLevel(target.getUniqueId(), level);
                    LobbyPlayer lp = playerCache.get(target.getUniqueId());
                    if (lp != null) {
                        lp.setLevel(level);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String msg = LanguageLoader.getMessage("level.set");
                        msg = msg.replace("%player%", target.getName()).replace("%level%", String.valueOf(level));
                        stack.getSender().sendMessage(msg);
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error setting level: " + e.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("level.invalid-level"));
        }
    }

    private void handleCheck(CommandSourceStack stack, String[] args) {
        Player target;

        if (args.length < 2) {
            if (!(stack.getExecutor() instanceof Player)) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("general.player-only"));
                return;
            }
            target = (Player) stack.getExecutor();
        } else {
            String playerName = args[1];
            target = Bukkit.getPlayer(playerName);

            if (target == null) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("general.unknown-player"));
                return;
            }
        }

        LobbyPlayer lp = playerCache.get(target.getUniqueId());
        if (lp == null) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("level.data-load-error"));
            return;
        }

        String msg = LanguageLoader.getMessage("level.current");
        msg = msg.replace("%level%", String.valueOf(lp.getLevel()));
        msg = msg.replace("%player%", target.getName());
        stack.getSender().sendMessage(msg);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 0) {
            return Arrays.asList("set", "check");
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Arrays.asList("set", "check").stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.hasPermission("nlobby.level");
    }

    @Override
    public @Nullable String permission() {
        return "nlobby.level";
    }
}
