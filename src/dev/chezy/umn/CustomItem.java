package dev.chezy.umn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItem {
	public static ItemStack getNametag() {
		ItemStack is = new ItemStack(Material.NAME_TAG, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Item Nametag " + ChatColor.RESET.toString() + ChatColor.DARK_GRAY.toString() + "(right click)");
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
}