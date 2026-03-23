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

public class CoinsCommand implements BasicCommand {
    private final NLobby plugin;
    private final PlayerDAO playerDAO;
    private final PlayerCache playerCache;

    public CoinsCommand(NLobby plugin) {
        this.plugin = plugin;
        this.playerDAO = plugin.getPlayerDAO();
        this.playerCache = plugin.getPlayerCache();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length < 2) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.usage"));
            return;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.unknown-player"));
            return;
        }

        switch (action) {
            case "give" -> handleGive(stack, target, args);
            case "take" -> handleTake(stack, target, args);
            case "set" -> handleSet(stack, target, args);
            case "check" -> handleCheck(stack, target);
            default -> stack.getSender().sendMessage(LanguageLoader.getMessage("nlobby-command.unknown-command"));
        }
    }

    private void handleGive(CommandSourceStack stack, Player target, String[] args) {
        if (!stack.getSender().hasPermission("nlobby.coins.give")) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.usage"));
            return;
        }

        try {
            long amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("coins.amount-positive"));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    playerDAO.addCoins(target.getUniqueId(), amount);
                    LobbyPlayer lp = playerCache.get(target.getUniqueId());
                    if (lp != null) {
                        lp.setCoins(lp.getCoins() + amount);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String msg = LanguageLoader.getMessage("coins.given");
                        msg = msg.replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount));
                        stack.getSender().sendMessage(msg);
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error adding coins: " + e.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.invalid-amount"));
        }
    }

    private void handleTake(CommandSourceStack stack, Player target, String[] args) {
        if (!stack.getSender().hasPermission("nlobby.coins.take")) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.usage"));
            return;
        }

        try {
            long amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("coins.amount-positive"));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    playerDAO.addCoins(target.getUniqueId(), -amount);
                    LobbyPlayer lp = playerCache.get(target.getUniqueId());
                    if (lp != null) {
                        lp.setCoins(Math.max(0, lp.getCoins() - amount));
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String msg = LanguageLoader.getMessage("coins.removed");
                        msg = msg.replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount));
                        stack.getSender().sendMessage(msg);
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error taking coins: " + e.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.invalid-amount"));
        }
    }

    private void handleSet(CommandSourceStack stack, Player target, String[] args) {
        if (!stack.getSender().hasPermission("nlobby.coins.set")) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.usage"));
            return;
        }

        try {
            long amount = Long.parseLong(args[2]);
            if (amount < 0) {
                stack.getSender().sendMessage(LanguageLoader.getMessage("coins.amount-positive"));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    LobbyPlayer lp = playerCache.get(target.getUniqueId());
                    if (lp != null) {
                        lp.setCoins(amount);
                        playerDAO.savePlayer(lp);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String msg = LanguageLoader.getMessage("coins.set-message");
                        msg = msg.replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount));
                        stack.getSender().sendMessage(msg);
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error setting coins: " + e.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.invalid-amount"));
        }
    }

    private void handleCheck(CommandSourceStack stack, Player target) {
        LobbyPlayer lp = playerCache.get(target.getUniqueId());
        if (lp == null) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("coins.data-load-error"));
            return;
        }

        String msg = LanguageLoader.getMessage("coins.balance");
        msg = msg.replace("%coins%", String.valueOf(lp.getCoins()));
        msg = msg.replace("%player%", target.getName());
        stack.getSender().sendMessage(msg);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Arrays.asList("give", "take", "set", "check").stream()
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
        return true;
    }

    @Override
    public @Nullable String permission() {
        return null;
    }
}
