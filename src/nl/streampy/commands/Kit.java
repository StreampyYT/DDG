package nl.streampy.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nl.streampy.Main;
import nl.streampy.library.KitClass;

public class Kit implements CommandExecutor {

	Main plugin = (Main) Main.getPlugin(Main.class);
	
	public Kit(Main main) {
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
		
		if (args.length == 0 ) {
			//help menu
			if (player.hasPermission("kit.create"))
				player.sendMessage(ChatColor.GREEN + "/kit create <naam>");
			if (player.hasPermission("kit.update"))
				player.sendMessage(ChatColor.GREEN + "/kit update <naam>");
			if (player.hasPermission("kit.displayname"))
				player.sendMessage(ChatColor.GREEN + "/kit displayname <naam> <displayname>");
			if (player.hasPermission("kit.delete"))
				player.sendMessage(ChatColor.GREEN + "/kit delete <naam>");
			if (player.hasPermission("kit.seticon"))
				player.sendMessage(ChatColor.GREEN + "/kit seticon <naam>");
			if (player.hasPermission("kit.list"))
				player.sendMessage(ChatColor.GREEN + "/kit list");
			if (player.hasPermission("kit.select"))
				player.sendMessage(ChatColor.GREEN + "/kit <naam>");
			return false;
		}
		
		switch(args[0].toLowerCase()) {
			case "create":
				if (!player.hasPermission("kit.create")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Check of de lengte genoeg is om de commando uit te voeren!
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
					return false;
				}
				
				//Check of de kit wel niet bestaat
				if (kitExist(args[1])) {
					player.sendMessage(ChatColor.RED + "Er is al een kit met de naam " + args[1] + "!");
					return false;
				}
				//We maken een nieuwe kit aan met de naam args[1] het default icon word STONE_SWORD dit kan later nog worden veranderd
				KitClass kitC = new KitClass(args[1], new ItemStack(Material.STONE_SWORD), player);
				plugin.kits.add(kitC);
				player.sendMessage(ChatColor.GREEN + "Je hebt succesvol de kit " +  ChatColor.DARK_GREEN + args[1] + ChatColor.GREEN + " aangemaakt!");
				break;
			case "update":
				if (!player.hasPermission("kit.update")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//update een kit / zet een nieuw inventory voor in de plaats van
				//Check of de lengte genoeg is om de commando uit te voeren!
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
					return false;
				}
				
				//Check of de kit wel bestaat
				if (!kitExist(args[1])) {
					player.sendMessage(ChatColor.RED + "Geen kit gevonden met de naam " + args[1] + "!");
					return false;
				}
				
				KitClass update = getKit(args[1]);
				update.setInventory(player.getInventory());
				player.sendMessage(ChatColor.GREEN + "De inventory van " + args[1] + " is nu geupdate!");
				break;
			case "displayname":
				if (!player.hasPermission("kit.displayname")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Check of de lengte genoeg is om de commando uit te voeren!
				if (args.length == 1 || args.length == 2) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam> <displayname>");
					return false;
				}
				
				//Check of de kit wel bestaat
				if (!kitExist(args[1])) {
					player.sendMessage(ChatColor.RED + "Geen kit gevonden met de naam " + args[1] + "!");
					return false;
				}
				
				//Krijg alle berichten vanaf het 2e argument in 1 string
				String name = "";
				for (int i = 2; i < args.length; i++) {
					name += " " + args[i];
				}
				//Haal de spatie die ervoor staat eruit.
				name = name.trim();
				
				//Pak de kit en zet displayname
				KitClass displayname = getKit(args[1]);
				displayname.setDisplayName(name);
				player.sendMessage(ChatColor.GREEN + "De displayname van " + ChatColor.DARK_GREEN +  args[1] + ChatColor.GREEN + " is gezet naar " + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.GREEN + "!");
				break;
			case "delete":
				if (!player.hasPermission("kit.delete")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//Check of de lengte genoeg is om de commando uit te voeren!
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
					return false;
				}
				
				//Check of de kit wel bestaat
				if (!kitExist(args[1])) {
					player.sendMessage(ChatColor.RED + "Geen kit gevonden met de naam " + args[1] + "!");
					return false;
				}
				
				//Loop door arraylist en als hij match vind verwijder hem en stop.
				for (int i = 0; i < plugin.kits.size(); i++) {
					if (plugin.kits.get(i).getName().equalsIgnoreCase(args[1])) {
						plugin.kits.remove(i);
						return false;
					}
				}
				
				player.sendMessage(ChatColor.RED + "Je hebt succesvol de kit " + args[1] + "verwijderd!");
				break;
			case "seticon":
				if (!player.hasPermission("kit.seticon")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + "Gebruik /" + cmd.getName() + " " +  args[0] + " <naam>");
					return false;
				}
				
				//Check of de kit wel bestaat
				if (!kitExist(args[1])) {
					player.sendMessage(ChatColor.RED + "Geen kit gevonden met de naam " + args[1] + "!");
					return false;
				}
				
				//Check of het item in zijn hand niet niks is!
				if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == null || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
					player.sendMessage(ChatColor.RED + "Je moet een item in je normale hand houden om als icon te zetten!");
					return false;
				}
				
				KitClass icon = getKit(args[1]);
				icon.setIcon(player.getInventory().getItemInMainHand());
				player.sendMessage(ChatColor.GREEN + "De icon van " + args[1] + " is nu geupdate!");
				break;
			case "list":
				if (!player.hasPermission("kit.list")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				
				String kitList = "";
				
				for (KitClass kits : plugin.kits) {
					kitList += ", " + kits.getName();
				}
				
				if (kitList.length() > 0) {
					kitList = kitList.substring(2);
				}
				player.sendMessage(ChatColor.GREEN + "Kits: " + (kitList == "" ? ChatColor.RED + "Er zijn geen kits gevonden!" : ChatColor.DARK_GREEN + kitList));
				break;
			default:
				if (!player.hasPermission("kit.select")) {
					player.sendMessage(ChatColor.RED + "Je hebt niet de juiste bevoegdheden op deze commando te gebruiken!");
					return false;
				}
				//pak de kit die je oproept
				//Check of de kit wel bestaat
				if (!kitExist(args[0])) {
					player.sendMessage(ChatColor.RED + "Geen kit gevonden met de naam " + args[0] + "!");
				}
				
				KitClass C = getKit(args[0]);
				C.givePlayer(player);
				player.sendMessage(ChatColor.GREEN + "Je hebt de kit " + ChatColor.DARK_GREEN + args[0] + ChatColor.GREEN + " ontvangen!");
		}
		return false;
	}
	
	private boolean kitExist(String name) {
		if (getKit(name) == null) {
			return false;
		}
		return true;
	}
	
	private KitClass getKit(String name) {
		for (KitClass C : plugin.kits) {
			if (C.getName().equalsIgnoreCase(name)) {
				return C;
			}
		}
		return null;
	}

}
