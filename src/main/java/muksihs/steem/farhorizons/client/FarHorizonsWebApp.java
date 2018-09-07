package muksihs.steem.farhorizons.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.JsonEncoderDecoder.DecodingException;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import blazing.chain.LZSEncoding;
import elemental2.dom.DomGlobal;
import muksihs.steem.farhorizons.client.cache.AccountCache;
import muksihs.steem.farhorizons.client.rsc.OrderFormResources;
import muksihs.steem.farhorizons.shared.Consts;
import muksihs.steem.farhorizons.shared.ExtractDetailedGameInfo;
import muksihs.steem.farhorizons.shared.JoinGameInfo;
import muksihs.steem.farhorizons.shared.OrderFormPart;
import muksihs.steem.farhorizons.shared.PlanetInfo;
import muksihs.steem.farhorizons.shared.SteemContent;
import muksihs.steem.farhorizons.shared.SteemPostingInfo;
import muksihs.steem.farhorizons.ui.MainView;
import steem.CommentMetadata;
import steem.CommentResult;
import steem.SteemApi;
import steem.SteemAuth;
import steem.SteemBroadcast;
import steem.SteemCallback;
import steem.SteemCallbackArray;
import steem.model.accountinfo.AccountInfo;
import steem.model.accountinfo.Posting;
import steem.model.discussion.Discussions;
import steem.model.discussion.Discussions.Discussion;
import steem.model.discussion.Discussions.JsonMetadata;

public class FarHorizonsWebApp implements ScheduledCommand, GlobalEventBus, ValueChangeHandler<String> {

	private static final String DEFAULT_USER = "default-user";

	private RootPanel rp;
	private ViewController controller;

	interface MyEventBinder extends EventBinder<FarHorizonsWebApp> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private String gamemaster;

	private String gamePermLink;

	private GameStats gameStats;

	protected String turnResultsReport = "";

	protected String previousOrders;

	private boolean isExpiredTurnAlerted = false;

	@EventHandler
	protected void isMostRecentTurn(Event.MostRecentTurnCheck event) {
		if (getGameId() == null) {
			DomGlobal.console.log("- no game id!");
			fireEvent(new Event.EnableOrderForm(false));
			return;
		}
		final int[] discussions = { 2 };
		SteemCallback<Discussions> cb = new SteemCallback<Discussions>() {
			@Override
			public void onResult(JavaScriptObject error, Discussions result) {
				boolean found = false;
				gamePostScan: for (Discussion discussion : result.getDiscussions()) {
					if (!discussion.getAuthor().equalsIgnoreCase(gamemaster)) {
						continue gamePostScan;
					}
					JsonMetadata metadata;
					try {
						metadata = SteemApi.Util.jsonMetadataCodec.decode(discussion.getJsonMetadataRaw());
					} catch (DecodingException e) {
						GWT.log(e.getMessage(), e);
						metadata = null;
					}
					if (metadata != null && metadata.getTags() != null && metadata.getTags().contains(getGameId())) {
						found = true;
						boolean isMostRecentTurn = event.getPermlink().equals(discussion.getPermlink());
						if (!isMostRecentTurn) {
							if (!isExpiredTurnAlerted) {
								fireEvent(new Event.AlertMessage(
										"This information is out of date. A newer turn exists."));
								isExpiredTurnAlerted = true;
							}
						}
						if (!event.getTags().contains("new-game")) {
							fireEvent(new Event.EnableOrderForm(isMostRecentTurn));
						}
						break gamePostScan;
					}
				}
				if (!found) {
					if (discussions[0] > 32) {
						GWT.log("This appears to be a very old or malformed game post. Not scanning any further back in time.");
						return;
					}
					discussions[0] += 4;
					GWT.log("Most recent game post not found yet. New search depth: " + discussions[0]);
					SteemApi.getDiscussionsByBlog(gamemaster, discussions[0], this);
				}
			}
		};
		SteemApi.getDiscussionsByBlog(gamemaster, discussions[0], cb);
	}

