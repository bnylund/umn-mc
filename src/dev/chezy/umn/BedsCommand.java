package dev.chezy.umn;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.List;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class BedsCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
    if (label.equalsIgnoreCase("beds") && cs instanceof Player) {
      final ClickableInventory bi = new ClickableInventory(3, "&c" + cs.getName() + "'s Beds");
      final SurvivalPlayer p = new SurvivalPlayer((Player)cs);
      if (p.getBeds().size() == 0) {
        p.sendMessage("&cYou don't have any beds!");
      }
      else {
        int i = 1;
        for (final Bed bed : p.getBeds()) {
          try {
            Integer cooldown = UMN.getCooldown(bed);
            Integer cooldownMins = cooldown / 60, cooldownSecs = cooldown % 60;
            final Material mat = cooldown > 0 ? Material.RED_STAINED_GLASS_PANE : bed.getLocation().getBlock().getType();
            final ItemStack is = new ItemStack(mat, 1);
            final ItemMeta im = is.getItemMeta();
            final List<String> lore = new ArrayList<String>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Location: " + bed.getLocation().getBlockX() + "x " + bed.getLocation().getBlockY() + "y " + bed.getLocation().getBlockZ() + "z");
            lore.add(ChatColor.GREEN + "World: " + bed.getLocation().getWorld().getName());
            lore.add("");

            if(cooldown > 0)
              lore.add(ChatColor.RED + "Cooldown active for " + (cooldownMins > 0 ? cooldownMins + "m " : "") + cooldownSecs + "s");
            else
              lore.add(ChatColor.GOLD + "Click to teleport!");
            im.setLore(lore);
            im.setDisplayName(ChatColor.AQUA + ChatColor.BOLD.toString() + "Bed #" + i);
            is.setItemMeta(im);
            final int bedNum = i;
            final Click click = new Click(is) {
              @Override
              public void run(final InventoryClickEvent e) {
                Integer rcooldown = UMN.getCooldown(bed);
                Integer cooldownMins = rcooldown / 60, cooldownSecs = rcooldown % 60;
                if(cooldown > 0) {
                  e.getWhoClicked().sendMessage(ChatColor.RED + "Bed #" + bedNum + " is on cooldown for " + (cooldownMins > 0 ? cooldownMins + " minute" + (cooldownMins == 1 ? "" : "s") + " and " : "") + cooldownSecs + " second" + (cooldownSecs == 1 ? "" : "s") + "!");
                } else {
                  e.getWhoClicked().closeInventory();
                  ((Player)e.getWhoClicked()).teleport(bed.getLocation());
                  e.getWhoClicked().sendMessage(ChatColor.AQUA + "Teleporting to bed...");
                  UMN.setCooldown(bed, 600);
                }
              }
            };
            bi.addItemWithClick(0, is, click);
            ++i;
          } catch(Exception ex) { 
            cs.sendMessage(ChatColor.RED + "Failed to load bed at " + bed.getLocation().getBlockX() + "x " + bed.getLocation().getBlockY() + "y " + bed.getLocation().getBlockZ() + "z!");
          }
        }
        ((Player)cs).openInventory((Inventory)bi);
      }
    }
    return true;
  }
}