package muksihs.steem.farhorizons.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;

import muksihs.steem.farhorizons.client.GameStats;
import muksihs.steem.farhorizons.client.GameStats.PlanetScan;
import muksihs.steem.farhorizons.client.GameStats.ScanInfo;
import muksihs.steem.farhorizons.client.GameStats.TechLevels;

public class ExtractDetailedGameInfo {
	public static final String DIVIDER = "* * * * * * * * * * * * * * * * * * * * * * * * *";

	public static GameStats parse(String report) {
		if (isBlank(report)) {
			return new GameStats();
		}
		report = report.replace("\t", " ");
		GameStats gameStats = parseSpeciesInformation(report);
		gameStats.setPlanetInfo(parseNamedPlanets(report));
		gameStats.setScannedPlanets(parseScannedPlanets(report));
		gameStats.setTechLevels(parseTechLevels(report));
		gameStats.setShipLocations(parseShips(report));

		Map<String, String> namedPlanets = new HashMap<>();
		for (PlanetInfo namedPlanet : gameStats.getPlanetInfo()) {
			StarSystem s = namedPlanet.starSystem;
			String key = s.getX() + "," + s.getY() + "," + s.getZ() + "," + namedPlanet.planetNo;
			namedPlanets.put(key, namedPlanet.getName());
		}

		int lifeSupport = gameStats.getTechLevels().getLifeSupport();
		for (ScanInfo system : gameStats.getScannedPlanets()) {
			String skey = system.getX() + "," + system.getY() + "," + system.getZ() + ",";
			for (PlanetScan planet : system.getPlanets()) {
				String pkey = skey + planet.getId();
				planet.setName(namedPlanets.get(pkey));
				Integer planetLsn;
				try {
					planetLsn = Integer.valueOf(planet.getLifeSupportNumber());
				} catch (NumberFormatException e) {
					continue;
				}
				planet.setColonizable(planetLsn <= lifeSupport);
			}
		}
		return gameStats;
	}

	private static List<ShipLocation> parseShips(String report) {
		List<ShipLocation> ships = new ArrayList<>();
		report = StringUtils.substringAfter(report, DIVIDER);
		report = StringUtils.substringBefore(report, "ORDER SECTION.");
		ListIterator<String> iLines = Arrays.asList(StringUtils.splitPreserveAllTokens(report, "\n")).listIterator();
		String x = "0";
		String y = "0";
		String z = "0";
		String p = "0";
		shipsAt: while (iLines.hasNext()) {
			String line = iLines.next().trim();
			if (isBlank(line)) {
				continue shipsAt;
			}
			if (line.startsWith("Coordinates:") && line.contains("planet number")) {
				line = StringUtils.substringAfter(line, "x =").trim();
				x = StringUtils.substringBefore(line, ",");
				line = StringUtils.substringAfter(line, "y =").trim();
				y = StringUtils.substringBefore(line, ",");
				line = StringUtils.substringAfter(line, "z =").trim();
				z = StringUtils.substringBefore(line, ",");
				p = StringUtils.substringAfter(line, "planet number").trim();

				continue shipsAt;
			}
			if (line.startsWith("Ships at ")) {
				iLines.previous();
				ships.addAll(parseShipsAt(x, y, z, p, iLines));
				continue shipsAt;
			}
			if (line.startsWith("Other planets and ships:")) {
				ships.addAll(parseOtherShip(iLines));
				continue shipsAt;
			}
		}
		return ships;
	}

	private static List<ShipLocation> parseShipsAt(String x, String y, String z, String p,
			ListIterator<String> iLines) {
		List<ShipLocation> ships = new ArrayList<>();
		String planet = iLines.next().trim();
		planet = StringUtils.substringAfter(planet, "Ships at");
		planet = StringUtils.substringBeforeLast(planet, ":").trim();
		if (!iLines.hasNext()) {
			return ships;
		}
		if (!iLines.next().trim().startsWith("Name ")) {
			iLines.previous();
			return ships;
		}
		if (!iLines.next().trim().startsWith("-")) {
			iLines.previous();
			return ships;
		}
		while (iLines.hasNext()) {
			String line = iLines.next().trim();
			if (isBlank(line)) {
				return ships;
			}
			if (line.startsWith("*")) {
				return ships;
			}
			ShipLocation ship = new ShipLocation();
			ship.setPlanet(planet);
			ship.setX(x);
			ship.setY(y);
			ship.SetZ(z);
			ship.setP(p);
			ship.setName(StringUtils.substringBeforeLast(line, "(").trim());
			parseShipStats(line, ship);
			ships.add(ship);
		}
		return ships;
	}

