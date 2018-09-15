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
import gwt.material.design.client.ui.MaterialPanel;
import muksihs.steem.farhorizons.client.Event;
import muksihs.steem.farhorizons.client.FarHorizonsWebApp;
import muksihs.steem.farhorizons.client.GameStats;
import muksihs.steem.farhorizons.client.GameStats.TechLevels;

public class TurnResultsUi extends EventBusComposite {

	private static TurnResultsUiUiBinder uiBinder = GWT.create(TurnResultsUiUiBinder.class);

	interface TurnResultsUiUiBinder extends UiBinder<Widget, TurnResultsUi> {
	}
	
	@UiField
	protected MaterialButton turnResults;
	@UiField
	protected MaterialButton submitOrders;
	@UiField
	protected MaterialButton speciesStatus;
	@UiField
	protected MaterialButton pdfManual;
	@UiField
	protected MaterialPanel gameStatsPanel;

	@EventHandler
	protected void enable(Event.TurnResultsMenuEnable event) {
		turnResults.setEnabled(event.isEnable());
		submitOrders.setEnabled(event.isEnable());
		speciesStatus.setEnabled(event.isEnable());
	}
	
	public TurnResultsUi() {
		initWidget(uiBinder.createAndBindUi(this));
		turnResults.addClickHandler((e)->fireEvent(new Event.WantShowTurnResults()));
		speciesStatus.addClickHandler((e)->fireEvent(new Event.WantSpeciesStatus()));
		submitOrders.addClickHandler((e)->fireEvent(new Event.WantShowOrdersForm(true)));
		pdfManual.addClickHandler((e)->fireEvent(new Event.ShowPdfManual()));
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
	
	@EventHandler
	protected void enableOrderForm(Event.EnableOrderForm event) {
		submitOrders.setEnabled(event.isEnable());
	}

	interface MyEventBinder extends EventBinder<TurnResultsUi>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
