package muksihs.steem.farhorizons.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialNavBrand;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialSideNavDrawer;
import muksihs.steem.farhorizons.client.Event;
import muksihs.steem.farhorizons.client.FarHorizonsWebApp;
import muksihs.steem.farhorizons.client.GameStats;
import muksihs.steem.farhorizons.client.GameStats.TechLevels;

public class HeaderBlock extends EventBusComposite {
	
	@UiField
	protected MaterialLink rawResults;
	@UiField
	protected MaterialLink turnResults;
	@UiField
	protected MaterialLink submitOrders;
	@UiField
	protected MaterialLink speciesStatus;
	@UiField
	protected MaterialLink pdfManual;
	@UiField
	protected MaterialSideNavDrawer menu;
	@UiField
	protected MaterialPanel gameStatsPanel;
	
	@EventHandler
	protected void enableOrderForm(Event.EnableOrderForm event) {
		submitOrders.setEnabled(event.isEnable());
	}
	
	@EventHandler
	protected void enable(Event.TurnResultsMenuEnable event) {
		rawResults.setEnabled(event.isEnable());
		turnResults.setEnabled(event.isEnable());
		submitOrders.setEnabled(event.isEnable());
		speciesStatus.setEnabled(event.isEnable());
	}
	
	@EventHandler
	protected void updateGameStats(Event.UpdateGameStats event) {
		gameStatsPanel.clear();
		if (event.getGameStats()==null) {
			return;
		}
		if (event.getGameStats().getTechLevels()==null) {
			return;
		}
		StringBuilder stats = new StringBuilder();
		GameStats gameStats = event.getGameStats();
		TechLevels techLevels = gameStats.getTechLevels();
		
		StringBuilder title = new StringBuilder();
		title.append(gameStats.getName());
		title.append(" - ");
		title.append("Turn ");
		title.append(gameStats.getTurn());
		String gameId = FarHorizonsWebApp.getGameId();
		if (gameId!=null&&gameId.startsWith("game-")) {
			title.append(" - Game ");
			title.append(gameId.substring("game-".length()));
		}
		title.append(" - ");
		title.append(" @");
		title.append(gameStats.getPlayer());
		
		stats.append("<h4>");
		stats.append(title.toString());
		stats.append("</h4>");
		stats.append("<span style='font-family: monospace;'>");
//		stats.append("<strong>");
//		stats.append(FarHorizonsWebApp.basicEscape(gameStats.getName()).replace(" ", "&nbsp;"));
//		stats.append("</strong>");
//		stats.append(": ");
		stats.append("Mining&nbsp;(MI)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getMining());
		stats.append("</span>");
		stats.append("], ");
		stats.append("Manufacturing&nbsp;(MA)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getManufacturing());
		stats.append("</span>");
		stats.append("], ");
		stats.append("Military&nbsp;(ML)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getMilitary());
		stats.append("</span>");
		stats.append("]<br/>");
		stats.append("Gravitics&nbsp;(GV)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getGravitics());
		stats.append("</span>");
		stats.append("], ");
		stats.append("Life&nbsp;Support&nbsp;(LS)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getLifeSupport());
		stats.append("</span>");
		stats.append("], ");
		stats.append("Biology&nbsp;(BI)&nbsp;[");
		stats.append("<span style='color: blue'>");
		stats.append(techLevels.getBiology());
		stats.append("</span>");
		stats.append("]");
		stats.append("</span>");
		HTML html = new HTML(stats.toString());
		html.getElement().getStyle().setFontSize(175, Unit.PCT);
		gameStatsPanel.add(html);
	}

	interface HeaderBlockUiBinder extends UiBinder<Widget, HeaderBlock> {
	}

	interface MyEventBinder extends EventBinder<HeaderBlock> {
	}
	
	private static String versionTxt = "19000101";

	private static HeaderBlockUiBinder uiBinder = GWT.create(HeaderBlockUiBinder.class);

	@UiField
	protected MaterialNavBrand navBrand;
	
	@UiField
	protected MaterialLabel version;
	@UiField
	protected MaterialButton account;

	public HeaderBlock() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		version.setText(versionTxt);
		account.addClickHandler((e)->fireEvent(new Event.LoginLogout()));
		navBrand.addClickHandler((e)->fireEvent(new Event.ShowAbout()));
		pdfManual.addClickHandler((e)->fireEvent(new Event.ShowPdfManual()));
		turnResults.addClickHandler((e)->fireEvent(new Event.WantShowTurnResults()));
		speciesStatus.addClickHandler((e)->fireEvent(new Event.WantSpeciesStatus()));
		rawResults.addClickHandler((e)->fireEvent(new Event.WantRawTurnResults()));
		submitOrders.addClickHandler((e)->fireEvent(new Event.WantShowOrdersForm(true)));
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetAppVersion());
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}
	
	@EventHandler
	public void showLoggedInStatus(Event.LoginComplete event) {
		if (event.isLoggedIn()) {
			account.setText("LOGOUT");
		} else {
			account.setText("LOGIN");
		}
	}

	@EventHandler
	public void setAppVersion(Event.AppVersion event) {
		version.setText(event.getAppVersion());
		versionTxt = event.getAppVersion();
	}
}
