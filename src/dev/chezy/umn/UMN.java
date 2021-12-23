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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
	public static int[] locations = { 11, 12, 13, 20, 21, 22, 29, 30, 31, 24};
	public static List<ShapedRecipe> cRecipes;
	public static String[] recipes = { "item_nametag" };
	public static List<String> inNametag;
	public static HashMap<String, Location> inBedRename;
	public static HashMap<Location, KeyValue<Integer, Integer>> bedCooldown; // K: task_id, V: cooldown secs
	public static List<Integer> tasks;
  
  public void onEnable() {
    pl = this;
    pm = getServer().getPluginManager();
		cRecipes = new ArrayList<ShapedRecipe>();
		inNametag = new ArrayList<String>();
		inBedRename = new HashMap<String, Location>();
		bedCooldown = new HashMap<Location, KeyValue<Integer, Integer>>();
		
		Bukkit.getLogger().log(Level.INFO, "Registering recipes...");
		registerRecipes();
	
		for (final Player p : Bukkit.getOnlinePlayers()) {
			// Add to config if they dont exist
			if (!pl.getConfig().contains("KnownPlayers")) {
				pl.getConfig().set("KnownPlayers", new String[] { p.getUniqueId().toString() });
			}
			else {
				final List<String> players = pl.getConfig().getStringList("KnownPlayers");
				if (!players.contains(p.getUniqueId().toString())) {
					players.add(p.getUniqueId().toString());
				}
				pl.getConfig().set("KnownPlayers", players);
			}
			pl.getConfig().set(p.getUniqueId().toString() + ".name", p.getName());
		}

    pm.registerEvents(new Listeners(), this);
		pm.registerEvents(this, this);
		getCommand("beds").setExecutor(new BedsCommand());
    getCommand("verify").setExecutor(new Commands());
		getCommand("recipe").setExecutor(new Commands());
		getCommand("test").setExecutor(new Commands());

    if(!pl.getConfig().contains("verified")) {
      pl.getConfig().set("verified", new ArrayList<String>());
    }

		pl.saveConfig();

		getLogger().log(Level.INFO, "Loading scoreboard...");
		main = new Sidebar(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "     Players     ", (Plugin) this, 20, new SidebarString[0]);
		for (Player p : Bukkit.getOnlinePlayers()) {
			main.showTo(p);
		}

		updateScoreboard();

		getLogger().log(Level.INFO, "Loading global inventory timer...");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					// topinv == clickable
					if(p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() instanceof ClickableInventory) {
						ClickableInventory ci = (ClickableInventory) p.getOpenInventory().getTopInventory();
						ci.refresh();
					}
				}
			}
		}, 0L, 20L);

    getLogger().log(Level.INFO, "Loaded UMN Plugin!");
  }

  public void onDisable() {
    getLogger().log(Level.INFO, "Shutting down UMN...");
		for (Player p: Bukkit.getOnlinePlayers()) {
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
		} catch(Exception ex) {}
	}
  
	public static void updateScoreboard() {
		main.setEntries(new ArrayList<SidebarString>());
		main.addEmpty();
		for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
			if(p.isOnline())
				main.addEntry(new SidebarString(ChatColor.GREEN + p.getName()));
		}
		for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
			if(!p.isOnline())
				main.addEntry(new SidebarString(ChatColor.RED + p.getName()));
		}
		main.addEmpty();
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
			if(pl.getConfig().contains(player.toString() + ".Beds." + i + ".name"))
				name = pl.getConfig().getString(player.toString() + ".Beds." + i + ".name");
			beds.add(new Bed(new Location(Bukkit.getWorld(world), (double)x, (double)y, (double)z), player, name));
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
		KeyValue<Integer, Integer> cooldown = bedCooldown.get(new Location(bed.getLocation().getWorld(), bed.getLocation().getBlockX(), bed.getLocation().getBlockY(), bed.getLocation().getBlockZ()));
		return cooldown == null ? 0 : cooldown.getValue();
	}

	public static void setCooldown(Bed bed, Integer seconds) {
		if(bedCooldown.containsKey(bed.getLocation())) {
			KeyValue<Integer, Integer> cur = bedCooldown.get(bed.getLocation());
			cur.setValue(cur.getValue()-1);
			bedCooldown.replace(bed.getLocation(), cur);
			return;
		}

		int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(UMN.pl, new Runnable() {
			@Override
			public void run() {
				if(bedCooldown.containsKey(bed.getLocation()) && bed.getLocation().getBlock().getType().toString().contains("BED")) {
					KeyValue<Integer, Integer> cur = bedCooldown.get(bed.getLocation());
					cur.setValue(cur.getValue()-1);
					bedCooldown.replace(bed.getLocation(), cur);
					if(bedCooldown.get(bed.getLocation()).getValue() <= 0) {
						bedCooldown.remove(bed.getLocation());
						Bukkit.getScheduler().cancelTask(cur.getKey());
					}
					return;
				}
			}
		}, 20, 20);
		bedCooldown.put(bed.getLocation(), new KeyValue<Integer,Integer>(task, seconds));
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent e) {
		if (e.getBlock().getType().toString().contains("BED") && e.getBlock().getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
			final SurvivalPlayer pl = new SurvivalPlayer(e.getPlayer());
			if(pl.getBeds().size() >= 9) {
				pl.sendMessage("&cYou have reached your bed limit. It will not be saved.");
				return;
			}
			final Bed bed = new Bed(e.getBlock().getLocation(), pl.getUUID(), "");
			pl.addBed(bed);
			pl.sendMessage("&6Your bed was saved!");
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		if (e.getBlock().getType().toString().contains("BED")) {
			for (final SurvivalPlayer p : getAllPlayers()) {
				for (final Bed bed : p.getBeds()) {
					Location location = bed.getLocation();
					if (e.getBlock().getLocation().getBlockX() == location.getBlockX() && e.getBlock().getLocation().getBlockY() == location.getBlockY() && e.getBlock().getLocation().getBlockZ() == location.getBlockZ() && e.getBlock().getWorld().getName() == location.getWorld().getName()) {
						p.removeBed(bed);
						p.sendMessage("&6One of your beds was destroyed! Location: " + location.getBlockX() + "x " + location.getBlockY() + "y " + location.getBlockZ() + "z");
						return;
					}
					else {
						final BlockData state = e.getBlock().getBlockData();
						if (!(state instanceof org.bukkit.block.data.type.Bed)) {
							continue;
						}
						org.bukkit.block.data.type.Bed bedFace = (org.bukkit.block.data.type.Bed) state;
						final Block blo = e.getBlock().getRelative(bedFace.getFacing().getOppositeFace());
						Location loc = blo.getLocation();
						if (loc.getBlockX() != location.getBlockX() || loc.getBlockY() != location.getBlockY() || loc.getBlockZ() != location.getBlockZ() || loc.getWorld().getName() != location.getWorld().getName()) {
							continue;
						}
						p.removeBed(bed);
						p.sendMessage("&6One of your beds was destroyed! Location: " + location.getBlockX() + "x " + location.getBlockY() + "y " + location.getBlockZ() + "z");
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		main.showTo(e.getPlayer());
		updateScoreboard();
		if (!pl.getConfig().contains("KnownPlayers")) {
			pl.getConfig().set("KnownPlayers", new String[] { e.getPlayer().getUniqueId().toString() });
		}
		else {
			final List<String> players = pl.getConfig().getStringList("KnownPlayers");
			if (!players.contains(e.getPlayer().getUniqueId().toString())) {
				players.add(e.getPlayer().getUniqueId().toString());
			}
			pl.getConfig().set("KnownPlayers", players);
		}
		pl.getConfig().set(e.getPlayer().getUniqueId().toString() + ".name", e.getPlayer().getName());
		pl.saveConfig();
		e.getPlayer().setGameMode(GameMode.SURVIVAL);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
			@Override
			public void run() {
				updateScoreboard();
			}
		}, 30L);
	}
}
