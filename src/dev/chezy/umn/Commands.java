package dev.chezy.umn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.james090500.APIManager.UserInfo;

import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class Commands implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
    if (label.equalsIgnoreCase("verify")) {
      if (cs.isOp()) {
        if (args.length == 0) {
          cs.sendMessage("Please specify a player.");
          return true;
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(UserInfo.getParsedUUID(args[0])));
        if (p != null && p.getUniqueId() != null) {
          List<String> verified = UMN.pl.getConfig().getStringList("verified");
          if (!verified.contains(p.getUniqueId().toString()))
            verified.add(p.getUniqueId().toString());
          UMN.pl.getConfig().set("verified", verified);

          if (args.length >= 2) {
            String name = args[1];
            if (UMN.pl.getConfig().contains(p.getUniqueId().toString())) {
              UMN.pl.getConfig().set(p.getUniqueId().toString() + ".name", name);
            }
            if (p.isOnline()) {
              Player pl = (Player) p;
              pl.setDisplayName(name);
              pl.setPlayerListHeaderFooter(ChatColor.RED + ChatColor.BOLD.toString() + "UMN SMP",
                  ChatColor.YELLOW + "https://discord.gg/eGXCYytxEw");
              pl.setPlayerListName(p.isOp()
                  ? ChatColor.RED + ChatColor.BOLD.toString() + "MOD " + ChatColor.RESET.toString() + pl.getDisplayName()
                  : ChatColor.RESET.toString() + pl.getDisplayName());
            }
          }

          UMN.pl.saveConfig();
          cs.sendMessage("Verified " + args[0] + " - " + p.getUniqueId().toString());
          UMN.updateScoreboard();
        } else {
          cs.sendMessage("player null");
        }
      }
    } else {
      if (cs instanceof Player) {
        if (label.equalsIgnoreCase("recipe")) {
          if (args.length == 0 || args.length < 2) {
            cs.sendMessage(" ");
            cs.sendMessage(ChatColor.DARK_GREEN + "    Recipes ");
            cs.sendMessage(" ");
            cs.sendMessage(
                ChatColor.GREEN + "/recipe show all " + ChatColor.GOLD + " - Shows all of the registered recipes.");
            cs.sendMessage(
                ChatColor.GREEN + "/recipe show <recipe> " + ChatColor.GOLD + " - Shows the specified recipe.");
            cs.sendMessage(ChatColor.GREEN + "/recipe ingredients <recipe> " + ChatColor.GOLD
                + " - Dumps the ingredients necessary for the recipe in the chat.");
            cs.sendMessage(" ");
          } else {
            if (args[0].equalsIgnoreCase("show")) {
              if (args[1].equalsIgnoreCase("all")) {
                cs.sendMessage(" ");
                cs.sendMessage(ChatColor.DARK_GREEN + "    Available Recipes ");
                cs.sendMessage(" ");
                for (String s : UMN.recipes)
                  cs.sendMessage(ChatColor.GOLD + s);
                cs.sendMessage(" ");
              } else {
                for (String s : UMN.recipes)
                  if (args[1].equals(s)) {
                    ClickableInventory inv = new ClickableInventory(5,
                        ChatColor.GOLD + Listeners.getFormattedName(s) + " Recipe") {
                      @Override
                      public void refresh() {
                        this.clear();
                        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                        ItemMeta im = filler.getItemMeta();
                        im.setDisplayName(" ");
                        filler.setItemMeta(im);

                        for (int i = 0; i < this.getSize(); i++) {
                          this.setItem(i, filler);
                        }

                        ShapedRecipe rec = null;
                        for (ShapedRecipe sr : UMN.cRecipes)
                          if (sr.getKey().getKey().equals(args[1]))
                            rec = sr;
                        // "123", "456", "789"
                        char s1 = ' ', s2 = ' ', s3 = ' ', s4 = ' ', s5 = ' ', s6 = ' ', s7 = ' ', s8 = ' ', s9 = ' ';
                        try {
                          s1 = rec.getShape()[0].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s2 = rec.getShape()[0].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s3 = rec.getShape()[0].charAt(2);
                        } catch (Exception ex) {
                        }
                        try {
                          s4 = rec.getShape()[1].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s5 = rec.getShape()[1].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s6 = rec.getShape()[1].charAt(2);
                        } catch (Exception ex) {
                        }
                        try {
                          s7 = rec.getShape()[2].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s8 = rec.getShape()[2].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s9 = rec.getShape()[2].charAt(2);
                        } catch (Exception ex) {
                        }

                        this.setItem(UMN.locations[0],
                            (rec.getIngredientMap().get(s1) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s1)));
                        this.setItem(UMN.locations[1],
                            (rec.getIngredientMap().get(s2) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s2)));
                        this.setItem(UMN.locations[2],
                            (rec.getIngredientMap().get(s3) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s3)));
                        this.setItem(UMN.locations[3],
                            (rec.getIngredientMap().get(s4) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s4)));
                        this.setItem(UMN.locations[4],
                            (rec.getIngredientMap().get(s5) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s5)));
                        this.setItem(UMN.locations[5],
                            (rec.getIngredientMap().get(s6) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s6)));
                        this.setItem(UMN.locations[6],
                            (rec.getIngredientMap().get(s7) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s7)));
                        this.setItem(UMN.locations[7],
                            (rec.getIngredientMap().get(s8) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s8)));
                        this.setItem(UMN.locations[8],
                            (rec.getIngredientMap().get(s9) == null ? new ItemStack(Material.AIR)
                                : rec.getIngredientMap().get(s9)));
                        this.setItem(UMN.locations[9], rec.getResult());
                      }
                    };
                    ((Player) cs).openInventory(inv);
                    inv.refresh();
                  }
              }
            } else if (args[0].equalsIgnoreCase("ingredients")) {
              for (String s : UMN.recipes)
                if (args[1].equals(s)) {
                  ShapedRecipe rec = null;
                  for (ShapedRecipe sr : UMN.cRecipes)
                    if (sr.getKey().getKey().equals(args[1]))
                      rec = sr;

                  cs.sendMessage(ChatColor.GREEN + "Ingredients required to craft " + rec.getKey().getKey() + ":");
                  cs.sendMessage(" ");

                  List<Character> sentTypes = new ArrayList<Character>();
                  for (char ch : rec.getIngredientMap().keySet()) {
                    if (ch != ' ') {
                      if (!sentTypes.contains(ch)) {

                        char s1 = ' ', s2 = ' ', s3 = ' ', s4 = ' ', s5 = ' ', s6 = ' ', s7 = ' ', s8 = ' ', s9 = ' ';
                        try {
                          s1 = rec.getShape()[0].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s2 = rec.getShape()[0].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s3 = rec.getShape()[0].charAt(2);
                        } catch (Exception ex) {
                        }
                        try {
                          s4 = rec.getShape()[1].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s5 = rec.getShape()[1].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s6 = rec.getShape()[1].charAt(2);
                        } catch (Exception ex) {
                        }
                        try {
                          s7 = rec.getShape()[2].charAt(0);
                        } catch (Exception ex) {
                        }
                        try {
                          s8 = rec.getShape()[2].charAt(1);
                        } catch (Exception ex) {
                        }
                        try {
                          s9 = rec.getShape()[2].charAt(2);
                        } catch (Exception ex) {
                        }

                        int i = 0;
                        if (ch == s1)
                          i++;
                        if (ch == s2)
                          i++;
                        if (ch == s3)
                          i++;
                        if (ch == s4)
                          i++;
                        if (ch == s5)
                          i++;
                        if (ch == s6)
                          i++;
                        if (ch == s7)
                          i++;
                        if (ch == s8)
                          i++;
                        if (ch == s9)
                          i++;

                        cs.sendMessage(
                            ChatColor.YELLOW + "x" + i + " " + rec.getIngredientMap().get(ch).getType().toString());
                        sentTypes.add(ch);
                      }
                    }
                  }
                  cs.sendMessage(" ");
                }
            }
          }
        } else if (label.equalsIgnoreCase("test") && cs.isOp()) {
          Player p = (Player) cs;
          p.getInventory().addItem(CustomItem.getNametag());
        } else if (label.equalsIgnoreCase("help")) {
          cs.sendMessage(" ");
          cs.sendMessage(ChatColor.RED + "  UMN Rocket League - Help");
          cs.sendMessage(" ");
          cs.sendMessage(ChatColor.DARK_GREEN + "/recipe show all " + ChatColor.YELLOW
              + "Shows all of the custom registered recipes");
          cs.sendMessage(ChatColor.DARK_GREEN + "/recipe show <recipe> " + ChatColor.YELLOW
              + "Graphically shows the specified recipe");
          cs.sendMessage(ChatColor.DARK_GREEN + "/recipe ingredients <recipe> " + ChatColor.YELLOW
              + "Dumps the ingredients needed to craft the recipe");
          cs.sendMessage(ChatColor.DARK_GREEN + "/xp " + ChatColor.YELLOW + "Shows your current exp");
          cs.sendMessage(ChatColor.DARK_GREEN + "/xpbottle <amount> " + ChatColor.YELLOW
              + "Extracts exp from your player into a bottle");
          cs.sendMessage(ChatColor.DARK_GREEN + "/beds " + ChatColor.YELLOW + "Lists all beds you have placed");
          cs.sendMessage(
              ChatColor.DARK_GREEN + "/tpa <player> " + ChatColor.YELLOW + "Sends a teleport request to a player");
          cs.sendMessage(ChatColor.DARK_GREEN + "/tpaccept " + ChatColor.YELLOW + "Accepts the last teleport request");
          cs.sendMessage(ChatColor.DARK_GREEN + "/tpdeny " + ChatColor.YELLOW + "Denies the last teleport request");
          cs.sendMessage(ChatColor.DARK_GREEN + "/msg <player> <message> " + ChatColor.YELLOW
              + "Sends a message to another player");
          cs.sendMessage(ChatColor.DARK_GREEN + "/r <message> " + ChatColor.YELLOW
              + "Replies to the last player that sent a message to you");
        } else if (!cs.isOp() && (label.equalsIgnoreCase("plugins") || label.equalsIgnoreCase("pl"))) {
          cs.sendMessage(ChatColor.RED + "You don't have access to that command.");
        }
      }
    }
    return true;
  }
}
