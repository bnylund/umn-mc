package dev.chezy.umn;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.event.Listener;

public class Bed implements Listener
{
  private Location location;
  private UUID owner;
  private String name;
  
  protected Bed(final Location loc, final UUID own, final String name) {
    this.location = loc;
    this.owner = own;
    this.name = name;
  }
  
  public Location getLocation() {
    return this.location;
  }

  public String getName() {
    return this.name.length() == 0 ? "Unnamed Bed" : this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public UUID getOwner() {
    return this.owner;
  }
}