	private static void parseShipStats(String line, ShipLocation ship) {
		if (ship.getName().contains("4")) {
			GWT.log(ship.getName()+": "+line);
		}
		String tmp = StringUtils.substringBetween(line, "(", ")");
		if (!isBlank(tmp)) {
			String[] stats = StringUtils.split(tmp,",");
			if (stats!=null&&stats.length!=0) {
				for (String stat: stats) {
					if (stat.startsWith("A")) {
						ship.setAge(stat.substring(1).trim());
						continue;
					}
					if (stat.startsWith("O")) {
						ship.setP(stat.substring(1).trim());
						continue;
					}
					if (stat.endsWith("ton")||stat.endsWith("tons")) {
						ship.setTons(StringUtils.substringBefore(stat, " ").trim());
						continue;
					}
					if (stat.startsWith("C")) {
						ship.setUnderConstruction(true);
						continue;
					}
					if (stat.startsWith("D")) {
						continue;
					}
				}
			}
		}
		tmp = StringUtils.substringAfterLast(line, ")");
		if (!isBlank(tmp)) {
			tmp=tmp.trim()+" ";
			if (tmp.startsWith(",")) {
				tmp=tmp.substring(1);
			}
			ship.setCapacity(StringUtils.substringBefore(tmp, " ").trim());
			ship.setCargo(StringUtils.substringAfter(tmp, " ").trim());
		}
	}

