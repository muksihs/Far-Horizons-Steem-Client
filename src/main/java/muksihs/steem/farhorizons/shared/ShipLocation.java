package muksihs.steem.farhorizons.shared;

public class ShipLocation implements Comparable<ShipLocation>{

	private String planet;
	private String x;
	private String y;
	private String z;
	private String p;
	
	private String age="";
	private String tons="";
	private String capacity="";
	private String cargo="";
	private boolean underConstruction=false;
	
	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getTons() {
		return tons;
	}

	public void setTons(String tons) {
		this.tons = tons;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getZ() {
		return z;
	}

	public void setZ(String z) {
		this.z = z;
	}

	public String getPlanet() {
		return planet;
	}

	public String getX() {
		return x;
	}

	public String getY() {
		return y;
	}

	public String getP() {
		return p;
	}

	public String getName() {
		return name;
	}

	private String name;

	public void setName(String name) {
		this.name=name;
	}

	public void setP(String p) {
		this.p=p;
	}

	public void SetZ(String z) {
		this.z=z;
	}

	public void setY(String y) {
		this.y=y;
	}

	public void setX(String x) {
		this.x=x;
	}

	public void setPlanet(String planet) {
		this.planet=planet;
	}

	@Override
	public int compareTo(ShipLocation o) {
		if (o==null) {
			return 1;
		}
		return getName().compareToIgnoreCase(o.getName());
	}

	public boolean isUnderConstruction() {
		return underConstruction;
	}

	public void setUnderConstruction(boolean underConstruction) {
		this.underConstruction = underConstruction;
	}

}
