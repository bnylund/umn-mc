package dev.chezy.umn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import com.coloredcarrot.api.sidebar.Sidebar;
import com.coloredcarrot.api.sidebar.SidebarString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UMN extends JavaPlugin implements Listener {
  public static Plugin pl;
  public static PluginManager pm;
  public static String prefix = ChatColor.translateAlternateColorCodes('&', "&4[&6&lUMN&r&4] &9");
  public static Sidebar main;
  public static int[] locations = { 11, 12, 13, 20, 21, 22, 29, 30, 31, 24 };
  public static List<ShapedRecipe> cRecipes;
  public static String[] recipes = { "item_nametag" };
  public static List<String> inNametag;
  public static HashMap<String, Location> inBedRename;
  public static HashMap<Location, KeyValue<Integer, Integer>> bedCooldown; // K: task_id, V: cooldown secs
  public static HashMap<String, Integer> xpCooldown;

  public void onEnable() {
    pl = this;
    pm = getServer().getPluginManager();
    cRecipes = new ArrayList<ShapedRecipe>();
    inNametag = new ArrayList<String>();
    inBedRename = new HashMap<String, Location>();
    bedCooldown = new HashMap<Location, KeyValue<Integer, Integer>>();
    xpCooldown = new HashMap<String, Integer>();

    Bukkit.getLogger().log(Level.INFO, "Registering recipes...");
    registerRecipes();

    for (final Player p : Bukkit.getOnlinePlayers()) {
      // Add to config if they dont exist
      if (!pl.getConfig().contains("KnownPlayers")) {
        pl.getConfig().set("KnownPlayers", new String[] { p.getUniqueId().toString() });
      } else {
        final List<String> players = pl.getConfig().getStringList("KnownPlayers");
        if (!players.contains(p.getUniqueId().toString())) {
          players.add(p.getUniqueId().toString());
        }
        pl.getConfig().set("KnownPlayers", players);
      }
      if(!pl.getConfig().contains(p.getUniqueId().toString() + ".name"))
        pl.getConfig().set(p.getUniqueId().toString() + ".name", p.getName());
    }

    pm.registerEvents(new Listeners(), this);
    pm.registerEvents(new XPBottle(), this);
    pm.registerEvents(this, this);
    getCommand("beds").setExecutor(new BedsCommand());
    getCommand("verify").setExecutor(new Commands());
    getCommand("recipe").setExecutor(new Commands());
    getCommand("test").setExecutor(new Commands());
    getCommand("plugins").setExecutor(new Commands());
    getCommand("pl").setExecutor(new Commands());
    getCommand("help").setExecutor(new Commands());
    getCommand("xpbottle").setExecutor(new XPBottle());
    getCommand("xp").setExecutor(new XPBottle());

    if (!pl.getConfig().contains("verified")) {
      pl.getConfig().set("verified", new ArrayList<String>());
    }

    pl.saveConfig();

    for(final Player p : Bukkit.getOnlinePlayers()) {
      p.setDisplayName(pl.getConfig().getString(p.getUniqueId().toString() + ".name"));
      p.setPlayerListHeaderFooter(ChatColor.RED + ChatColor.BOLD.toString() + "UMN SMP",
          ChatColor.YELLOW + "https://discord.gg/eGXCYytxEw");
      p.setPlayerListName(p.isOp()
              ? ChatColor.RED + ChatColor.BOLD.toString() + "MOD " + ChatColor.RESET.toString() + p.getDisplayName()
              : ChatColor.RESET.toString() + p.getDisplayName());
    }

    getLogger().log(Level.INFO, "Loading scoreboard...");
    main = new Sidebar(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "     Players     ", (Plugin) this, 20,
        new SidebarString[0]);
    for (Player p : Bukkit.getOnlinePlayers()) {
      main.showTo(p);
    }

    updateScoreboard();

    getLogger().log(Level.INFO, "Loading global timer...");
    Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        // Clickable Inventory Refresh
        for (Player p : Bukkit.getOnlinePlayers()) {
          if (p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() instanceof ClickableInventory) {
            ClickableInventory ci = (ClickableInventory) p.getOpenInventory().getTopInventory();
            ci.refresh();
          }
        }

        // XP Cooldown
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
          if(xpCooldown.containsKey(p.getUniqueId().toString())) {
            xpCooldown.replace(p.getUniqueId().toString(), xpCooldown.get(p.getUniqueId().toString()) - 1);

            if(xpCooldown.get(p.getUniqueId().toString()) <= 0) {
              xpCooldown.remove(p.getUniqueId().toString());
            }
          }
        }
      }
    }, 0L, 20L);

    getLogger().log(Level.INFO, "Loaded UMN Plugin!");
  }

  public void onDisable() {
    getLogger().log(Level.INFO, "Shutting down UMN...");
    for (Player p : Bukkit.getOnlinePlayers()) {
      main.hideFrom(p);
    }
    Bukkit.getScheduler().cancelTasks(this);
  }

  private void registerRecipes() {
    NamespacedKey key = new NamespacedKey(this, "item_nametag");
    ShapedRecipe sr = new ShapedRecipe(key, CustomItem.getNametag());
    sr.shape(" D ", "DND", " D ");
    ExactChoice ec = new ExactChoice(new ItemStack(Material.DIAMOND, 1));
    sr.setIngredient('D', ec);
    ec = new ExactChoice(new ItemStack(Material.NAME_TAG, 1));
    sr.setIngredient('N', ec);
    cRecipes.add(sr);
    try {
      Bukkit.addRecipe(sr);
    } catch (Exception ex) {
    }
  }

  public static void updateScoreboard() {
    List<SidebarString> entries = new ArrayList<SidebarString>();
    entries.add(new SidebarString(new String(new char[entries.size()]).replace("\0", " ")));
    for (Player p : Bukkit.getOnlinePlayers()) {
      entries.add(new SidebarString(ChatColor.GREEN + p.getDisplayName()));
    }
    for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
      if (!p.isOnline())
        entries.add(new SidebarString(ChatColor.RED + (pl.getConfig().contains(p.getUniqueId().toString() + ".name") ? pl.getConfig().getString(p.getUniqueId().toString() + ".name") : p.getName())));
    }
    entries.add(new SidebarString(new String(new char[entries.size()]).replace("\0", " ")));
    entries.add(new SidebarString(ChatColor.RED + "UMN Rocket League"));
    main.setEntries(entries);
  }

  public static List<Bed> getPlayerBeds(final UUID player) {
    final int totalBeds = pl.getConfig().getInt(player.toString() + ".TotalBeds");
    final List<Bed> beds = new ArrayList<Bed>();
    for (int i = 0; i < totalBeds; ++i) {
      final int x = pl.getConfig().getInt(player.toString() + ".Beds." + i + ".x");
      final int y = pl.getConfig().getInt(player.toString() + ".Beds." + i + ".y");
      final int z = pl.getConfig().getInt(player.toString() + ".Beds." + i + ".z");
      final String world = pl.getConfig().getString(player.toString() + ".Beds." + i + ".world");
      String name = ChatColor.AQUA + ChatColor.BOLD.toString() + "Bed #" + i;
      if (pl.getConfig().contains(player.toString() + ".Beds." + i + ".name"))
        name = pl.getConfig().getString(player.toString() + ".Beds." + i + ".name");
      beds.add(new Bed(new Location(Bukkit.getWorld(world), (double) x, (double) y, (double) z), player, name));
    }
    return beds;
  }

  public static List<SurvivalPlayer> getAllPlayers() {
    final List<SurvivalPlayer> players = new ArrayList<SurvivalPlayer>();
    for (final String s : pl.getConfig().getStringList("KnownPlayers")) {
      players.add(new SurvivalPlayer(UUID.fromString(s)));
    }
    return players;
  }

  public static Integer getCooldown(Bed bed) {
    KeyValue<Integer, Integer> cooldown = bedCooldown.get(new Location(bed.getLocation().getWorld(),
        bed.getLocation().getBlockX(), bed.getLocation().getBlockY(), bed.getLocation().getBlockZ()));
    return cooldown == null ? 0 : cooldown.getValue();
  }

  public static void setCooldown(Bed bed, Integer seconds) {
    if (bedCooldown.containsKey(bed.getLocation())) {
      KeyValue<Integer, Integer> cur = bedCooldown.get(bed.getLocation());
      cur.setValue(cur.getValue() - 1);
      bedCooldown.replace(bed.getLocation(), cur);
      return;
    }

    int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(UMN.pl, new Runnable() {
      @Override
      public void run() {
        if (bedCooldown.containsKey(bed.getLocation())
            && bed.getLocation().getBlock().getType().toString().contains("BED")) {
          KeyValue<Integer, Integer> cur = bedCooldown.get(bed.getLocation());
          cur.setValue(cur.getValue() - 1);
          bedCooldown.replace(bed.getLocation(), cur);
          if (bedCooldown.get(bed.getLocation()).getValue() <= 0) {
            bedCooldown.remove(bed.getLocation());
            Bukkit.getScheduler().cancelTask(cur.getKey());
          }
          return;
        }
      }
    }, 20, 20);
    bedCooldown.put(bed.getLocation(), new KeyValue<Integer, Integer>(task, seconds));
  }

  /*@EventHandler
  public void onBlockPlace(final BlockPlaceEvent e) {
    if (e.getBlock().getType().toString().contains("BED")
        && e.getBlock().getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
      final SurvivalPlayer pl = new SurvivalPlayer(e.getPlayer());
      if (pl.getBeds().size() >= 9) {
        pl.sendMessage("&cYou have reached your bed limit. It will not be saved.");
        return;
      }
      final Bed bed = new Bed(e.getBlock().getLocation(), pl.getUUID(), "");
      pl.addBed(bed);
      pl.sendMessage("&6Your bed was saved!");
    }
  }*/

  @EventHandler
  public void onBedEnter(final PlayerBedEnterEvent e) {
    if(e.getBed().getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
      final SurvivalPlayer pl = new SurvivalPlayer(e.getPlayer());
      boolean hasBed = false;
      for(Bed b : pl.getBeds()) {
        if(b.getLocation().distance(e.getBed().getLocation()) < 3) {
          hasBed = true;
        }
      }
      if(!hasBed) {
        if (pl.getBeds().size() >= 9) {
          pl.sendMessage("&cYou have reached your bed limit. This bed will not be saved.");
          return;
        }
        final Bed bed = new Bed(e.getBed().getLocation(), pl.getUUID(), "");
        pl.addBed(bed);
        pl.sendMessage("&aBed saved!");
      }
    }
  }

  @EventHandler
  public void onBlockBreak(final BlockBreakEvent e) {
    if (e.getBlock().getType().toString().contains("BED")) {
      for (final SurvivalPlayer p : getAllPlayers()) {
        for (final Bed bed : p.getBeds()) {
          Location location = bed.getLocation();
          if (e.getBlock().getLocation().getBlockX() == location.getBlockX()
              && e.getBlock().getLocation().getBlockY() == location.getBlockY()
              && e.getBlock().getLocation().getBlockZ() == location.getBlockZ()
              && e.getBlock().getWorld().getName() == location.getWorld().getName()) {
            p.removeBed(bed);
            p.sendMessage("&cOne of your beds was destroyed! Location: " + location.getBlockX() + "x "
                + location.getBlockY() + "y " + location.getBlockZ() + "z");
          } else {
            final BlockData state = e.getBlock().getBlockData();
            if (!(state instanceof org.bukkit.block.data.type.Bed)) {
              continue;
            }
            org.bukkit.block.data.type.Bed bedFace = (org.bukkit.block.data.type.Bed) state;
            final Block blo = e.getBlock().getRelative(bedFace.getFacing().getOppositeFace());
            Location loc = blo.getLocation();
            if (loc.getBlockX() != location.getBlockX() || loc.getBlockY() != location.getBlockY()
                || loc.getBlockZ() != location.getBlockZ()
                || loc.getWorld().getName() != location.getWorld().getName()) {
              continue;
            }
            p.removeBed(bed);
            p.sendMessage("&cOne of your beds was destroyed! Location: " + location.getBlockX() + "x "
                + location.getBlockY() + "y " + location.getBlockZ() + "z");
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    if (!pl.getConfig().contains("KnownPlayers")) {
      pl.getConfig().set("KnownPlayers", new String[] { e.getPlayer().getUniqueId().toString() });
    } else {
      final List<String> players = pl.getConfig().getStringList("KnownPlayers");
      if (!players.contains(e.getPlayer().getUniqueId().toString())) {
        players.add(e.getPlayer().getUniqueId().toString());
      }
      pl.getConfig().set("KnownPlayers", players);
    }
    if(!pl.getConfig().contains(e.getPlayer().getUniqueId().toString() + ".name"))
      pl.getConfig().set(e.getPlayer().getUniqueId().toString() + ".name", e.getPlayer().getName());
    pl.saveConfig();
    e.getPlayer().setDisplayName(pl.getConfig().getString(e.getPlayer().getUniqueId().toString()  + ".name"));
    e.getPlayer().setGameMode(GameMode.SURVIVAL);
    e.getPlayer().setFlying(false);
    
    for (Player p : Bukkit.getOnlinePlayers())
      p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 200.0F, 100.0F);
    e.setJoinMessage(ChatColor.GOLD + e.getPlayer().getDisplayName().toString() + ChatColor.RED.toString() + " just joined.");
    e.getPlayer().setPlayerListHeaderFooter(ChatColor.RED + ChatColor.BOLD.toString() + "UMN SMP",
        ChatColor.YELLOW + "https://discord.gg/eGXCYytxEw");
    e.getPlayer()
        .setPlayerListName(e.getPlayer().isOp()
            ? ChatColor.RED + ChatColor.BOLD.toString() + "MOD " + ChatColor.RESET.toString() + e.getPlayer().getDisplayName()
            : ChatColor.RESET.toString() + e.getPlayer().getDisplayName());
    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&4[&c&l!&4] &cThere are no land protection features on this server! Build at your own risk."));
    main.showTo(e.getPlayer());
    updateScoreboard();
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent e) {
    Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
      @Override
      public void run() {
        updateScoreboard();
      }
    }, 30L);
    for (Player p : Bukkit.getOnlinePlayers())
      p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 200.0F, 100.0F);
    e.setQuitMessage(
        ChatColor.GOLD + e.getPlayer().getDisplayName().toString() + ChatColor.RED.toString() + " just disconnected.");
  }
}
