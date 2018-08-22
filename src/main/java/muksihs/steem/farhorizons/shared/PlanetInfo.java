package muksihs.steem.farhorizons.shared;

import java.util.ArrayList;
import java.util.List;

public class PlanetInfo {
	StarSystem starSystem;
	int planetNo;
	private String name;
	private boolean isColony;
	private String lsn;
	private final List<String> inventory=new ArrayList<>();

	public StarSystem getStarSystem() {
		return starSystem;
	}

	public void setStarSystem(StarSystem starSystem) {
		this.starSystem = starSystem;
	}

	public int getPlanetNo() {
		return planetNo;
	}

	public void setPlanetNo(int planetNo) {
		this.planetNo = planetNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isColony() {
		return isColony;
	}

	public void setColony(boolean isColony) {
		this.isColony = isColony;
	}

	public String getLsn() {
		return lsn;
	}

	public void setLsn(String lsn) {
		this.lsn = lsn;
	}

	public List<String> getInventory() {
		return inventory;
	}
}