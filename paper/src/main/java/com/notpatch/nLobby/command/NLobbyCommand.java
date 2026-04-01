package com.notpatch.nLobby.command;

import com.notpatch.nLobby.NLobby;
import com.notpatch.nLobby.LanguageLoader;
import com.notpatch.nLobby.manager.SpawnManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class NLobbyCommand implements BasicCommand {
    private final NLobby plugin;
    private final SpawnManager spawnManager;

    public NLobbyCommand(NLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 0) {
            stack.getSender().sendMessage(LanguageLoader.getMessage("nlobby-command.usage"));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                if (!stack.getSender().hasPermission("nlobby.admin")) {
                    stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
                    return;
                }
                plugin.getConfigManager().reload();
                stack.getSender().sendMessage(LanguageLoader.getMessage("general.plugin-reloaded"));
                break;

            case "setspawn":
                if (!stack.getSender().hasPermission("nlobby.admin")) {
                    stack.getSender().sendMessage(LanguageLoader.getMessage("general.no-permission"));
                    return;
                }
                if (!(stack.getExecutor() instanceof Player)) {
                    stack.getSender().sendMessage(LanguageLoader.getMessage("general.player-only"));
                    return;
                }
                Player player = (Player) stack.getExecutor();
                spawnManager.setSpawn(player.getLocation());
                stack.getSender().sendMessage(LanguageLoader.getMessage("spawn.set"));
                break;

            case "version":
                String versionMsg = LanguageLoader.getMessage("nlobby-command.version");
                stack.getSender().sendMessage(versionMsg.replace("%version%", plugin.getDescription().getVersion()));
                break;

            default:
                stack.getSender().sendMessage(LanguageLoader.getMessage("nlobby-command.unknown-command"));
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 0){
            return Arrays.asList("reload", "setspawn", "version");
        }
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Arrays.asList("reload", "setspawn", "version").stream()
                    .filter(cmd -> cmd.startsWith(input))
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
