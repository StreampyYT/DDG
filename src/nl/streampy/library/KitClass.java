package nl.streampy.library;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class KitClass {

	public String name;
	public String displayname;
	public ItemStack icon;
	public ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
	public ArrayList<ItemStack> armor = new ArrayList<ItemStack>();
	
	public KitClass(String name, ItemStack icon, Player player) {
		this.name = name;
		this.displayname = name;
		this.icon = icon;	
		setInventory(player.getInventory());
		//Ik geef de kit terug aan de speler. Wegens tijdens het testen als je /clear deed de kit ook werd gereset en dit was de oplossing voor het probleem.
		givePlayer(player);
	}
	
	public KitClass(String name, ItemStack icon, String displayname, ArrayList<ItemStack> inv, ArrayList<ItemStack> armor) {
		this.name = name;
		this.displayname = displayname;
		this.icon = icon;	
		this.inv = inv;
		this.armor = armor;
	}
	
	//Gets
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayname;
	}
	
	public ItemStack getIcon() {
		return icon;
	}
	
	//Sets
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDisplayName(String name) {
		this.displayname = name;
	}
	
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}
	
	public void setInventory(PlayerInventory inv) {
		//Dit vond ik de makkelijkste manier om een inventory op te slaan
		//Kijkt hoe groot de inventory is & daarna maakt hij een nieuw item met de metadata
		//Als het anders word gedaan saved hij de metadata niet
		for (int a = 0; a < inv.getSize(); a++) {
			ItemStack item = inv.getItem(a);
			if (inv.getItem(a) != null && inv.getItem(a).hasItemMeta()) {
				ItemMeta meta = inv.getItem(a).getItemMeta();
				item.setItemMeta(meta);
			}
			this.inv.add(a, item);
		}
		//Dit is een arraylist van 4 items max dit is voor de armor
		//maakt hij een nieuw item met de metadata
		//Ookal zal het item null zijn zal hij een leeg item erin voegen en word de space als nog gebruikt hierdoor is hij altijd 4 groot
		ItemStack item = inv.getBoots();
		if (inv.getBoots() != null && inv.getBoots().hasItemMeta()) {
			ItemMeta meta2 = inv.getBoots().getItemMeta();
			item.setItemMeta(meta2);
		}
		this.armor.add(0, item);
		
		item = inv.getLeggings();
		if (inv.getLeggings() != null && inv.getLeggings().hasItemMeta()) {
			ItemMeta meta3 = inv.getLeggings().getItemMeta();
			item.setItemMeta(meta3);
		}
		this.armor.add(1, item);
		
		item = inv.getChestplate();
		if (inv.getChestplate() != null && inv.getChestplate().hasItemMeta()) {
			ItemMeta meta4 = inv.getChestplate().getItemMeta();
			item.setItemMeta(meta4);
		}
		this.armor.add(2, item);
		
		item = inv.getHelmet();
		if (inv.getHelmet() != null && inv.getHelmet().hasItemMeta()) {
			ItemMeta meta5 = inv.getHelmet().getItemMeta();
			item.setItemMeta(meta5);
		}
		this.armor.add(3, item);

	}
	
	//Extra
	public void givePlayer(Player player) {
		//Clear doet alleen bovenste gedeelte van inventory en dus niet de armor!
		player.getInventory().clear();
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		
		//Hier pakt hij de inventory die op is geslagen maakt een nieuw item ervan en voeg deze toe aan het inventory van de speler.
		for (int a = 0; a < inv.size(); a++) {
			if (inv.get(a) != null) {
				
				@SuppressWarnings("deprecation")
				ItemStack item = new ItemStack(inv.get(a).getType(), inv.get(a).getAmount(), inv.get(a).getData().getData());
				if (inv.get(a).hasItemMeta())
					item.setItemMeta(inv.get(a).getItemMeta());
				
				player.getInventory().setItem(a, item);
			}
		}
		
		//Pakt uit de altijd 4 grote arraylist de juiste item voor de juiste slot
		//Check of het armorstuk niet null is want dan hoeft hij niks neer te zetten.
		if (armor.get(0) != null) 
			player.getInventory().setBoots(armor.get(0));
		if (armor.get(1) != null) 
			player.getInventory().setLeggings(armor.get(1));
		if (armor.get(2) != null) 
			player.getInventory().setChestplate(armor.get(2));
		if (armor.get(3) != null) 
			player.getInventory().setHelmet(armor.get(3));
		
		//Update de inventory
		player.updateInventory();
	}
	
}
