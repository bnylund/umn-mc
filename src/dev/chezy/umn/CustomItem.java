package dev.chezy.umn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItem {
  public static ItemStack getNametag() {
    ItemStack is = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta im = is.getItemMeta();
    im.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Item Nametag " + ChatColor.RESET.toString()
        + ChatColor.DARK_GRAY.toString() + "(right click)");
    List<String> lore = new ArrayList<String>();
    lore.add("");
    lore.add(ChatColor.YELLOW + "After right clicking with this item in your hand, you");
    lore.add(ChatColor.YELLOW + "can rename any item in your inventory. To do so, hold");
    lore.add(ChatColor.YELLOW + "the item you want to rename in your main hand and type");
    lore.add(ChatColor.YELLOW + "in the name you'd like. Color codes are accepted (&).");
    lore.add("");
    im.setLore(lore);
    is.setItemMeta(im);
    return is;
  }

  public static ItemStack getXPBottle(int amount, String author) {
    ItemStack is = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
    ItemMeta im = is.getItemMeta();
    im.setDisplayName(ChatColor.GREEN + "Experience Bottle " + ChatColor.RESET.toString()
        + ChatColor.GRAY.toString() + "(Throw)");
    List<String> lore = new ArrayList<String>();
    lore.add(ChatColor.LIGHT_PURPLE + "Value " + ChatColor.WHITE + amount + " XP");
    lore.add(ChatColor.LIGHT_PURPLE + "Enchanter " + ChatColor.WHITE + author);
    im.setLore(lore);
    is.setItemMeta(im);
    return is;
  }
}
