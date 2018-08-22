package muksihs.steem.farhorizons.client;

import java.util.ArrayList;
import java.util.List;

import muksihs.steem.farhorizons.shared.PlanetInfo;
import muksihs.steem.farhorizons.shared.ShipLocation;

public class GameStats {
	private String name;
	private String govName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGovName() {
		return govName;
	}
	public void setGovName(String govName) {
		this.govName = govName;
	}
	public String getGovType() {
		return govType;
	}
	public void setGovType(String govType) {
		this.govType = govType;
	}
	public GameStats.TechLevels getTechLevels() {
		return techLevels;
	}
	public void setTechLevels(GameStats.TechLevels techLevels) {
		this.techLevels = techLevels;
	}
	private String govType;
	private GameStats.TechLevels techLevels;
	private List<PlanetInfo> planetInfo=new ArrayList<>();
	private List<ScanInfo> scannedPlanets=new ArrayList<>();
	private List<ShipLocation> shipLocations;
	public List<ShipLocation> getShipLocations() {
		return shipLocations;
	}
	public List<PlanetInfo> getPlanetInfo() {
		return planetInfo;
	}
	public static class TechLevels {
		private int mining;
		private int manufacturing;
		private int military;
		private int gravitics;
		public int getMining() {
			return mining;
		}
		public void setMining(int mining) {
			this.mining = mining;
		}
		public int getManufacturing() {
			return manufacturing;
		}
		public void setManufacturing(int manufacturing) {
			this.manufacturing = manufacturing;
		}
		public int getMilitary() {
			return military;
		}
		public void setMilitary(int military) {
			this.military = military;
		}
		public int getGravitics() {
			return gravitics;
		}
		public void setGravitics(int gravitics) {
			this.gravitics = gravitics;
		}
		public int getLifeSupport() {
			return lifeSupport;
		}
		public void setLifeSupport(int lifeSupport) {
			this.lifeSupport = lifeSupport;
		}
		public int getBiology() {
			return biology;
		}
		public void setBiology(int biology) {
			this.biology = biology;
		}
		private int lifeSupport;
		private int biology;
	}
	public static enum TechLevelNames {
		Mining("MI"), Manufacturing("MA"), Military("ML"), Gravitics("GV"), LifeSupport("LS"), Biology("BI");
		private final String abbr;
		private TechLevelNames(String abbreviation) {
			this.abbr=abbreviation;
		}
		public String getAbbr() {
			return abbr;
		}
	}
	public void setPlanetInfo(List<PlanetInfo> planetInfo) {
		this.planetInfo=planetInfo;
	}
	
	public static class ScanInfo {
		private String x="0";
		private String y="0";
		private String z="0";
		private String noPlanets="0";
		private String star="Empty space";
		private List<PlanetScan> planets=new ArrayList<>();
		private String ship;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (x != null) {
				builder.append("x=");
				builder.append(x);
				builder.append(", ");
			}
			if (y != null) {
				builder.append("y=");
				builder.append(y);
				builder.append(", ");
			}
			if (z != null) {
				builder.append("z=");
				builder.append(z);
				builder.append(", ");
			}
			if (noPlanets != null) {
				builder.append("noPlanets=");
				builder.append(noPlanets);
				builder.append(", ");
			}
			if (star != null) {
				builder.append("star=");
				builder.append(star);
				builder.append(", ");
			}
			if (ship != null) {
				builder.append("ship=");
				builder.append(ship);
			}
			if (planets != null) {
				builder.append("\nPLANETS:\n");
				for (PlanetScan planet: planets) {
					builder.append(planet.toString());
					builder.append("\n");
				}
			}
			return builder.toString();
		}
		public String getShip() {
			return ship;
		}
		public String getX() {
			return x;
		}
		public void setX(String x) {
			this.x = x;
		}
		public String getY() {
			return y;
		}
		public void setY(String y) {
			this.y = y;
		}
		public String getZ() {
			return z;
		}
		public void setZ(String z) {
			this.z = z;
		}
		public String getNoPlanets() {
			return noPlanets;
		}
		public void setNoPlanets(String noPlanets) {
			this.noPlanets = noPlanets;
		}
		public String getStar() {
			return star;
		}
		public void setStar(String star) {
			this.star = star;
		}
		public List<PlanetScan> getPlanets() {
			return planets;
		}
		public void setPlanets(List<PlanetScan> planets) {
			this.planets = planets;
		}
		public void setShip(String ship) {
			this.ship=ship;
		}
	}
	
	public static class PlanetScan {
		private boolean colonizable;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PlanetScan [");
			if (id != null) {
				builder.append("id=");
				builder.append(id);
				builder.append(", ");
			}
			if (diameter != null) {
				builder.append("diameter=");
				builder.append(diameter);
				builder.append(", ");
			}
			if (gravity != null) {
				builder.append("gravity=");
				builder.append(gravity);
				builder.append(", ");
			}
			if (tempuratureClassification != null) {
				builder.append("tempuratureClassification=");
				builder.append(tempuratureClassification);
				builder.append(", ");
			}
			if (pressureClassification != null) {
				builder.append("pressureClassification=");
				builder.append(pressureClassification);
				builder.append(", ");
			}
			if (miningDifficulty != null) {
				builder.append("miningDifficulty=");
				builder.append(miningDifficulty);
				builder.append(", ");
			}
			if (lifeSupportNumber != null) {
				builder.append("lifeSupportNumber=");
				builder.append(lifeSupportNumber);
				builder.append(", ");
			}
			if (atmosphere != null) {
				builder.append("atmosphere=");
				builder.append(atmosphere);
			}
			builder.append("]");
			return builder.toString();
		}
		private String id="0";
		private String diameter="0";
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDiameter() {
			return diameter;
		}
		public void setDiameter(String diameter) {
			this.diameter = diameter;
		}
		public String getGravity() {
			return gravity;
		}
		public void setGravity(String gravity) {
			this.gravity = gravity;
		}
		public String getTempuratureClassification() {
			return tempuratureClassification;
		}
		public void setTempuratureClassification(String tempuratureClassification) {
			this.tempuratureClassification = tempuratureClassification;
		}
		public String getPressureClassification() {
			return pressureClassification;
		}
		public void setPressureClassification(String pressureClassification) {
			this.pressureClassification = pressureClassification;
		}
		public String getMiningDifficulty() {
			return miningDifficulty;
		}
		public void setMiningDifficulty(String miningDifficulty) {
			this.miningDifficulty = miningDifficulty;
		}
		public String getLifeSupportNumber() {
			return lifeSupportNumber;
		}
		public void setLifeSupportNumber(String lifeSupportNumber) {
			this.lifeSupportNumber = lifeSupportNumber;
		}
		public String getAtmosphere() {
			return atmosphere;
		}
		public void setAtmosphere(String atmosphere) {
			this.atmosphere = atmosphere;
		}
		public boolean isColonizable() {
			return colonizable;
		}
		public void setColonizable(boolean colonizable) {
			this.colonizable = colonizable;
		}
		private String gravity="0.00";
		private String tempuratureClassification="0";
		private String pressureClassification="0";
		private String miningDifficulty="0.00";
		private String lifeSupportNumber="0";
		private String atmosphere="Vacuum";
		private String name=null;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name=name;
		}
	}

	public List<ScanInfo> getScannedPlanets() {
		return scannedPlanets;
	}
	public void setScannedPlanets(List<ScanInfo> scannedPlanets) {
		this.scannedPlanets = scannedPlanets;
	}
	public void setShipLocations(List<ShipLocation> parseShips) {
		this.shipLocations=parseShips;
	}
}