	@Override
	public void execute() {
		rp = RootPanel.get("farhorizons");
		rp.clear();
		MainView mainView = new MainView();
		rp.add(mainView);
		eventBinder.bindEventHandlers(this, eventBus);
		setController(new ViewController(mainView.getPanel()));
		fireEvent(new Event.Loading(true));
		// hash parsing
		History.addValueChangeHandler(this);
		Scheduler.get().scheduleDeferred(this::startApp);
	}

	@EventHandler
	protected void loginLogoutToggle(Event.LoginLogout event) {
		if (isLoggedIn()) {
			AccountCache accountCache = new AccountCache();
			SteemPostingInfo steemPostingInfo = accountCache.get(DEFAULT_USER);
			steemPostingInfo.setWif(null);
			accountCache.put(DEFAULT_USER, steemPostingInfo);
			loggedIn = false;
			fireEvent(new Event.LoginComplete(false));
			Location.reload();
		} else {
			fireEvent(new Event.Login<GenericEvent>(null));
		}
	}

	private void startApp() {
		DomGlobal.console.log("Far Horizons Steem: START");
		// validate cached login credentials (if any)
		AccountCache cache = new AccountCache();
		SteemPostingInfo info = cache.get(DEFAULT_USER);
		if (info != null) {
			fireEvent(new Event.TryLogin(info.getUsername(), info.getWif(), true));
		} else {
			fireEvent(new Event.LoginComplete(false));
		}
	}

	interface SteemContentCodec extends JsonEncoderDecoder<SteemContent> {
	}

	private static String gameId;

	@EventHandler
	protected void doShowWantSpeciesStatus(Event.WantSpeciesStatus event) {
		if (!isLoggedIn()) {
			fireEvent(new Event.LoginLogout());
			return;
		}
		fireEvent(new Event.ShowSpeciesStatus(turnResultsReport, gameStats));
	}

	private String basicUnescape(String text) {
		if (text == null) {
			return null;
		}
		return text.replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&");
	}

