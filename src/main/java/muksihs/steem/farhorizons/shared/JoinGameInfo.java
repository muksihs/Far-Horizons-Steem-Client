package muksihs.steem.farhorizons.shared;

public class JoinGameInfo {
	public JoinGameInfo(String speciesName, String homeName, String govName, String govType, int ml, int gv, int ls,
			int bi) {
		super();
		this.speciesName = speciesName;
		this.homeName = homeName;
		this.govName = govName;
		this.govType = govType;
		this.ml = ml;
		this.gv = gv;
		this.ls = ls;
		this.bi = bi;
	}
	private final String speciesName;
	private final String homeName;
	private final String govName;
	private final String govType;
	private final int ml;
	private final int gv;
	private final int ls;
	private final int bi;
	
	public String getSpeciesName() {
		return speciesName;
	}
	public String getHomeName() {
		return homeName;
	}
	public String getGovName() {
		return govName;
	}
	public String getGovType() {
		return govType;
	}
	public int getMl() {
		return ml;
	}
	public int getGv() {
		return gv;
	}
	public int getLs() {
		return ls;
	}
	public int getBi() {
		return bi;
	}
	
}
