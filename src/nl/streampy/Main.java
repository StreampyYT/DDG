package nl.streampy;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import nl.streampy.commands.Kit;
import nl.streampy.commands.Spawns;
import nl.streampy.library.EventsHandler;
import nl.streampy.library.KitClass;

public class Main extends JavaPlugin {
	private Connection conn;
	public String host, database, user, pass, ptable, ltable;
	public int port;
	public Location spawnLocation;
	public ArrayList<KitClass> kits = new ArrayList<KitClass>();
	private File map = new File("plugins/DDG");
	private File kitFile = new File("plugins/DDG/kits.yml");
	private FileConfiguration kitConfig = new YamlConfiguration();

	public void onEnable() {
		new EventsHandler(this);
		loadConfig();
		mysqlSetup();
		createTable();
		load();
		//Zet de main spawn/respawn locatie dan hoeft de server niet elke keer database te benaderen als hij de locatie nodig heeft!
		try {
			PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + ltable + " WHERE Main=" + 1);
			
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				PreparedStatement statement2 = getConnection().prepareStatement("SELECT * FROM " + ltable + " WHERE Main=" + 1);
				ResultSet select = statement2.executeQuery();
				select.next();
				spawnLocation = new Location(Bukkit.getWorld(select.getString("world")), select.getDouble("X"), select.getDouble("Y"), select.getDouble("Z"), (float) select.getDouble("Yaw"), (float) select.getDouble("Pitch"));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	
		
		getCommand("spawns").setExecutor(new Spawns(this));
		getCommand("kit").setExecutor(new Kit(this));
	}
	
