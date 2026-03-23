package com.notpatch.nLobby.util;

import com.notpatch.nlib.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material, 1);
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public ItemBuilder name(String displayName) {
        if (meta != null) {
            meta.setDisplayName(colorize(displayName));
        }
        return this;
    }

    public ItemBuilder lore(String... loreLines) {
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(colorize(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> loreLines) {
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(colorize(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder glow() {
        if (meta != null) {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder addFlag(ItemFlag flag) {
        if (meta != null) {
            meta.addItemFlags(flag);
        }
        return this;
    }

    public ItemBuilder hideAttributes() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String colorize(String text) {
        if (text == null) return "";
        return ColorUtil.hexColor(text);
    }
}
