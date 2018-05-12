package steem.model.discussion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Discussions {
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Discussion {
		
		private Map<String, Object> _properties=new HashMap<>();
		private String author;
		private String body;
		@JsonProperty("cashout_time")
		private String cashoutTime;
		
		private String category;
		private String created;
		@JsonProperty("curator_payout_value")
		private String curatorPayoutValue;
		private long id;
		@JsonProperty("json_metadata")
		private String jsonMetadataRaw;
		
		@JsonProperty("last_payout")
		private String lastPayout;

		@JsonProperty("pending_payout_value")
		private String pendingPayoutValue;

		private String permlink;

		private String title;

		@JsonProperty("total_payout_value")
		private String totalPayoutValue;

		@JsonAnyGetter
		protected Map<String, Object> _get() {
			return _properties;
		}

		public Object _get(String key) {
			return _properties.get(key);
		}

		@JsonAnySetter
		public void _put(String key, Object value) {
			_properties.put(key, value);
		}

		public String getAuthor() {
			return author;
		}

		public String getBody() {
			return body;
		}

		public String getCashoutTime() {
			return cashoutTime;
		}

		public String getCategory() {
			return category;
		}

		public String getCreated() {
			return created;
		}
		public String getCuratorPayoutValue() {
			return curatorPayoutValue;
		}
		public long getId() {
			return id;
		}
		public String getJsonMetadataRaw() {
			return jsonMetadataRaw;
		}
		
		public String getLastPayout() {
			return lastPayout;
		}

		public String getPendingPayoutValue() {
			return pendingPayoutValue;
		}

		public String getPermlink() {
			return permlink;
		}
		public String getTitle() {
			return title;
		}
		public String getTotalPayoutValue() {
			return totalPayoutValue;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public void setCashoutTime(String cashoutTime) {
			this.cashoutTime = cashoutTime;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public void setCreated(String created) {
			this.created = created;
		}

		public void setCuratorPayoutValue(String curatorPayoutValue) {
			this.curatorPayoutValue = curatorPayoutValue;
		}

		public void setId(long id) {
			this.id = id;
		}

		public void setJsonMetadataRaw(String jsonMetadataRaw) {
			this.jsonMetadataRaw = jsonMetadataRaw;
		}

		public void setLastPayout(String lastPayout) {
			this.lastPayout = lastPayout;
		}

		public void setPendingPayoutValue(String pendingPayoutValue) {
			this.pendingPayoutValue = pendingPayoutValue;
		}

		public void setPermlink(String permlink) {
			this.permlink = permlink;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setTotalPayoutValue(String totalPayoutValue) {
			this.totalPayoutValue = totalPayoutValue;
		}
	}
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JsonMetadata {
		private Map<String, Object> _properties=new HashMap<>();
		private List<String> tags=new ArrayList<>();
		private List<String> users=new ArrayList<>();
		@JsonAnyGetter
		protected Map<String, Object> _get() {
			return _properties;
		}
		public Object _get(String key) {
			return _properties.get(key);
		}
		@JsonAnySetter
		public void _put(String key, Object value) {
			_properties.put(key, value);
		}
		public List<String> getTags() {
			return tags;
		}
		public List<String> getUsers() {
			return users;
		}
		
		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public void setUsers(List<String> users) {
			this.users = users;
		}
	}
	
	private Map<String, Object> _properties=new HashMap<>();
	@JsonProperty("discussions")
	private List<Discussion> discussions = new ArrayList<>();

	@JsonAnyGetter
	protected Map<String, Object> _get() {
		return _properties;
	}
	
	public Object _get(String key) {
		return _properties.get(key);
	}

	@JsonAnySetter
	public void _put(String key, Object value) {
		_properties.put(key, value);
	}

	public List<Discussion> getDiscussions() {
		return discussions;
	}

	public void setDiscussions(List<Discussion> discussions) {
		this.discussions = discussions;
	}
}
