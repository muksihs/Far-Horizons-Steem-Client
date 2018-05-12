package muksihs.steem.farhorizons.client.cache;

import java.util.Date;

import muksihs.steem.farhorizons.shared.SteemPostingInfo;

public class CachedAccountInfo implements HasExpiration {
	private static final long HOUR_ms = 60l * 60l * 1000l;

	public CachedAccountInfo(SteemPostingInfo steemPostingInfo) {
		this(steemPostingInfo, getAutomaticExpirationDate());
	}

	private static Date getAutomaticExpirationDate() {
		return new Date(System.currentTimeMillis() + HOUR_ms * 24l * 7l * 52l);
	}

	public CachedAccountInfo() {
	}

	@Override
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public CachedAccountInfo(SteemPostingInfo info, Date expires) {
		setAccountInfo(info);
		setExpires(expires);
	}

	private Date expires;
	private SteemPostingInfo steemPostingInfo;

	@Override
	public Date getExpires() {
		return expires;
	}

	public SteemPostingInfo getAccountInfo() {
		return steemPostingInfo;
	}

	public void setAccountInfo(SteemPostingInfo steemPostingInfo) {
		this.steemPostingInfo = steemPostingInfo;
	}

	public void resetExpiration() {
		setExpires(getAutomaticExpirationDate());
	}
}