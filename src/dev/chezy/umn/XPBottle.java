package dev.chezy.umn;

import java.text.DecimalFormat;

import com.earth2me.essentials.craftbukkit.SetExpFix;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;

public class XPBottle implements CommandExecutor, Listener {
  @EventHandler
  public void onXPSplash(ExpBottleEvent e) {
    // Check for player
    if (!(e.getEntity().getShooter() instanceof Player) || !e.getEntity().getItem().hasItemMeta()) {
      return;
    }

    // Check for name
    ItemStack xpTest = CustomItem.getXPBottle(1, "test");
    if (!e.getEntity().getItem().getItemMeta().hasDisplayName()
        || !e.getEntity().getItem().getItemMeta().getDisplayName().equals(xpTest.getItemMeta().getDisplayName())) {
      return;
    }

    // Check for lore
    if (!e.getEntity().getItem().getItemMeta().hasLore()
        || e.getEntity().getItem().getItemMeta().getLore().size() != xpTest.getItemMeta().getLore().size()) {
      return;
    }

    Player p = (Player) e.getEntity().getShooter();

    // Parse lore
    String line = ChatColor.stripColor(e.getEntity().getItem().getItemMeta().getLore().get(0));
    if (line.contains("Value ") && line.contains("XP")) {
      int amount = -1;
      try {
        String xpString = line.substring(6, line.indexOf("XP") - 1);
        amount = Integer.parseInt(xpString);
      } catch (Exception ex) {
        return;
      }

      if (amount < 0) {
        return;
      }

      DecimalFormat df = new DecimalFormat("##,###");
      SetExpFix.setTotalExperience(p, SetExpFix.getTotalExperience(p) + amount);
      p.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "+ " + df.format(amount) + "XP");

      e.setShowEffect(false);
      e.setExperience(0);
      e.setCancelled(true);
    }
  }

  @Override
  public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
    DecimalFormat df = new DecimalFormat("##,###");
    if (label.equalsIgnoreCase("xpbottle") && cs instanceof Player) {
      Player p = (Player) cs;
      int xp = SetExpFix.getTotalExperience(p);

      if (args.length == 0) {
        p.sendMessage(
            ChatColor.RED + "Usage: /xpbottle <amount>");
        return true;
      }

      if (UMN.xpCooldown.containsKey(p.getUniqueId().toString())) {
        Integer cooldown = UMN.xpCooldown.get(p.getUniqueId().toString());
        Integer cooldownMins = cooldown / 60, cooldownSecs = cooldown % 60;
        p.sendMessage(ChatColor.RED + "You cannot extract XP for "
            + (cooldownMins > 0 ? cooldownMins + " minute" + (cooldownMins == 1 ? "" : "s") + " and " : "")
            + cooldownSecs + " second" + (cooldownSecs == 1 ? "" : "s") + ".");
        return true;
      }

      int amount = -1;
      try {
        amount = Integer.parseInt(args[0]);
      } catch (Exception ex) {
        p.sendMessage(ChatColor.RED + "That isn't a valid number.");
        return true;
      }
      if (amount > 0) {
        if (xp >= amount) {
          if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(ChatColor.RED + "Your inventory is full!");
            return true;
          }
          ItemStack bottle = CustomItem.getXPBottle(amount, p.getDisplayName());
          SetExpFix.setTotalExperience(p, xp - amount);
          p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "- " + df.format(amount) + "XP");
          p.getInventory().addItem(bottle);

          UMN.xpCooldown.put(p.getUniqueId().toString(), 600); // 10min cooldown
        } else {
          p.sendMessage(ChatColor.RED + "You don't have that much XP! You have " + ChatColor.GOLD.toString() + xp
              + ChatColor.RED.toString() + " exp.");
        }
      } else {
        p.sendMessage(ChatColor.RED + "XP amount can't be negative.");
      }
    } else if(label.equalsIgnoreCase("xp") && cs instanceof Player) {
      Player p = (Player) cs;
      p.sendMessage(ChatColor.GOLD + "You have " + ChatColor.RED.toString() + df.format(SetExpFix.getTotalExperience(p)) + ChatColor.GOLD + " exp (level " + ChatColor.RED.toString() + p.getLevel() + ChatColor.GOLD.toString() + ")");
    }
    return true;
  }
}