	public void onDisable() {
		save();
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void createTable() {
		try {
			DatabaseMetaData dbm = conn.getMetaData();
		    ResultSet presults = dbm.getTables(null, null, ptable, null);
			
		    if(!presults.next()) {
				PreparedStatement pcreate = getConnection().prepareStatement("CREATE TABLE " + ptable + " (UUID VARCHAR(255) PRIMARY KEY NOT NULL, Name VARCHAR(40), Kills INT, Deaths INT)");
				
				pcreate.executeUpdate();
				Bukkit.getLogger().info("Created the table " + ptable + " because it didn't exists!");
			}	
		    
		    ResultSet lresults = dbm.getTables(null, null, ltable, null);
			
		    if(!lresults.next()) {
				PreparedStatement lcreate = getConnection().prepareStatement("CREATE TABLE " + ltable + " (Name VARCHAR(255) PRIMARY KEY NOT NULL, world VARCHAR(255), X Double, Y Double, Z Double, Yaw Double, Pitch Double, Main Boolean)");
				
				lcreate.executeUpdate();
				Bukkit.getLogger().info("Created the table " + ltable + " because it didn't exists!");
			}
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void mysqlSetup() {
		host = this.getConfig().getString("host");
		port = this.getConfig().getInt("port");
		database = this.getConfig().getString("database");
		user = this.getConfig().getString("user");
		pass = this.getConfig().getString("pass");
		ptable = this.getConfig().getString("playertable");
		ltable = this.getConfig().getString("locationtable");
		
		try {
			synchronized(this) {
				if(getConnection() != null && !getConnection().isClosed()) {
					return;
				}
				
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.pass));
				Bukkit.getLogger().info("Connected to Database!");
			}
		}catch(SQLException ex) {
			ex.printStackTrace();
		}catch(ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void setConnection(Connection connection) {
		this.conn = connection;
	}
	
	public void load() {
		//Maak de betreffende files aan bij inladen.
		if (!map.exists()) {
			map.mkdir();
		}
		
		if (!kitFile.exists()) {
			try {
				kitFile.createNewFile();
			}catch(Exception ex) { ex.printStackTrace(); }
		}
		
		if (kitFile.exists()) {
			try {
				kitConfig.load(kitFile);
				for (int i = 0; kitConfig.contains("kit." + i); i++) {
					//Basis toevoegen
					String name = kitConfig.getString("kit." + i + ".name");
					String displayname = kitConfig.getString("kit." + i + ".displayname");
					ItemStack icon = loadItemData("kit." + i + ".icon");
					
					//Armor
					ArrayList<ItemStack> armor = new ArrayList<ItemStack>();
					armor.add(kitConfig.contains("kit." + i + ".armor.boots") ? loadItemData("kit." + i + ".armor.boots") : null);
					armor.add(kitConfig.contains("kit." + i + ".armor.leggings") ? loadItemData("kit." + i + ".armor.leggings") : null);
					armor.add(kitConfig.contains("kit." + i + ".armor.chestplate") ? loadItemData("kit." + i + ".armor.chestplate") : null);
					armor.add(kitConfig.contains("kit." + i + ".armor.helmet") ? loadItemData("kit." + i + ".armor.helmet") : null);

					//Inventory inladen
					ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
					for (int b = 0; b < 36; b++) {
						ItemStack item = null;
						if (kitConfig.contains("kit." + i + ".items." + b)) {
							item = loadItemData("kit." + i + ".items." + b);
						}
						inv.add(b, item);
					}
					
					//Maak nieuwe kit aan 
					KitClass K = new KitClass(name, icon, displayname, inv, armor);
					kits.add(K);
				}
			}catch(Exception ex) { ex.printStackTrace(); }
		}
	}
	
	public void save() {
		//Maak de betreffende files aan bij uitladen.
		//Deze is ter controle dat niet de map is verwijdert toen de server nog aanstond. (Na controlen)
		if (!map.exists()) {
			map.mkdir();
		}
		
		if (!kitFile.exists()) {
			try {
				kitFile.createNewFile();
			}catch(Exception ex) { ex.printStackTrace(); }
		}
		
		//Als het hierboven fout gaat dan kan hij hem niet saven dus nog een controle!
		if (kitFile.exists()) {
			try {
				//Loop alle kits door
				kitConfig.load(kitFile);
				kitConfig.set("kit", null);
				kitConfig.save(kitFile);
				kitConfig.load(kitFile);
				
				for (int i = 0; i < kits.size(); i++) {
					KitClass kit = kits.get(i);
					kitConfig.set("kit." + i + ".name", kit.getName());
					kitConfig.set("kit." + i + ".displayname", kit.getDisplayName());
					saveItemData(kit.getIcon(), "kit." + i + ".icon");
					
					kitConfig.save(kitFile);
					//ARMOR
					if (kit.armor.get(0) != null) {
						saveItemData(kit.armor.get(0), "kit." + i + ".armor.boots");
					}
					if (kit.armor.get(1) != null) {
						saveItemData(kit.armor.get(1), "kit." + i + ".armor.leggings");
					}
					if (kit.armor.get(2) != null) {
						saveItemData(kit.armor.get(2), "kit." + i + ".armor.chestplate");
					}
					if (kit.armor.get(3) != null) {
						saveItemData(kit.armor.get(3), "kit." + i + ".armor.helmet");
					}
					
					//INVENTORY
					for (int b = 0; b < kit.inv.size(); b++) {
						ItemStack item = kit.inv.get(b);
						if (item != null) {
							saveItemData(item, "kit." + i + ".items." + b);
						}
					}
					
					
				}
			}catch(Exception ex) { ex.printStackTrace(); }
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void saveItemData(ItemStack item, String label) {
		try {
			//laad de file in
			kitConfig.load(kitFile);
			//Saved de standaard gegevens die er altijd zullen zijn bij een itemstack
			kitConfig.set(label + ".type", item.getType().toString());
			kitConfig.set(label + ".amount", item.getAmount());
			//Kijk of er metadata aanwezig is bij de itemstack
			if (item.hasItemMeta()) {
				//Kijk of er displayname meta is
				kitConfig.set(label + ".itemMeta.displayName", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName().toString() : null);
				
				//Check of er nog lores zijn
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					for (int c = 0; c < lore.size(); c++) {
						kitConfig.set(label + ".itemMeta.lore" + c, lore.get(c));
					}
				}
				
				//Checkt nog voor enchantments
				if (item.getItemMeta().hasEnchants()) {
					Map<Enchantment, Integer> ench = item.getItemMeta().getEnchants();
					int d = 0;
					for (Entry<Enchantment, Integer> entry : ench.entrySet()) {
						kitConfig.set(label + ".itemMeta.enchants." + d + ".type", entry.getKey().getName().toString());
						kitConfig.set(label + ".itemMeta.enchants." + d + ".level", entry.getValue());
						
						d++;
					}
				}
				
			}
			//saved de file nadat hij klaar is met dit item
			kitConfig.save(kitFile);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack loadItemData(String label) {
		try {
			//Laad het bestand in 
			kitConfig.load(kitFile);
			
			//Kijkt of de material wel bestaat van het item
			if (Material.getMaterial(kitConfig.getString(label + ".type")) == null) {
				Bukkit.getLogger().info("Could not find item: " + kitConfig.getString(label + ".type"));
				return null;
			}
			//Maak nieuwe itemstack aan met basis gegevens & maak direct metadata aan 
			ItemStack item = new ItemStack(Material.getMaterial(kitConfig.getString(label + ".type")), kitConfig.getInt(label + ".amount"));
			ItemMeta meta = item.getItemMeta();
			
			//Check of er metadata is opgeslagen
			if (kitConfig.contains(label + ".itemMeta")) {
				//Kijkt of displayname is gezet zoja toevoegen aan meta
				if (kitConfig.contains(label + ".itemMeta.displayName")) { meta.setDisplayName(kitConfig.getString(label + ".itemMeta.displayName")); }
				
				//Kijken of er nog lores zijn zoja toevoegen aan meta
				if (kitConfig.contains(label + ".itemMeta.lore")) {
					ArrayList<String> lore = new ArrayList<String>();
					for (int c = 0; kitConfig.contains(label + ".itemMeta.lore" + c); c++) {
						lore.add(kitConfig.getString(label + ".itemMeta.lore" + c));
					}
					meta.setLore(lore);
				}
			
				//Check of er enchantments zijn gesaved zoja toevoegen aan meta
				if (kitConfig.contains(label + ".itemMeta.enchants")) {
					for (int d = 0; kitConfig.contains(label + ".itemMeta.enchants." + d); d++) {
						meta.addEnchant(Enchantment.getByName(kitConfig.getString(label + ".itemMeta.enchants." + d + ".type")), kitConfig.getInt(label + ".itemMeta.enchants." + d + ".level"), true);
					}
				}
			}
			//Zet itemmeta op itemstack
			item.setItemMeta(meta);
			
			//stuur itemstack terug
			return item;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
}
