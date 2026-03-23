package com.notpatch.nLobby.gui;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class GUIItem {
    private final ItemStack itemStack;
    private final Consumer<Player> action;

    public GUIItem(ItemStack itemStack, Consumer<Player> action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    public GUIItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.action = null;
    }
}
