package nl.streampy.library;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import nl.streampy.Main;
import nl.streampy.functions.Mysql;

public class EventsHandler extends Mysql implements Listener {

	Main plugin = (Main) Main.getPlugin(Main.class);
	
	public EventsHandler(Main main) {
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		createPlayer(event.getPlayer());
		updateScoreboard(event.getPlayer());
		if (plugin.spawnLocation != null && plugin.spawnLocation.getWorld() != null) {
			event.getPlayer().teleport(plugin.spawnLocation);
		}
		
		//Clear het hele inventory en voeg een kit selector toe aan je inventory
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
		
		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta meta = compass.getItemMeta();
		
		meta.setDisplayName(ChatColor.GOLD + "Kit Selector");
		compass.setItemMeta(meta);
		
		event.getPlayer().getInventory().setItem(0, compass);
	}
	
	@EventHandler
	public void onDeathEvent(PlayerDeathEvent event) {		
		if (event.getEntity().getKiller() instanceof Player) {
			//Update de gevens in database
			update("UPDATE " + plugin.ptable + " SET Deaths=( Deaths + 1) WHERE UUID='" + event.getEntity().getUniqueId().toString() + "'");
			update("UPDATE " + plugin.ptable + " SET Kills=( Kills + 1 ) WHERE UUID='" + event.getEntity().getKiller().getUniqueId().toString() + "'");
			//Update de score in het scoreboard alleen als er iets gebeurd
			updateScoreboard(event.getEntity());
			updateScoreboard(event.getEntity().getKiller());
			
		}
		event.setDeathMessage(null);	
	}
	