	private static List<ShipLocation> parseOtherShip(ListIterator<String> iLines) {
		List<ShipLocation> ships = new ArrayList<>();
		if (!iLines.hasNext()) {
			return ships;
		}
		if (!isBlank(iLines.next())) {
			iLines.previous();
			return ships;
		}
		byLocationScan: while (iLines.hasNext()) {
			String planet = "Empty space";
			String x = "0";
			String y = "0";
			String z = "0";
			String p = "0";
			String line = iLines.next().trim();
			if (isBlank(line)) {
				return ships;
			}
			if (line.contains("#") && startsWithAsciiNumeric(line)) {
				x = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();
				y = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();
				z = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, "#").trim();
				p = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();
				planet = StringUtils.substringBefore(line, ",").trim();
				while (iLines.hasNext()) {
					line = iLines.next().trim();
					if (startsWithAsciiNumeric(line)) {
						iLines.previous();
						continue byLocationScan;
					}
					if (isBlank(line)) {
						iLines.previous();
						continue byLocationScan;
					}
					ShipLocation ship = new ShipLocation();
					ship.setPlanet(planet);
					ship.setX(x);
					ship.setY(y);
					ship.SetZ(z);
					ship.setP(p);
					ship.setName(StringUtils.substringBeforeLast(line, "(").trim());
					parseShipStats(line, ship);
					ships.add(ship);
				}
				continue byLocationScan;
			}
			if (startsWithAsciiNumeric(line)) {
				x = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();
				y = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();
				z = StringUtils.substringBefore(line, " ");
				line = StringUtils.substringAfter(line, " ").trim();

				p = "0";
				planet = "Deep Space";

				ShipLocation ship = new ShipLocation();
				ship.setPlanet(planet);
				ship.setX(x);
				ship.setY(y);
				ship.SetZ(z);
				ship.setP(p);
				ship.setName(StringUtils.substringBeforeLast(line, "(").trim());
				parseShipStats("("+StringUtils.substringAfterLast(line, "("), ship);
				ships.add(ship);

				while (iLines.hasNext()) {
					line = iLines.next().trim();
					if (startsWithAsciiNumeric(line)) {
						iLines.previous();
						continue byLocationScan;
					}
					if (isBlank(line)) {
						iLines.previous();
						continue byLocationScan;
					}
					ShipLocation ship2 = new ShipLocation();
					ship2.setPlanet(planet);
					ship2.setX(x);
					ship2.setY(y);
					ship2.SetZ(z);
					ship2.setP(p);
					ship2.setName(StringUtils.substringBeforeLast(line, "(").trim());
					parseShipStats(line, ship2);
					ships.add(ship2);
				}
				continue byLocationScan;
			}
		}
		return ships;
	}

	private static List<ScanInfo> parseScannedPlanets(String report) {
		List<ScanInfo> list = new ArrayList<>();
		if (!report.contains("Scan done by") && !report.contains("Scan of home star system")) {
			return list;
		}
		ListIterator<String> iLines = Arrays.asList(report.split("\n")).listIterator();
		scanResultsLoop: while (iLines.hasNext()) {
			String next = iLines.next();
			if (next.trim().isEmpty()) {
				continue scanResultsLoop;
			}
			String ship = "No Ship";
			String x = "0";
			String y = "0";
			String z = "0";
			String star = "";
			String planets = "0";
			boolean startFound = false;
			if (next.startsWith("Scan done by")) {
				ship = StringUtils.substringBetween(next, "Scan done by", ":").trim();
				startFound = true;
			}
			if (next.startsWith("Scan of home star system for")) {
				ship = "No Ship";
				startFound = true;
			}
			if (!startFound) {
				continue scanResultsLoop;
			}
			ScanInfo info = new ScanInfo();
			info.setShip(ship);
			starCoordinatesScan: while (iLines.hasNext()) {
				String line = iLines.next().trim();
				if (line.startsWith("Scan Report: There is no star system at")) {
					continue scanResultsLoop;
				}
				if (!line.startsWith("Coordinates:")) {
					continue starCoordinatesScan;
				}
				x = line.replaceAll(".*x = (\\d+).*", "$1");
				y = line.replaceAll(".*y = (\\d+).*", "$1");
				z = line.replaceAll(".*z = (\\d+).*", "$1");
				star = line.replaceAll(".*stellar type =\\s+([^\\s]+).*", "$1");
				planets = line.replaceAll(".*\\d+\\s*planets.*", "$1");
				info.setX(x);
				info.setY(y);
				info.setZ(z);
				info.setStar(star);
				info.setNoPlanets(planets);
				break;
			}
			list.add(info);
			startOfPlanetsSearch: while (iLines.hasNext()) {
				String line = iLines.next().trim();
				if (!line.startsWith("---")) {
					continue startOfPlanetsSearch;
				}
				break startOfPlanetsSearch;
			}
			planetScan: while (iLines.hasNext()) {
				String line = iLines.next().trim();
				if (isBlank(line)) {
					continue planetScan;
				}
				if (!StringUtils.startsWithAny(line, "1", "2", "3", "4", "5", "6", "7", "8", "9")) {
					iLines.previous();
					break planetScan;
				}
				String number = line.replaceAll("^(\\d+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String diameter = line.replaceAll("(\\d+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String gravity = line.replaceAll("([\\d\\.]+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String tempClass = line.replaceAll("(\\d+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String pressClass = line.replaceAll("(\\d+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String mining = line.replaceAll("([\\d\\.]+).*", "$1");
				line = StringUtils.substringAfter(line, " ").trim();
				String lsn = line.replaceAll("(\\d+).*", "$1");
				String atmosphere = StringUtils.substringAfter(line, " ").trim();
				PlanetScan planet = new PlanetScan();
				planet.setAtmosphere(atmosphere);
				planet.setDiameter(diameter);
				planet.setGravity(gravity);
				planet.setId(number);
				planet.setLifeSupportNumber(lsn);
				planet.setMiningDifficulty(mining);
				planet.setPressureClassification(pressClass);
				planet.setTempuratureClassification(tempClass);
				info.getPlanets().add(planet);
				continue planetScan;
			}
		}
		return list;
	}

	private static boolean isBlank(String line) {
		return line == null || line.trim().isEmpty();
	}

	private static List<PlanetInfo> parseNamedPlanets(String report) {
		List<PlanetInfo> planets = new ArrayList<>();
		String tmp;

		/**
		 * parse chunks for Planet STATUS results
		 */
		tmp = StringUtils.substringAfter(report, DIVIDER);
		if (tmp.contains(" PLANET:") || tmp.contains(" COLONY:")) {
			
			ListIterator<String> iLines = Arrays.asList(StringUtils.splitPreserveAllTokens(tmp, "\n")).listIterator();
			planetScan: while (iLines.hasNext()) {
				String line = iLines.next().replace("\t", " ").trim();
				if (!iLines.hasNext()) {
					break planetScan;
				}
				if (!line.contains(": ")) {
					continue planetScan;
				}
				if (!tmp.contains(" PLANET:") && !tmp.contains(" COLONY:")) {
					continue planetScan;
				}
				String planet = StringUtils.substringAfter(line, ":").trim();
				line = iLines.next().replace("\t", " ").trim();
				if (!line.startsWith("Coordinates:") || !line.contains("planet number")) {
					iLines.previous();
					continue planetScan;
				}
				line = StringUtils.substringAfter(line, "x =").trim();
				String x = StringUtils.substringBefore(line, ",");
				line = StringUtils.substringAfter(line, "y =").trim();
				String y = StringUtils.substringBefore(line, ",");
				line = StringUtils.substringAfter(line, "z =").trim();
				String z = StringUtils.substringBefore(line, ",");
				String p = StringUtils.substringAfter(line, "planet number").trim();
				
				PlanetInfo planetInfo = new PlanetInfo();
				planets.add(planetInfo);
				
				StarSystem starSystem = new StarSystem();
				starSystem.setX(parseInteger(x));
				starSystem.setY(parseInteger(y));
				starSystem.setZ(parseInteger(z));
				
				planetInfo.setStarSystem(starSystem);
				planetInfo.setPlanetNo(parseInteger(p));
				planetInfo.setName(planet);
				planetInfo.setColony(true);
				
				planetInfo.setLsn("");
				GWT.log("DETAILS: "+planetInfo.getName());
				detailScan: while (iLines.hasNext()) {
					line = iLines.next().replace("\t", " ").trim();
					if (!iLines.hasNext()) {
						break planetScan;
					}
					if (line.contains(" PLANET:") || line.contains(" COLONY:")) {
						iLines.previous();
						continue planetScan;
					}
					if (line.contains("(LSN =")) {
						String lsn = StringUtils.substringAfter(line, "(LSN =");
						lsn = StringUtils.substringBefore(lsn, ")").trim();
						planetInfo.setLsn(lsn);
						GWT.log(" -- LSN: "+lsn);
						continue detailScan;
					}
					if (line.startsWith("Planetary inventory:")) {
						List<String> inventory = planetInfo.getInventory();
						scanInventory: while (iLines.hasNext()) {
							line = iLines.next().replace("\t", " ").trim();
							if (line.contains(" PLANET:") || line.contains(" COLONY:")) {
								iLines.previous();
								continue planetScan;
							}
							if (line.isEmpty()) {
								break scanInventory;
							}
							GWT.log(" -- "+line);
							inventory.add(line);
						}
					}
				}
 			}
		}

		/**
		 * parse supplied PRODUCTION orders for planets
		 */
		tmp = StringUtils.substringBetween(report, "START PRODUCTION", "END");
		if (tmp != null) {
			tmp = tmp.trim();
			ListIterator<String> iter = Arrays.asList(StringUtils.split(tmp, "\n")).listIterator();
			parseProductionSections: while (iter.hasNext()) {
				String line = iter.next().trim();
				if (line.isEmpty()) {
					continue;
				}
				if (!line.startsWith("PRODUCTION PL")) {
					continue;
				}
				PlanetInfo planetInfo = new PlanetInfo();
				planetInfo.setName(StringUtils.substringAfter(line, "PRODUCTION").trim());
				while (iter.hasNext()) {
					line = iter.next().trim();
					if (line.startsWith("PRODUCTION PL")) {
						iter.previous();
						continue parseProductionSections;
					}
					if (!line.contains("(sector ")) {
						continue;
					}
					line = StringUtils.substringAfterLast(line, "(sector ").trim();
					line = StringUtils.substringBefore(line, ")");
					if (!line.contains("#")) {
						continue;
					}
					StarSystem starSystem = new StarSystem();
					starSystem.setX(parseInteger(line));
					line = StringUtils.substringAfter(line, " ").trim();
					starSystem.setY(parseInteger(line));
					line = StringUtils.substringAfter(line, " ").trim();
					starSystem.setZ(parseInteger(line));
					planetInfo.setStarSystem(starSystem);
					line = StringUtils.substringAfter(line, "#").trim();
					planetInfo.setPlanetNo(parseInteger(line));
					planetInfo.setColony(true);
					break;
				}
				planets.add(planetInfo);
			}
		}
		/**
		 * Parse other available planet containing data
		 */
		tmp = StringUtils.substringBetween(report, "Other planets and ships:", DIVIDER);
		if (tmp != null) {
			for (String line : StringUtils.split(tmp, "\n")) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (!line.contains("PL")) {
					continue;
				}
				if (!line.contains("#")) {
					continue;
				}
				if (!line.matches("[\\s\\S]*?\\d+\\s+\\d+\\s+#\\d+[\\s\\S]*")) {
					continue;
				}
				if (line.contains(",")) {
					line = StringUtils.substringBefore(line, ",");
				}
				line = line.trim();
				StarSystem starSystem = new StarSystem();
				starSystem.setX(parseInteger(line));
				line = StringUtils.substringAfter(line, " ").trim();
				starSystem.setY(parseInteger(line));
				line = StringUtils.substringAfter(line, " ").trim();
				starSystem.setZ(parseInteger(line));
				PlanetInfo planetInfo = new PlanetInfo();
				planetInfo.setStarSystem(starSystem);
				line = StringUtils.substringAfter(line, "#").trim();
				planetInfo.setPlanetNo(parseInteger(line));
				line = "PL " + StringUtils.substringAfter(line, "PL ").trim();
				planetInfo.setName(line);
				planets.add(planetInfo);
			}
		}
		return planets;
	}

	private static GameStats parseSpeciesInformation(String report) {
		GameStats gameStats = new GameStats();
		String tmp;
		tmp = StringUtils.substringBetween(report, "START OF TURN", DIVIDER);
		gameStats.setName(StringUtils.substringBetween(tmp, "Species name:", "\n").trim());
		gameStats.setGovName(StringUtils.substringBetween(tmp, "Government name:", "\n").trim());
		gameStats.setGovType(StringUtils.substringBetween(tmp, "Government type:", "\n").trim());
		return gameStats;
	}

	private static TechLevels parseTechLevels(String report) {
		String tmp = StringUtils.substringBetween(report, "START OF TURN", DIVIDER);
		TechLevels techLevels = new TechLevels();
		techLevels.setBiology(parseInteger(StringUtils.substringBetween(tmp, "Biology =", "\n").trim()));
		techLevels.setGravitics(parseInteger(StringUtils.substringBetween(tmp, "Gravitics =", "\n").trim()));
		techLevels.setLifeSupport(parseInteger(StringUtils.substringBetween(tmp, "Life Support =", "\n").trim()));
		techLevels.setManufacturing(parseInteger(StringUtils.substringBetween(tmp, "Manufacturing =", "\n").trim()));
		techLevels.setMilitary(parseInteger(StringUtils.substringBetween(tmp, "Military =", "\n").trim()));
		techLevels.setMining(parseInteger(StringUtils.substringBetween(tmp, "Mining =", "\n").trim()));
		return techLevels;
	}

	public static int parseInteger(String value) {
		try {
			return Integer.valueOf(value.trim().replaceAll("^(\\d+).*", "$1"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * <p>
	 * Checks whether the character is ASCII 7 bit numeric.
	 * </p>
	 *
	 * <pre>
	 *   CharUtils.isAsciiNumeric('a')  = false
	 *   CharUtils.isAsciiNumeric('A')  = false
	 *   CharUtils.isAsciiNumeric('3')  = true
	 *   CharUtils.isAsciiNumeric('-')  = false
	 *   CharUtils.isAsciiNumeric('\n') = false
	 *   CharUtils.isAsciiNumeric('&copy;') = false
	 * </pre>
	 *
	 * @param ch
	 *            the character to check
	 * @return true if between 48 and 57 inclusive
	 */
	public static boolean isAsciiNumeric(final char ch) {
		return ch >= '0' && ch <= '9';
	}

	public static boolean startsWithAsciiNumeric(String text) {
		return isBlank(text) ? false : isAsciiNumeric(text.charAt(0));
	}
}
