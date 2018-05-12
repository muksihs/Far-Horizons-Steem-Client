package muksihs.steem.farhorizons.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SteemContent {
	private long id;
	private String author;
	private String permlink;
	private String category;
	private String title;
	private String body;
	@JsonProperty("cashout_time")
	private String cashoutTime;
	@JsonProperty("last_payout")
	private String lastPayout;
	@JsonProperty("pending_payout_value")
	private String pendingPayoutValue;
	@JsonProperty("curator_payout_value")
	private String curatorPayoutValue;
	@JsonProperty("total_payout_value")
	private String totalPayoutValue;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPermlink() {
		return permlink;
	}
	public void setPermlink(String permlink) {
		this.permlink = permlink;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getCashoutTime() {
		return cashoutTime;
	}
	public void setCashoutTime(String cashoutTime) {
		this.cashoutTime = cashoutTime;
	}
	public String getLastPayout() {
		return lastPayout;
	}
	public void setLastPayout(String lastPayout) {
		this.lastPayout = lastPayout;
	}
	public String getPendingPayoutValue() {
		return pendingPayoutValue;
	}
	public void setPendingPayoutValue(String pendingPayoutValue) {
		this.pendingPayoutValue = pendingPayoutValue;
	}
	public String getCuratorPayoutValue() {
		return curatorPayoutValue;
	}
	public void setCuratorPayoutValue(String curatorPayoutValue) {
		this.curatorPayoutValue = curatorPayoutValue;
	}
	public String getTotalPayoutValue() {
		return totalPayoutValue;
	}
	public void setTotalPayoutValue(String totalPayoutValue) {
		this.totalPayoutValue = totalPayoutValue;
	}
	
}
