package dev.chezy.umn;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.event.Listener;

public class Bed implements Listener
{
  private Location location;
  private UUID owner;
  
  protected Bed(final Location loc, final UUID own) {
    this.location = loc;
    this.owner = own;
  }
  
  public Location getLocation() {
    return this.location;
  }
  
  public UUID getOwner() {
    return this.owner;
  }
}