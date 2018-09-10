package muksihs.steem.farhorizons.client;

import java.util.List;
import java.util.Set;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import muksihs.steem.farhorizons.client.GameStats.ScanInfo;
import muksihs.steem.farhorizons.shared.JoinGameInfo;
import muksihs.steem.farhorizons.shared.MostRecentPostInfo;
import muksihs.steem.farhorizons.shared.OrderFormPart;
import muksihs.steem.farhorizons.shared.ShipLocation;
import muksihs.steem.farhorizons.shared.View;

public interface Event {

	public class LoadPreviousOrders extends GenericEvent {

		private final String gamemaster;
		private final String username;
		private final String permlink;

		public String getGamemaster() {
			return gamemaster;
		}

		public String getUsername() {
			return username;
		}

		public String getPermlink() {
			return permlink;
		}

		public LoadPreviousOrders(String gamemaster, String username, String permlink) {
			this.gamemaster = gamemaster;
			this.username = username;
			this.permlink = permlink;
		}

	}

	public class ShowShipsAndBases extends GenericEvent {
		public static enum SortOrder {
			ByName, ByLocation, ByCoordinates;
		}

		private final List<ShipLocation> shipLocations;
		private final ShowShipsAndBases.SortOrder sortOrder;

		public ShowShipsAndBases(List<ShipLocation> shipLocations, ShowShipsAndBases.SortOrder sortOrder) {
			this.shipLocations = shipLocations;
			this.sortOrder = sortOrder;
		}

		public List<ShipLocation> getShipLocations() {
			return shipLocations;
		}

		public ShowShipsAndBases.SortOrder getSortOrder() {
			return sortOrder;
		}
	}

	public class LoadTurnResultsComplete extends GenericEvent {

	}

	public class LoadTurnResults extends GenericEvent {

	}

	public class CommandNamesAdd extends GenericEvent {

		private final List<String> nameCommands;

		public CommandNamesAdd(List<String> nameCommands) {
			this.nameCommands = nameCommands;
		}

		public List<String> getNameCommands() {
			return nameCommands;
		}

	}

	public class HelperNamePlanets extends GenericEvent {
		private final List<ScanInfo> scannedPlanets;
		private final GameStats gameStats;

		public HelperNamePlanets(GameStats gameStats, List<ScanInfo> scannedPlanets) {
			this.scannedPlanets = scannedPlanets;
			this.gameStats = gameStats;
		}

		public List<ScanInfo> getScannedPlanets() {
			return scannedPlanets;
		}

		public GameStats getGameStats() {
			return gameStats;
		}

	}

	public class JoinGameSubmitDone extends GenericEvent {

		private final String orders;

		public JoinGameSubmitDone(String orders) {
			this.orders = orders;
		}

		public String getOrders() {
			return orders;
		}

	}

	public class ValidateThenSubmitJoinGame extends GenericEvent {

		private final JoinGameInfo info;

		public ValidateThenSubmitJoinGame(JoinGameInfo info) {
			this.info = info;
		}

		public JoinGameInfo getInfo() {
			return info;
		}

	}

	public class ClearView extends GenericEvent {

	}

	public class TurnResultsMenuEnable extends GenericEvent {

		private final boolean enable;

		public TurnResultsMenuEnable(boolean enable) {
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}

	}

	public class ShowGameJoinForm extends GenericEvent {

	}

	public class HelpMessage extends GenericEvent {

		private final String title;
		private final String html;

		public HelpMessage(String title, String html) {
			this.title = title;
			this.html = html;
		}

		public String getTitle() {
			return title;
		}

		public String getHtml() {
			return html;
		}

	}

	public class ShowCommandHelp extends GenericEvent {

		private final String sectionName;
		private final String cmd;

		public ShowCommandHelp(String sectionName, String cmd) {
			this.sectionName = sectionName;
			this.cmd = cmd;
		}

		public String getSectionName() {
			return sectionName;
		}

		public String getCmd() {
			return cmd;
		}

	}

	public class UpdateGameStats extends GenericEvent {
		private final GameStats gameStats;

		public UpdateGameStats(GameStats gameStats) {
			this.gameStats = gameStats;
		}

		public GameStats getGameStats() {
			return gameStats;
		}
	}

	public class EnableOrderForm extends GenericEvent {
		private final boolean enable;

		public EnableOrderForm(boolean enable) {
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}

	}

	public class MostRecentTurnCheck extends GenericEvent {
		private final String permlink;
		private final List<String> tags;

		public MostRecentTurnCheck(String permlink, List<String> tags) {
			this.permlink = permlink;
			this.tags = tags;
		}

		public String getPermlink() {
			return permlink;
		}

		public List<String> getTags() {
			return tags;
		}

	}

	public class OrdersSubmitIncomplete extends GenericEvent {

	}

	public class ShowPdfManual extends GenericEvent {

	}

	public class ShowSpeciesStatus extends GenericEvent {
		private final String report;
		private final GameStats gameStats;

		public ShowSpeciesStatus(String report, GameStats gameStats) {
			this.report = report;
			this.gameStats = gameStats;
		}