	@EventHandler
	public void onRespawnEvent(PlayerRespawnEvent event) {
		if (plugin.spawnLocation != null && plugin.spawnLocation.getWorld() != null) {
			event.setRespawnLocation(plugin.spawnLocation);
		}
		
		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta meta = compass.getItemMeta();
		
		meta.setDisplayName(ChatColor.GOLD + "Kit Selector");
		compass.setItemMeta(meta);
		
		event.getPlayer().getInventory().setItem(0, compass);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		//Wanneer een sign word aangemaakt
		if(event.getLine(0).equalsIgnoreCase("[teleporter]") && !event.getLine(1).isEmpty()) {
			event.setLine(0, ChatColor.GREEN + "[Teleporter]");
			//Check of inderdaad de spawn locatie bestaat.
			if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + event.getLine(1) + "'") != true) {
				event.setLine(1, ChatColor.DARK_RED + "Not found!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action act = event.getAction();
		Player player = event.getPlayer();
		
		if(act == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Teleporter]")) {
					//Checkt of locatie nog bestaat bij interactie van de sign!
					//Als hij niet bestaat verander de tekst dan kan de functie eronder hem afkappen
					if (valueExists("SELECT * FROM " + plugin.ltable + " WHERE Name='" + sign.getLine(1) + "'") != true) {
						sign.setLine(1, ChatColor.DARK_RED + "Not found!");
						sign.update();
					}
					
					//Check of de sign tekst 'Not found!' staat daarna afkappen
					if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("Not found!")) {
						player.sendMessage(ChatColor.RED + "De sign die je klikt heeft geen locatie toegewezen!");
						return;
					}
					
					//Hier weet je zeker of hij nog bestaat haal gegevens op en teleporteer de gebruiker naar de betreffende locatie!
					ResultSet select = select("SELECT * FROM " + plugin.ltable + " WHERE Name='" + sign.getLine(1) + "'");
					try {
						select.next();
						//Check of de wereld nog bestaat voordat hem ernaar toe sturen
						if (Bukkit.getWorld(select.getString("world")) == null) {
							player.sendMessage(ChatColor.RED + "De wereld waarin deze locatie staat bestaat niet meer of is hernoemt!");
							return;
						}
						player.teleport(new Location(Bukkit.getWorld(select.getString("world")), select.getDouble("X"), select.getDouble("Y"), select.getDouble("Z"), (float) select.getDouble("Yaw"), (float) select.getDouble("Pitch")));
						player.sendMessage(ChatColor.GREEN + "Teleporting... ");
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					
					
					
				}
			}
		}
		

		//Left / Right click to open kit compass
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS && event.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && ChatColor.stripColor(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName()).equalsIgnoreCase("kit selector")) {
			Inventory inv = Bukkit.createInventory(null, 27, (ChatColor.GOLD + "Kit Selector"));
			
			//Voor elke kit laad de icon in + displayname en voeg toe aan inventory
			for(KitClass kitC : plugin.kits) {
				
				ItemStack stack = kitC.getIcon();
				ItemMeta stackm = stack.getItemMeta();
				stackm.setDisplayName(ChatColor.translateAlternateColorCodes('&', kitC.getDisplayName()));
				stack.setItemMeta(stackm);
				
				inv.addItem(stack);
			}
			
			event.getPlayer().openInventory(inv);
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && ChatColor.stripColor(event.getClickedInventory().getName()).equalsIgnoreCase("kit selector")) {
			if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
				KitClass kit = getKitFromDisplayName(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
				if (kit != null) {
					Bukkit.getServer().dispatchCommand(event.getWhoClicked(), "kit " + kit.getName());
				}else {
					event.getWhoClicked().sendMessage(ChatColor.RED + "Het lijkt erop de kit die je probeerd te klikken niet langer bestaat!");
				}
				event.getWhoClicked().closeInventory();
			}
			event.setCancelled(true);
		}else if (event.isShiftClick() && event.getInventory() != null && event.getInventory().getName() != null && ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("kit selector")) {
			event.setCancelled(true);
		}

	}
	
	//Ter vekomen dat de inventory op een andere manier word aangeraak door bvb items te kunnen toevoegen
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryInteractEvent(InventoryInteractEvent event) {
		if (event.getInventory() != null && event.getInventory().getName() != null && ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("kit selector")) {
			event.setCancelled(true);
		}
	}
	
	//Ter vekomen dat de inventory op een andere manier word aangeraak door bvb items te kunnen toevoegen
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (event.getDestination() != null && event.getDestination().getName() != null && ChatColor.stripColor(event.getDestination().getName()).equalsIgnoreCase("kit selector")) {
			event.setCancelled(true);
		}
	}
	
	//Ter vekomen dat de inventory op een andere manier word aangeraak door bvb items te kunnen toevoegen
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent event) {
		if (event.getInventory() != null && event.getInventory().getName() != null && ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("kit selector")) {
			event.setCancelled(true);
		}
	}
	
	//Ter vekomen dat de inventory op een andere manier word aangeraak door bvb items te kunnen toevoegen
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
		if (event.getInventory() != null && event.getInventory().getName() != null && ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("kit selector")) {
			event.setCancelled(true);
		}
	}
	
	public KitClass getKitFromDisplayName(String name) {
		for (KitClass C : plugin.kits) {
			if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', C.getDisplayName())).equalsIgnoreCase(name)) {
				return C;
			}
		}
		return null;
	}
	
	public void updateScoreboard(Player player) {
		try {
			ResultSet result = select("SELECT * FROM " + plugin.ptable + " WHERE UUID='" + player.getUniqueId().toString() +  "'");
			result.next();
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard board = manager.getNewScoreboard();
			@SuppressWarnings("deprecation")
			Objective objective = board.registerNewObjective("test", "dummy");
			
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.GOLD + "DDG Kitpvp");
			
			Score Score_killdeath = objective.getScore(ChatColor.GOLD + "K/D: " + ChatColor.YELLOW + String.format("%.2f", ((double) (result.getInt("Kills") + 1) / (double) (result.getInt("Deaths") + 1))));
			Score_killdeath.setScore(0);
			
			
			player.setScoreboard(board);
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
		
	}

}
