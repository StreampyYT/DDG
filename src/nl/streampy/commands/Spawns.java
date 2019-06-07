package nl.streampy.commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.streampy.Main;
import nl.streampy.functions.Mysql;

public class Spawns extends Mysql implements CommandExecutor {

	Main plugin = (Main) Main.getPlugin(Main.class);
	
	public Spawns(Main main) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			//Als de sender geen speler is! dus door console of command block etc
			sender.sendMessage(ChatColor.RED + "Je moet een speler zijn om deze command uit te voeren!");
			return false;
		}
		
		Player player = (Player) sender;
		
		if (args.length == 0) {
			if (player.hasPermission("spawns.set"))
				player.sendMessage(ChatColor.GREEN + "/spawns set <naam>");
			if (player.hasPermission("spawns.delete"))
				player.sendMessage(ChatColor.GREEN + "/spawns delete <naam>");
			if (player.hasPermission("spawns.setmain"))
				player.sendMessage(ChatColor.GREEN + "/spawns setmain <naam>");
			if (player.hasPermission("spawns.list"))
				player.sendMessage(ChatColor.GREEN + "/spawns list");
			if (player.hasPermission("spawns.select"))
				player.sendMessage(ChatColor.GREEN + "/spawns select");
			//Als er geen sub commands zijn bij gestuurd.
			
			return false;
		}
		
		switch(args[0].toLowerCase()) {
			case "set":
				if (!player.hasPermission("spawns.set")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				
				if (args.length == 2) {
					//args[1] is de naam van de spawn
					if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[1] + "'") != true) {
						Location loc = player.getLocation();
						update("INSERT INTO " + plugin.ltable + " (Name, World, X, Y, Z, Yaw, Pitch, Main) VALUE ('" + args[1] + "','" + loc.getWorld().getName() + "'," + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ", " + loc.getYaw() + ", " + loc.getPitch() + ", '" + 0 + "')");
						player.sendMessage(ChatColor.GREEN + "Je hebt succesvol de locatie " +  ChatColor.DARK_GREEN + args[1] + ChatColor.GREEN + " aangemaakt!");
					}else {
						player.sendMessage(ChatColor.RED + "Er is al een Locatie met de naam " + args[1] + "!");
					}
				}else {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
				}
				break;
			case "delete":
				if (!player.hasPermission("spawns.delete")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Delete een bestaande spawn locatie
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
					return false;
				}
				
				if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[1] + "'") != true) {
					player.sendMessage(ChatColor.RED + "Geen locatie gevonden met de naam " + args[1] + "!");
					return false;
				}
				
				update("DELETE FROM " + plugin.ltable + " WHERE Name='" + args[1] + "'");
				player.sendMessage(ChatColor.GREEN + "Locatie is succesvol gedeleted!");
				break;
			case "setmain":
				if (!player.hasPermission("spawns.setmain")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Set de login / respawn locatie!
				if (args.length == 2) {
					//Kijk of de locatie bestaat
					if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[1] + "'") == true) {
						//update all spawns die Main=1 hebben naar 0 & zet de locatie die je aangaf als Main 1
						update("UPDATE " + plugin.ltable + " SET Main=(" + 0 + ") WHERE Main=" + 1);
						update("UPDATE " + plugin.ltable + " SET Main=(" + 1 + ") WHERE Name='" + args[1] + "'");
						//Selecteer de locatie om de gegevens op te halen en als respawn/spawn locatie in te zetten
						ResultSet select = select("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[1] + "'");
						try {
							select.next();
							plugin.spawnLocation = new Location(Bukkit.getWorld(select.getString("world")), select.getDouble("X"), select.getDouble("Y"), select.getDouble("Z"), (float) select.getDouble("Yaw"), (float) select.getDouble("Pitch"));
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
						player.sendMessage(ChatColor.GREEN + "De locatie " + ChatColor.DARK_GREEN + args[1] + ChatColor.GREEN + " is nu gezet als main (spawn/respawn) locatie!");
					}else {
						player.sendMessage(ChatColor.RED + "Geen locatie gevonden met de naam " + args[1] + "!");
					}
				}else {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
				}
				break;
			case "list":
				if (!player.hasPermission("spawns.list")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Stuur een lijst met alle spawn locatie's
				String spawnList = "";
				ResultSet selects = select("SELECT * FROM " + plugin.ltable);
				try {
					while (selects.next()) {
						spawnList += ", " + selects.getString("Name");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				if (spawnList.length() > 0) {
					spawnList = spawnList.substring(2);
				}
				player.sendMessage(ChatColor.GREEN + "Locations: " + (spawnList == "" ? ChatColor.RED + "Er zijn geen locations gevonden!" : ChatColor.DARK_GREEN + spawnList));
				break;
			default:
				if (!player.hasPermission("spawns.select")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Teleport naar de locatie
				//Check of er wel een Locatie bestaat met de naam zo ja haal gegeven ervan op
				if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[0] + "'") == true) {
					ResultSet select = select("SELECT * FROM " + plugin.ltable + " WHERE Name='" + args[0] + "'");
					try {
						select.next();
						//Check of de wereld nog bestaat voordat hem ernaar toe sturen
						if (Bukkit.getWorld(select.getString("world")) == null) {
							player.sendMessage(ChatColor.RED + "De wereld waarin deze locatie staat bestaat niet meer of is hernoemt!");
							return false;
						}
						player.teleport(new Location(Bukkit.getWorld(select.getString("world")), select.getDouble("X"), select.getDouble("Y"), select.getDouble("Z"), (float) select.getDouble("Yaw"), (float) select.getDouble("Pitch")));
						player.sendMessage(ChatColor.GREEN + "Geteleporteerd naar " + ChatColor.DARK_GREEN + args[0] + ChatColor.GREEN + "!");
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
				}else {
					player.sendMessage(ChatColor.RED + "Geen locatie gevonden met de naam " + args[0] + "!");
				}
		}
		return false;
	}

}
