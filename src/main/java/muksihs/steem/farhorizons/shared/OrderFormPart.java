package muksihs.steem.farhorizons.shared;

public class OrderFormPart {
	public OrderFormPart(String section, String orders) {
		super();
		this.section = section;
		this.orders = orders;
	}
	private String section;
	private String orders;
	public OrderFormPart() {
	}
	
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public String getOrders() {
		return orders;
	}
	public void setOrders(String orders) {
		this.orders = orders;
	}
}