	public static String basicEscape(String text) {
		if (text == null) {
			return null;
		}
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	@EventHandler
	protected void doShowTurnResults(Event.WantShowTurnResults event) {
		if (!isLoggedIn()) {
			fireEvent(new Event.LoginLogout());
			return;
		}
		fireEvent(new Event.TurnResultsMenuEnable(true));
		fireEvent(new Event.ShowTurnResult(turnResultsReport));
	}

	private void populateStatsUsing(String report) {
		this.gameStats = ExtractDetailedGameInfo.parse(report);
		fireEvent(new Event.UpdateGameStats(gameStats));
	}

	@EventHandler
	protected void doShowOrdersForm(Event.WantShowOrdersForm event) {
		if (!isLoggedIn()) {
			fireEvent(new Event.LoginLogout());
			return;
		}
		String report;
		if (event.isLoadPreviousOrders() && !isBlank(previousOrders)) {
			report = previousOrders;
		} else {
			report = turnResultsReport;
			report = StringUtils.substringAfterLast(report, "ORDER SECTION.");
		}
		report = report.replace("\t", "     ");
		// need an extra '\n' between sections to simplify parsing
		report = "\n" + report + "\n";
		report = report.replace("END\n", "END\n\n");
		report = report.replace("\nSTART ", "\n\nSTART ");
		String[] parts = StringUtils.substringsBetween(report, "\nSTART ", "\nEND\n");
		if (parts == null || parts.length == 0) {
			fireEvent(new Event.FatalError("UNABLE TO PROCESS SECRET GALACTIC ORDER FORM!"));
			return;
		}
		List<OrderFormPart> ordersTemplate = new ArrayList<>();
		for (String part : parts) {
			OrderFormPart template = new OrderFormPart();
			template.setSection(StringUtils.substringBefore(part, "\n").trim());
			template.setOrders(StringUtils.substringAfter(part, template.getSection()).trim() + "\n");
			ordersTemplate.add(template);
			if ("POST-ARRIVAL".equals(template.getSection())) {
				if (!template.getOrders().toLowerCase().contains("auto")) {
					template.setOrders(template.getOrders()
							+ "\n;The following command will provide suggested orders for production, jumps, etc, on your next turn.\nAUTO\n");
				}
			}
		}
		/*
		 * make sure all needed sections EXIST and are in the correct order
		 */
		Map<String, Integer> sectionSorts = new HashMap<>();
		sectionSorts.put("combat", 0);
		sectionSorts.put("pre-departure", 1);
		sectionSorts.put("jumps", 2);
		sectionSorts.put("production", 3);
		sectionSorts.put("post-arrival", 4);
		sectionSorts.put("strikes", 5);

		Collections.sort(ordersTemplate, (a, b) -> {
			Integer s1 = sectionSorts.get(a.getSection().toLowerCase());
			Integer s2 = sectionSorts.get(b.getSection().toLowerCase());
			if (s1 == null) {
				s1 = -1;
			}
			if (s2 == null) {
				s2 = -1;
			}
			return Integer.compare(s1, s2);
		});
		if (ordersTemplate.size() < 6) {
			int secNo = 0;
			String section = "COMBAT";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
			secNo = 1;
			section = "PRE-DEPARTURE";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
			secNo = 2;
			section = "JUMPS";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
			secNo = 3;
			section = "PRODUCTION";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
			secNo = 4;
			section = "POST-ARRIVAL";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
			secNo = 5;
			section = "STRIKES";
			if (ordersTemplate.size() == secNo || !ordersTemplate.get(secNo).getSection().equalsIgnoreCase(section)) {
				ordersTemplate.add(new OrderFormPart(section, ";Place " + section.toLowerCase() + " orders here."));
			}
		}
		OrderFormPart productionSection = ordersTemplate.get(3);
		String orders = productionSection.getOrders();
		for (PlanetInfo info: gameStats.getPlanetInfo()) {
			if (info.getName()==null||info.getName().trim().isEmpty()) {
				continue;
			}
			if (info.getLsn()==null||info.getLsn().trim().isEmpty()) {
				continue;
			}
			final String marker = "PRODUCTION "+info.getName();
			GWT.log(marker);
			if (!orders.contains(marker)) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(";========================================\n");
			sb.append(marker);
			sb.append("\n");
			sb.append(";----------------------------------------\n");
			sb.append("; LSN = ");
			String lsn = info.getLsn();
			if (lsn==null || lsn.trim().isEmpty()) {
				sb.append("???");
			} else {
				sb.append(lsn.trim());
			}
			sb.append("\n");
			if (!info.getInventory().isEmpty()) {
				for (String inventory: info.getInventory()) {
					sb.append("    ; ");
					sb.append(inventory.trim());
					sb.append("\n");
				}
			}
			orders = orders.replaceAll(marker+"\\s*?\n", sb.toString());
		}
		productionSection.setOrders(orders);
		//space fix each of the order sections
//		for (OrderFormPart section: ordersTemplate) {
//			StringBuilder sb = new StringBuilder();
//			for (String order: section.getOrders().split("\n")) {
//				order = order.trim();
//				if (order.isEmpty()) {
//					continue;
//				}
//				sb.append(order);
//				sb.append("\n");
//			}
//			section.setOrders(sb.toString());
//		}
		fireEvent(new Event.ShowOrdersForm(ordersTemplate));
	}

	public static boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		fireEvent(new Event.ClearView());
		fireEvent(new Event.LoadTurnResults());
	}

