package dev.chezy.umn;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import java.util.List;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SurvivalPlayer
{
  private String Name;
  private UUID Uuid;
  private Player _player;
  private List<Bed> Beds;
  
  protected SurvivalPlayer(final Player player) {
    this.Name = player.getName();
    this.Uuid = player.getUniqueId();
    this._player = player;
    this.Beds = UMN.getPlayerBeds(player.getUniqueId());
  }
  
  protected SurvivalPlayer(final UUID player) {
    final OfflinePlayer p = Bukkit.getOfflinePlayer(player);
    this.Name = p.getName();
    this.Uuid = p.getUniqueId();
    this.Beds = UMN.getPlayerBeds(player);
    try {
      this._player = Bukkit.getPlayer(player);
    }
    catch (Exception ex) {}
  }
  
  public String getName() {
    return this.Name;
  }
  
  public UUID getUUID() {
    return this.Uuid;
  }
  
  public List<Bed> getBeds() {
    return this.Beds;
  }
  
  public void addBed(final Bed bed) {
    this.Beds.add(bed);
    this.saveAllBeds();
  }
  
  public void sendMessage(final String message) {
    this._player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
  }
  
  public void removeBed(final Bed bed) {
    this.Beds.remove(bed);
    this.saveAllBeds();
  }
  
  public void teleportToBed(final Bed bed) {
    this._player.teleport(bed.getLocation());
  }
  
  public void saveAllBeds() {
    UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".Beds", null);
    UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".TotalBeds", this.Beds.size());
    for (int i = 0; i < this.Beds.size(); ++i) {
      final Bed bed = this.Beds.get(i);
      UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".Beds." + i + ".x", bed.getLocation().getBlockX());
      UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".Beds." + i + ".y", bed.getLocation().getBlockY());
      UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".Beds." + i + ".z", bed.getLocation().getBlockZ());
      UMN.pl.getConfig().set(String.valueOf(this.Uuid.toString()) + ".Beds." + i + ".world", bed.getLocation().getWorld().getName());
    }
    UMN.pl.saveConfig();
  }
}