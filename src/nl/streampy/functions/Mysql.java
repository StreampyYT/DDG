package nl.streampy.functions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import nl.streampy.Main;

public class Mysql {

	Main plugin = (Main) Main.getPlugin(Main.class);
	
	public boolean valueExists(String query) {
		try {
			PreparedStatement statement = plugin.getConnection().prepareStatement(query);
			
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				return true;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public void createPlayer(Player player) {
			//Check of speler al bestaat zo niet maak nieuwe aan
			if (valueExists("SELECT * FROM " + plugin.ptable + " WHERE UUID='" + player.getUniqueId().toString() + "'") != true) {
				update("INSERT INTO " + plugin.ptable + " (UUID, Name, Kills, Deaths) VALUE ('" + player.getUniqueId().toString() + "','" + player.getName() + "', 0, 0)");
			}
		
	}
	
	//Stuur makkelijk een SQL INSERT Query naar Database
	public void update(String query) {
		try {
			//Verstuur query
			PreparedStatement statement = plugin.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	//Maak een select statement naar de database en geeft waardes terug
	public ResultSet select(String query) {
		try {
			//Verstuur query
			PreparedStatement statement = plugin.getConnection().prepareStatement(query);
			ResultSet result = statement.executeQuery();
			return result;
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
}
