package dev.chezy.umn;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UMN extends JavaPlugin implements Listener {
  public static Plugin pl;
  public static PluginManager pm;
  public static String prefix = ChatColor.translateAlternateColorCodes('&', "&4[&6&lUMN&r&4] &9");
  
  public void onLoad() {
    pl = this;
    pm = getServer().getPluginManager();

    getLogger().log(Level.INFO, "Loaded UMN Plugin!");
  }

  public void onUnload() {
    getLogger().log(Level.INFO, "Shutting down UMN...");
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    e.setJoinMessage(ChatColor.GOLD + e.getPlayer().getName().toString() + ChatColor.RED.toString() + " just joined.");
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent e) {
    e.setQuitMessage(ChatColor.GOLD + e.getPlayer().getName().toString() + ChatColor.RED.toString() + " just disconnected.");
  }

  @EventHandler
  public void onPing(ServerListPingEvent e) {
    e.setMaxPlayers(Bukkit.getOnlinePlayers().size() + 1);
    e.setMotd(ChatColor.translateAlternateColorCodes('&', "   &4-= &6&lUMN Rocket League&r &4=-"));
  }
}