	@EventHandler
	protected void loadTurnResults(Event.LoadTurnResults event) {
		if (mostRecentTimer != null) {
			mostRecentTimer.cancel();
			mostRecentTimer = null;
		}
		turnResultsReport = "";
		previousOrders = "";
		gameStats = new GameStats();
		fireEvent(new Event.UpdateGameStats(gameStats));
		fireEvent(new Event.Loading(true));
		String token = History.getToken();
		SteemCallback<Discussion> cb = new SteemCallback<Discussion>() {
			@Override
			public void onResult(JavaScriptObject error, Discussion content) {
				fireEvent(new Event.Loading(false));
				if (error != null) {
					fireEvent(new Event.FatalError(JsonUtils.stringify(error, " ")));
					return;
				}
				if (content == null || isBlank(content.getBody())) {
					fireEvent(new Event.AlertMessage("Invalid Permlink!"));
					fireEvent(new Event.WantShowTurnResults());
					return;
				}
				JsonMetadata metadata;
				try {
					metadata = SteemApi.Util.jsonMetadataCodec.decode(content.getJsonMetadataRaw());
					for (String tag : metadata.getTags()) {
						if (tag.startsWith("game-")) {
							setGameId(tag);
						}
					}
				} catch (DecodingException e) {
					GWT.log(e.getMessage(), e);
					DomGlobal.console.log(e.getMessage());
					metadata = new JsonMetadata();
				}
				if (metadata.getTags().contains("new-game")) {
					fireEvent(new Event.TurnResultsMenuEnable(false));
					fireEvent(new Event.ShowGameJoinForm());
					return;
				}
				String msg = content.getBody();
				String secretMsg;
				String gameData;
				String compressedGameData = metadata.getGameData();
				if (compressedGameData == null) {
					secretMsg = StringUtils.substringBetween(msg, "<div id='secret-message'>", "</div>");
					if (secretMsg == null) {
						fireEvent(new Event.ShowTurnResult("!!! UNABLE TO DISPLAY SPECIES STATUS!"));
						return;
					}
					gameData = LZSEncoding.decompressFromUTF16(basicUnescape(secretMsg));
					if (gameData == null) {
						fireEvent(new Event.ShowTurnResult("!!! UNABLE TO DISPLAY SPECIES STATUS!"));
						return;
					}
				} else {
					gameData = LZSEncoding.decompressFromBase64(compressedGameData);
					if (gameData == null) {
						fireEvent(new Event.ShowTurnResult("!!! UNABLE TO DISPLAY SPECIES STATUS!"));
						return;
					}
				}
				String report = StringUtils.substringBetween(gameData, "@" + username + " BEGIN",
						"@" + username + " END");
				if (report == null) {
					fireEvent(new Event.ShowTurnResult(
							"!!! UNABLE TO DISPLAY SPECIES STATUS!\n!!! ARE YOU LOOKING AT THE CORRECT GAME?\n"));
					return;
				}
				report = report.trim();
				turnResultsReport = report;
				populateStatsUsing(turnResultsReport);
				fireEvent(new Event.LoadPreviousOrders(gamemaster, username, content.getPermlink()));
				periodicMostRecenTurnCheck(content.getPermlink(), metadata.getTags());
			}
		};
		gamemaster = token.replaceAll("@(.*?)/.*", "$1");
		gamePermLink = token.replaceAll(".*?/", "");
		SteemApi.getContent(gamemaster, gamePermLink, cb);
	}

	private Timer mostRecentTimer = null;

	protected void periodicMostRecenTurnCheck(String permlink, List<String> tags) {
		if (mostRecentTimer != null && mostRecentTimer.isRunning()) {
			return;
		}
		mostRecentTimer = new Timer() {
			@Override
			public void run() {
				periodicMostRecenTurnCheck(permlink, tags);
			}
		};
		mostRecentTimer.schedule(5 * 60 * 1000);
		fireEvent(new Event.MostRecentTurnCheck(permlink, tags));
	}