		public String getReport() {
			return report;
		}

		public GameStats getGameStats() {
			return gameStats;
		}

	}

	public class WantSpeciesStatus extends GenericEvent {

	}

	public class OrdersSubmitDone extends GenericEvent {

		private final String orders;

		public OrdersSubmitDone(String orders) {
			this.orders = orders;
		}

		public String getOrders() {
			return orders;
		}

	}

	public class SubmitOrders extends GenericEvent {

		private final List<OrderFormPart> orderFormSections;

		public SubmitOrders(List<OrderFormPart> orderFormSections) {
			this.orderFormSections = orderFormSections;
		}

		public List<OrderFormPart> getOrderFormSections() {
			return orderFormSections;
		}

	}

	public class ShowOrdersForm extends GenericEvent {
		private final List<OrderFormPart> orderFormParts;
		private final String username;
		private final String gamePermLink;

		public ShowOrdersForm(String username, String gamePermLink, List<OrderFormPart> orderFormParts) {
			this.username = username;
			this.gamePermLink = gamePermLink;
			this.orderFormParts = orderFormParts;
		}

		public List<OrderFormPart> getOrderFormParts() {
			return orderFormParts;
		}

		public String getGamePermLink() {
			return gamePermLink;
		}

		public String getUsername() {
			return username;
		}

	}

	public class ShowTurnResult extends GenericEvent {
		private final String report;

		public ShowTurnResult(String report) {
			this.report = report;
		}

		public String getReport() {
			return report;
		}
	}

	public class WantShowOrdersForm extends GenericEvent {
		private final boolean loadPreviousOrders;

		public WantShowOrdersForm(boolean loadPreviousOrders) {
			this.loadPreviousOrders = loadPreviousOrders;
		}

		public boolean isLoadPreviousOrders() {
			return loadPreviousOrders;
		}

	}

	public class WantShowTurnResults extends GenericEvent {

	}

	public class PostDone extends GenericEvent {
	}

	public class DoPost extends GenericEvent {
		private final String title;

		public DoPost(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

	}

	public class ShowAbout extends GenericEvent {

	}

	public class LoginLogout extends GenericEvent {

	}

	public class TryLogin extends GenericEvent {
		private final String username;
		private final String wif;
		private final boolean silent;

		public TryLogin(String username, String wif) {
			this(username, wif, false);
		}

		public TryLogin(String username, String wif, boolean silent) {
			this.username = username;
			this.wif = wif;
			this.silent = silent;
		}

		public String getUsername() {
			return username;
		}

		public String getWif() {
			return wif;
		}

		public boolean isSilent() {
			return silent;
		}
	}

	public class LoginComplete extends GenericEvent {
		private final boolean loggedIn;

		public LoginComplete(boolean loggedIn) {
			this.loggedIn = loggedIn;
		}

		public boolean isLoggedIn() {
			return loggedIn;
		}
	}

	public class ShowLoginUi extends GenericEvent {
	}

	public class AlertMessage extends GenericEvent {
		private final String message;

		public AlertMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class QuickMessage extends GenericEvent {
		private final String message;

		public QuickMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class Login<T extends GenericEvent> extends GenericEvent {
		private final T refireEvent;

		public Login(T event) {
			this.refireEvent = event;
		}

		public T getRefireEvent() {
			return refireEvent;
		}
	}

	public class FatalError extends GenericEvent {

		private final String message;

		public FatalError(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class SetRatingsBoxes extends GenericEvent {

		private final Set<String> mustHaveRatings;

		public SetRatingsBoxes(Set<String> mustHaveRatings) {
			this.mustHaveRatings = mustHaveRatings;
		}

		public Set<String> getMustHaveRatings() {
			return mustHaveRatings;
		}

	}

	public class BrowseViewLoaded extends GenericEvent {

	}

	public class EnablePreviousButton extends GenericEvent {

		private final boolean enable;

		public EnablePreviousButton(boolean enable) {
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}

	}

	public class RemoveFromFilter extends GenericEvent {
		private final String tag;

		public RemoveFromFilter(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

	}

	public class Loading extends GenericEvent {

		private final boolean loading;

		public Loading(boolean loading) {
			this.loading = loading;
		}

		public boolean isLoading() {
			return loading;
		}

	}

	public class AppVersion extends GenericEvent {
		private final String appVersion;

		public AppVersion(String appVersion) {
			this.appVersion = appVersion;
		}

		public String getAppVersion() {
			return appVersion;
		}
	}

	public class GetAppVersion extends GenericEvent {

	}

	public class GetMostRecentPostInfo extends GenericEvent {

	}

	public class SetMostRecentPostInfo extends GenericEvent {
		private final MostRecentPostInfo info;

		public SetMostRecentPostInfo(MostRecentPostInfo info) {
			this.info = info;
		}

		public MostRecentPostInfo getInfo() {
			return info;
		}
	}

	public class ShowView extends GenericEvent {
		private final View view;

		public ShowView(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}
	}
}
