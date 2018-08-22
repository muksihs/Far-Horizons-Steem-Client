package muksihs.steem.farhorizons.shared;

import java.util.ArrayList;
import java.util.List;

public class PlanetDetails {
	private String planet;
	private String lsn;
	private List<String> inventory;
	private List<String> ships;
	public PlanetDetails() {
		planet = "";
		lsn = "";
		inventory = new ArrayList<>();
		ships = new ArrayList<>();
	}
	
	public PlanetDetails(String planet) {
		this();
		setPlanet(planet);
	}

	public List<String> getInventory() {
		return inventory;
	}

	public void setInventory(List<String> inventory) {
		this.inventory = inventory;
	}

	public String getPlanet() {
		return planet;
	}

	public void setPlanet(String planet) {
		this.planet = planet;
	}

	public String getLsn() {
		return lsn;
	}

	public void setLsn(String lsn) {
		this.lsn = lsn;
	}

	public List<String> getShips() {
		return ships;
	}

	public void setShips(List<String> ships) {
		this.ships = ships;
	}
}