	@EventHandler
	protected void loadPreviousOrders(Event.LoadPreviousOrders event) {
		SteemCallback<Discussions> cb = new SteemCallback<Discussions>() {
			@Override
			public void onResult(JavaScriptObject error, Discussions result) {
				if (error != null) {
					fireEvent(new Event.AlertMessage(JsonUtils.stringify(error, "   ")));
					fireEvent(new Event.WantShowTurnResults());
					return;
				}
				String tmpPrevOrders = null;
				for (Discussion reply : result.getDiscussions()) {
					if (!reply.getAuthor().equals(event.getUsername())) {
						continue;
					}
					String body = reply.getBody();
					if (!body.startsWith("<html")) {
						continue;
					}
					body = StringUtils.substringBetween(body, "<html>", "</html>");
					if (body == null) {
						continue;
					}
					body = basicUnescape(body);
					body = LZSEncoding.decompressFromUTF16(body);
					if (body == null) {
						continue;
					}
					if (!body.startsWith("START COMBAT")) {
						continue;
					}
					if (!body.trim().endsWith("END")) {
						continue;
					}
					tmpPrevOrders = body;
				}
				if (tmpPrevOrders != null && !tmpPrevOrders.trim().isEmpty()) {
					previousOrders = tmpPrevOrders;
					populateGameStateNamedPlanets(previousOrders);
					GWT.log("Previous Orders: \n" + previousOrders);
				}
				fireEvent(new Event.LoadTurnResultsComplete());
				fireEvent(new Event.WantShowTurnResults());
			}
		};
		SteemApi.getContentReplies(event.getGamemaster(), event.getPermlink(), cb);
	}

	private void populateGameStateNamedPlanets(String previousOrders) {
		if (isBlank(previousOrders)) {
			return;
		}
		GameState gameState = new GameState();
		ListIterator<String> iLines = Arrays.asList(StringUtils.split(previousOrders, "\n")).listIterator();
		while (iLines.hasNext()) {
			String line = iLines.next();
			if (isBlank(line)) {
				continue;
			}
			line = line.trim();
			if (!line.toLowerCase().startsWith("name ")) {
				continue;
			}
			line = StringUtils.substringAfter(line, " ").trim();
			line = line.replace(" pl ", " PL ");
			line = line.replace(" Pl ", " PL ");
			line = line.replace(" pL ", " PL ");
			String planetTag = StringUtils.substringBefore(line, "PL ");
			String planetName = "PL " + StringUtils.substringAfter(line, "PL ");
			if (isBlank(planetName)) {
				continue;
			}
			if (isBlank(planetTag)) {
				continue;
			}
			gameState.put(FarHorizonsWebApp.getGameId() + " planet: " + planetTag.trim(), planetName.trim());
		}
	}

	public ViewController getController() {
		return controller;
	}

	public void setController(ViewController controller) {
		this.controller = controller;
	}

	private boolean loggedIn;

	private boolean isLoggedIn() {
		return loggedIn;
	}

	@EventHandler
	protected void loginComplete(Event.LoginComplete event) {
		fireEvent(new Event.Loading(false));
		loggedIn = event.isLoggedIn();
		if (afterLoginPendingEvent != null && event.isLoggedIn()) {
			fireEvent(afterLoginPendingEvent);
		}
		afterLoginPendingEvent = null;
		if (event.isLoggedIn()) {
			History.fireCurrentHistoryState();
		} else {
			fireEvent(new Event.ShowTurnResult("!!! YOU MUST LOGIN TO PLAY!"));
			fireEvent(new Event.TurnResultsMenuEnable(false));
		}
	}

	private GenericEvent afterLoginPendingEvent;

	@EventHandler
	protected <T extends GenericEvent> void login(Event.Login<T> event) {
		afterLoginPendingEvent = event.getRefireEvent();
		fireEvent(new Event.ShowLoginUi());
	}

	private String username = "";
	private String userWif = "";

	@EventHandler
	protected void tryLogin(Event.TryLogin event) {
		final String wif = event.getWif() == null ? "" : event.getWif().trim();
		if (wif.isEmpty()) {
			fireEvent(new Event.LoginComplete(false));
			return;
		}
		if (!wif.equals(wif.replaceAll("[^123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]", ""))) {
			if (!event.isSilent()) {
				fireEvent(new Event.AlertMessage("That is not a valid key."));
			}
			fireEvent(new Event.Loading(false));
			return;
		}
		if (wif.startsWith("STM")) {
			if (!event.isSilent()) {
				fireEvent(new Event.AlertMessage(
						"That is not the PRIVATE posting key. Please visit your wallet => permissions => select \"show private key\" next to your posting key, to obtain your private posting key. It will not start with 'STM'."));
			}
			fireEvent(new Event.Loading(false));
			return;
		}
		SteemCallbackArray<AccountInfo> cb = new SteemCallbackArray<AccountInfo>() {

			@Override
			public void onResult(JavaScriptObject error, AccountInfo[] result) {
				fireEvent(new Event.Loading(false));
				if (error != null) {
					if (!event.isSilent()) {
						fireEvent(new Event.AlertMessage(JsonUtils.stringify(error, " ")));
					}
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				if (result.length == 0) {
					if (!event.isSilent()) {
						fireEvent(new Event.AlertMessage("Username not found!"));
					}
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				AccountInfo accountInfo = result[0];
				if (accountInfo == null) {
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				Posting posting = accountInfo.getPosting();
				if (posting == null) {
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				String[][] keyAuths = posting.getKeyAuths();
				if (keyAuths == null || keyAuths.length == 0) {
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				String[] keylist = keyAuths[0];
				if (keylist == null || keylist.length == 0) {
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				String publicWif = keylist[0];
				try {
					if (!SteemAuth.wifIsValid(wif, publicWif)) {
						new AccountCache().remove(DEFAULT_USER);
						if (!event.isSilent()) {
							fireEvent(new Event.AlertMessage("THAT IS NOT YOUR PRIVATE POSTING KEY"));
						}
						fireEvent(new Event.LoginComplete(false));
						return;
					}
				} catch (JavaScriptException e) {
					DomGlobal.console.log("JavaScriptException: " + e.getMessage());
					if (!event.isSilent()) {
						fireEvent(new Event.AlertMessage(e.getMessage()));
					}
					fireEvent(new Event.LoginComplete(false));
					return;
				}
				AccountCache cache = new AccountCache();
				SteemPostingInfo info = new SteemPostingInfo();
				info.setUsername(accountInfo.getName());
				info.setWif(wif);
				cache.put(DEFAULT_USER, info);
				fireEvent(new Event.LoginComplete(true));
				DomGlobal.console.log("Logged in as: " + accountInfo.getName());
				username = accountInfo.getName();
				userWif = wif;
			}
		};
		fireEvent(new Event.Loading(true));
		String username = event.getUsername();
		while (username.trim().startsWith("@")) {
			username = username.trim().substring(1);
		}
		try {
			SteemApi.getAccounts(new String[] { username }, cb);
		} catch (Exception e) {
			DomGlobal.console.log(e);
			new Timer() {
				@Override
				public void run() {
					fireEvent(event);
				}
			}.schedule(500);
		}
	}

	@EventHandler
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.AppVersion(Consts.APP_VERSION));
	}

	private int getLength(String text) {
		if (text == null || text.trim().isEmpty()) {
			return 0;
		}
		int l1 = text.trim().length();
		int l2 = text.trim().getBytes(StandardCharsets.UTF_8).length;
		return l1 > l2 ? l1 : l2;
	}

	@EventHandler
	protected void doJoinGame(Event.ValidateThenSubmitJoinGame event) {
		if (!isLoggedIn()) {
			afterLoginPendingEvent = event;
			fireEvent(new Event.LoginLogout());
			return;
		}
		JoinGameInfo info = event.getInfo();
		/*
		 * must be exactly 15 in value and no negatives for point spread
		 */
		if (info.getBi() + info.getGv() + info.getLs() + info.getMl() != 15) {
			fireEvent(new Event.AlertMessage("Your points must add up to 15!"));
			return;
		}
		if (info.getBi() < 0 || info.getGv() < 0 || info.getLs() < 0 || info.getMl() < 0) {
			fireEvent(new Event.AlertMessage("Your points must range from 0 to 15!"));
			return;
		}
		/*
		 * length checks
		 */
		if (getLength(info.getGovName()) > 31) {
			fireEvent(new Event.AlertMessage("Government name too long!"));
			return;
		}
		if (getLength(info.getGovType()) > 31) {
			fireEvent(new Event.AlertMessage("Government type too long!"));
			return;
		}
		if (getLength(info.getHomeName()) > 31) {
			fireEvent(new Event.AlertMessage("Home planet name too long!"));
			return;
		}
		if (getLength(info.getSpeciesName()) > 31) {
			fireEvent(new Event.AlertMessage("Species name too long!"));
			return;
		}
		if (getLength(info.getSpeciesName()) < 7) {
			fireEvent(new Event.AlertMessage("Species name too short!"));
			return;
		}

		/*
		 * no commas
		 */
		if (info.getGovName().contains(",")) {
			fireEvent(new Event.AlertMessage("Government name cannot contain a comma!"));
			return;
		}
		if (info.getGovType().contains(",")) {
			fireEvent(new Event.AlertMessage("Government type cannot contain a comma!"));
			return;
		}
		if (info.getHomeName().contains(",")) {
			fireEvent(new Event.AlertMessage("Home planet name cannot contain a comma!"));
			return;
		}
		if (info.getSpeciesName().contains(",")) {
			fireEvent(new Event.AlertMessage("Species name cannot contain a comma!"));
			return;
		}
		/*
		 * nothing blank
		 */
		if (info.getGovName().trim().isEmpty()) {
			fireEvent(new Event.AlertMessage("Government name must be specified!"));
			return;
		}
		if (info.getGovType().trim().isEmpty()) {
			fireEvent(new Event.AlertMessage("Government type must be specified!"));
			return;
		}
		if (info.getHomeName().trim().isEmpty()) {
			fireEvent(new Event.AlertMessage("Home planet name must be specified!"));
			return;
		}
		if (info.getSpeciesName().trim().isEmpty()) {
			fireEvent(new Event.AlertMessage("Species name must be specified!"));
			return;
		}
		fireEvent(new Event.Loading(true));

		String replyPermLink = gamePermLink + "-" + System.currentTimeMillis() + "-" + username;
		CommentMetadata metadata = new CommentMetadata();
		metadata.setApp("far-horizons/1.0");
		metadata.setFormat("text/html");
		metadata.setTags(new String[] { gamePermLink, "far-horizons", "games", "gaming" });
		StringBuilder body = new StringBuilder();

		body.append("Species name: ");
		body.append(info.getSpeciesName());
		body.append("\n");

		body.append("Home planet name: ");
		body.append(info.getHomeName());
		body.append("\n");

		body.append("Government name: ");
		body.append(info.getGovName());
		body.append("\n");

		body.append("Government type: ");
		body.append(info.getGovType());
		body.append("\n");

		body.append("Military: ");
		body.append(info.getMl());
		body.append("\n");

		body.append("Gravitics: ");
		body.append(info.getGv());
		body.append("\n");

		body.append("Life Support: ");
		body.append(info.getLs());
		body.append("\n");

		body.append("Biology: ");
		body.append(info.getBi());
		body.append("\n");

		final String orders = body.toString();

		String secretMessage = "<html>" + basicEscape(LZSEncoding.compressToUTF16(orders)) + "</html>";
		SteemCallback<CommentResult> cb = new SteemCallback<CommentResult>() {
			@Override
			public void onResult(JavaScriptObject error, CommentResult result) {
				fireEvent(new Event.Loading(false));
				if (error != null) {
					String errorJson = JsonUtils.stringify(error, " ");
					if (errorJson.contains("bandwidth limit exceeded.")) {
						fireEvent(new Event.AlertMessage(
								"You STEEM bandwidth limit has been exceeded. Try again later."));
					} else if (errorJson.contains("STEEMIT_MIN_REPLY_INTERVAL")) {
						fireEvent(new Event.AlertMessage("You may only comment once every 20 seconds.  Try again."));
					} else {
						fireEvent(new Event.FatalError(errorJson));
					}
					return;
				}
				fireEvent(new Event.JoinGameSubmitDone(orders));
			}
		};
		SteemBroadcast.comment(userWif, gamemaster, gamePermLink, username, replyPermLink,
				"Far Horizons - " + gamePermLink, secretMessage, metadata, cb);

	}

	@EventHandler
	protected void doSubmitOrders(Event.SubmitOrders event) {
		if (!isLoggedIn()) {
			fireEvent(new Event.LoginLogout());
			return;
		}
		fireEvent(new Event.Loading(true));
		String replyPermLink = gamePermLink + "-" + System.currentTimeMillis() + "-" + username;
		CommentMetadata metadata = new CommentMetadata();
		metadata.setApp("far-horizons/1.0");
		metadata.setFormat("text/html");
		metadata.setTags(new String[] { gamePermLink, "far-horizons", "games", "gaming" });
		StringBuilder body = new StringBuilder();
		for (OrderFormPart part : event.getOrderFormSections()) {
			body.append("START ");
			body.append(part.getSection());
			body.append("\n");
			String[] orders = part.getOrders()==null?new String[0]:part.getOrders().split("\n");
			for (String order: orders) {
				order = order.replaceAll(";.*$", "").trim();
				if (order.isEmpty()) {
					continue;
				}
				body.append("    ");
				body.append(order);
				body.append("\n");
			}
			body.append("END\n\n");
		}
		final String orders = body.toString();

		String secretMessage = "<html>" + basicEscape(LZSEncoding.compressToUTF16(orders)) + "</html>";
		SteemCallback<CommentResult> cb = new SteemCallback<CommentResult>() {
			@Override
			public void onResult(JavaScriptObject error, CommentResult result) {
				fireEvent(new Event.Loading(false));
				if (error != null) {
					String errorJson = JsonUtils.stringify(error, " ");
					if (errorJson.contains("bandwidth limit exceeded.")) {
						fireEvent(new Event.AlertMessage(
								"You STEEM bandwidth limit has been exceeded. Try again later."));
						fireEvent(new Event.OrdersSubmitIncomplete());
					} else if (errorJson.contains("STEEMIT_MIN_REPLY_INTERVAL")) {
						fireEvent(new Event.AlertMessage("You may only comment once every 20 seconds. Try again."));
					} else {
						fireEvent(new Event.FatalError(errorJson));
					}
					return;
				}
				fireEvent(new Event.OrdersSubmitDone(orders));
			}
		};
		SteemBroadcast.comment(userWif, gamemaster, gamePermLink, username, replyPermLink,
				"Far Horizons - " + gamePermLink, secretMessage, metadata, cb);
	}

	@EventHandler
	protected void showPdfManual(Event.ShowPdfManual event) {
		Window.open("Far-Horizons-Manual.pdf", "_blank", "");
	}

	@EventHandler
	protected void showCommandHelp(Event.ShowCommandHelp event) {
		if (event.getSectionName() == null || event.getCmd() == null) {
			return;
		}
		String sec = event.getSectionName().toLowerCase().trim();
		String cmd = event.getCmd().toLowerCase().trim();
		if (sec.isEmpty() || cmd.isEmpty()) {
			return;
		}
		String tag1 = "<!-- " + sec + ":" + cmd + " begin -->";
		String tag2 = "<!-- " + sec + ":" + cmd + " end -->";
		String helpHtml = OrderFormResources.INSTANCE.commandHelp().getText();
		helpHtml = StringUtils.substringBetween(helpHtml, tag1, tag2);
		if (helpHtml == null || helpHtml.trim().isEmpty()) {
			return;
		}
		fireEvent(new Event.HelpMessage(cmd.toUpperCase(), helpHtml));
	}

	/**
	 * Reload app after 60 seconds.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void reloadOnFatalError(Event.FatalError event) {
		new Timer() {
			@Override
			public void run() {
				Location.reload();
			}
		}.schedule(1000 * 60);
	}

	public static String getGameId() {
		return gameId;
	}

	public static void setGameId(String gameId) {
		DomGlobal.console.log("=== GAME ID: " + gameId);
		FarHorizonsWebApp.gameId = gameId;
	}
}
