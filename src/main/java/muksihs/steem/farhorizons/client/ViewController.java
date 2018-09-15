package muksihs.steem.farhorizons.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.CssName;
import gwt.material.design.client.constants.TextAlign;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialTitle;
import gwt.material.design.client.ui.MaterialToast;
import muksihs.steem.farhorizons.client.Event.ShowShipsAndBases;
import muksihs.steem.farhorizons.client.Event.ShowShipsAndBases.SortOrder;
import muksihs.steem.farhorizons.client.GameStats.PlanetScan;
import muksihs.steem.farhorizons.client.GameStats.ScanInfo;
import muksihs.steem.farhorizons.client.cache.GameState;
import muksihs.steem.farhorizons.shared.PlanetInfo;
import muksihs.steem.farhorizons.shared.ShipLocation;
import muksihs.steem.farhorizons.ui.AboutUi;
import muksihs.steem.farhorizons.ui.JoinGameView;
import muksihs.steem.farhorizons.ui.LoginUi;
import muksihs.steem.farhorizons.ui.PostDoneUi;

public class ViewController implements GlobalEventBus {

	interface MyEventBinder extends EventBinder<ViewController> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private final Panel view;

	public ViewController(Panel view) {
		this.view = view;
		eventBinder.bindEventHandlers(this, eventBus);
	}

	@EventHandler
	protected void showShipsAndBases(ShowShipsAndBases event) {
		if (event.getShipLocations() == null || event.getShipLocations().isEmpty()) {
			return;
		}
		List<ShipLocation> ships = new ArrayList<>(event.getShipLocations());
		SortOrder sortOrder = event.getSortOrder();
		if (sortOrder == null) {
			sortOrder = SortOrder.ByName;
		}
		switch (sortOrder) {
		case ByCoordinates:
			Collections.sort(ships, (a, b) -> {
				String x1 = StringUtils.leftPad(a.getX(), 3, "0");
				String x2 = StringUtils.leftPad(b.getX(), 3, "0");
				if (!x1.equals(x2)) {
					return x1.compareTo(x2);
				}
				String y1 = StringUtils.leftPad(a.getY(), 3, "0");
				String y2 = StringUtils.leftPad(b.getY(), 3, "0");
				if (!y1.equals(y2)) {
					return y1.compareTo(y2);
				}
				String z1 = StringUtils.leftPad(a.getZ(), 3, "0");
				String z2 = StringUtils.leftPad(b.getZ(), 3, "0");
				if (!z1.equals(z2)) {
					return z1.compareTo(z2);
				}
				String p1 = StringUtils.leftPad(a.getP(), 3, "0");
				String p2 = StringUtils.leftPad(b.getP(), 3, "0");
				return p1.compareTo(p2);
			});
			break;
		case ByLocation:
			Collections.sort(ships, (a, b) -> {
				String p1 = a.getPlanet();
				String p2 = b.getPlanet();
				return p1.compareTo(p2);
			});
			break;
		case ByName:
			Collections.sort(ships, (a, b) -> {
				String n1 = a.getName();
				String n2 = b.getName();
				return n1.compareTo(n2);
			});
			break;
		}
		StringBuilder shipList = new StringBuilder();
		shipList.append("<div style='text-align: center; overflow-x: auto; padding: 0.5em;'>");
		shipList.append("<table style='margin: auto; border: none; width: auto; table-layout: auto;'>");
		shipList.append("<thead><tr>");
		shipList.append("<th style='padding: 8px; vertical-align: top;'>SHIP or BASE</th>");
		shipList.append("<th style='padding: 8px; vertical-align: top;'>LOCATION</th>");
		shipList.append("<th style='padding: 8px; vertical-align: top;'>COORDINATES</th>");
		shipList.append("<th style='padding: 8px; vertical-align: top;'>STATS</th>");
		shipList.append("</tr></thead>");
		for (ShipLocation ship : ships) {
			shipList.append("<tr>");
			shipList.append("<td style='padding: 8px; vertical-align: top;'>");
			shipList.append(FarHorizonsWebApp.basicEscape(ship.getName()));
			shipList.append("</td>");
			shipList.append("<td style='padding: 8px; vertical-align: top;'>");
			shipList.append(FarHorizonsWebApp.basicEscape(ship.getPlanet()));
			shipList.append("</td>");
			shipList.append("<td style='padding: 8px; vertical-align: top;'>");
			shipList.append(" [");
			shipList.append("<span style='color: darkblue; font-weight: bold;'>");
			shipList.append(ship.getX());
			shipList.append(" ");
			shipList.append(ship.getY());
			shipList.append(" ");
			shipList.append(ship.getZ());
			if (!"0".equals(ship.getP())) {
				shipList.append(" ");
				shipList.append(ship.getP());
			}
			shipList.append("</span>");
			shipList.append("]");
			shipList.append("</td>");

			shipList.append("<td style='padding: 8px; vertical-align: top;'>");
			shipList.append("<ul style='margin: 0px;'>");
			if (!isBlank(ship.getAge())) {
				shipList.append("<li>");
				shipList.append("Age: ");
				shipList.append("<span style='color: darkblue;'>");
				shipList.append(ship.getAge());
				shipList.append("</span>");
				shipList.append("</li>");
			}
			if (!isBlank(ship.getCapacity())) {
				shipList.append("<li>");
				shipList.append("Capacity: ");
				shipList.append("<span style='color: darkblue;'>");
				shipList.append(ship.getCapacity());
				shipList.append("</span>");
				shipList.append("</li>");
			}
			String tons = ship.getTons();
			if (!isBlank(tons)) {
				tons = StringUtils.reverse(tons);
				for (int i = 3 * (tons.length() / 3); i > 0; i -= 3) {
					if (tons.length() > i) {
						tons = tons.substring(0, i) + "," + tons.substring(i);
					}
				}
				tons = StringUtils.reverse(tons);
				shipList.append("<li>");
				shipList.append("Tonnage: ");
				shipList.append("<span style='color: darkblue;'>");
				shipList.append(tons);
				shipList.append("</span>");
				shipList.append("</li>");
			}
			if (!isBlank(ship.getCargo())) {
				shipList.append("<li>");
				shipList.append("Cargo: ");
				shipList.append("<span style='color: darkgreen; font-weight: bold;'>");
				shipList.append(ship.getCargo());
				shipList.append("</span>");
				shipList.append("</li>");
			}
			shipList.append("</ul>");
			shipList.append("</td>");

			shipList.append("</tr>");
		}
		shipList.append("</table></div>");

		MaterialModal modal = new MaterialModal();
		modal.setFontSize(175, Unit.PCT);
		modal.setPadding(4);

		MaterialPanel btnSortSelect = new MaterialPanel();
		btnSortSelect.setTextAlign(TextAlign.CENTER);

		MaterialButton btnSortByName = new MaterialButton("Sort By Name");
		btnSortByName.setMargin(4);
		btnSortByName.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnSortByName.addClickHandler((e) -> {
			modal.close();
			fireEvent(new Event.ShowShipsAndBases(ships, SortOrder.ByName));
		});

		MaterialButton btnSortByLocation = new MaterialButton("Sort By Location");
		btnSortByLocation.setMargin(4);
		btnSortByLocation.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnSortByLocation.addClickHandler((e) -> {
			modal.close();
			fireEvent(new Event.ShowShipsAndBases(ships, SortOrder.ByLocation));
		});

		MaterialButton btnSortByCoordinates = new MaterialButton("Sort By Coordinates");
		btnSortByCoordinates.setMargin(4);
		btnSortByCoordinates.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnSortByCoordinates.addClickHandler((e) -> {
			modal.close();
			fireEvent(new Event.ShowShipsAndBases(ships, SortOrder.ByCoordinates));
		});

		btnSortSelect.add(btnSortByName);
		btnSortSelect.add(btnSortByLocation);
		btnSortSelect.add(btnSortByCoordinates);

		MaterialPanel panel = new MaterialPanel();
		panel.setTextAlign(TextAlign.CENTER);
		panel.add(new HTML("<div style='font-family: monospace;'>" + shipList.toString() + "</div>"));

		MaterialButton btnDismiss = new MaterialButton("DISMISS");
		btnDismiss.setMargin(4);
		btnDismiss.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnDismiss.addClickHandler((e) -> modal.close());

		MaterialPanel btnPanel = new MaterialPanel();
		btnPanel.setTextAlign(TextAlign.RIGHT);

		btnPanel.add(btnDismiss);

		modal.add(btnPanel);
		modal.add(btnSortSelect);
		modal.add(panel);

		modal.addCloseHandler((e) -> {
			modal.removeFromParent();
		});
		modal.setFullscreen(true);
		RootPanel.get().add(modal);
		modal.open();
	}
	
	@EventHandler
	protected void setBrowserTitle(Event.SetBrowserTitle event) {
		if (event.getTitle()!=null && !event.getTitle().trim().isEmpty()) {
			Window.setTitle(event.getTitle());
		} else {
			Window.setTitle("Far Horizons Steem");
		}
		
	}

	@EventHandler
	protected void helperNamePlanets(Event.HelperNamePlanets event) {
		List<ScanInfo> scannedPlanets = event.getScannedPlanets();
		if (scannedPlanets.isEmpty()) {
			return;
		}
		boolean colonizable = event.isColonizable();
		Iterator<ScanInfo> iScans = scannedPlanets.iterator();
		Set<String> alreadyColonized = new HashSet<>();
		List<PlanetInfo> planetInfo = event.getGameStats().getPlanetInfo();
		if (colonizable) {
			for (PlanetInfo info : planetInfo) {
				if (info.isColony()) {
					alreadyColonized.add(info.getName());
				}
			}
		}
		scans: while (iScans.hasNext()) {
			ScanInfo scan = iScans.next();
			for (PlanetScan planet : scan.getPlanets()) {
				String name = planet.getName();
				boolean isColonizable = planet.isColonizable();
				if (colonizable) {
					if (isColonizable && !alreadyColonized.contains(name)) {
						continue scans;
					}
				} else {
					if (isBlank(name)) {
						continue scans;
					}
				}
			}
			iScans.remove();
		}
		GameState gameState = new GameState(event.getGameStats());
		List<MaterialTextBox> nameInputs = new ArrayList<>();
		MaterialModal modal = new MaterialModal();
		modal.setFontSize(175, Unit.PCT);
		String textInstruct1 = "The following planets need names:";
		if (colonizable) {
			textInstruct1 = "The following planets can be colonized:";
		}
		MaterialLabel instruct = new MaterialLabel(textInstruct1);
		instruct.setMargin(4);
		instruct.setFontSize(125, Unit.PCT);
		instruct.setFontWeight(FontWeight.BOLD);
		instruct.setTextAlign(TextAlign.CENTER);
		modal.add(instruct);
		MaterialLabel instruct2 = new MaterialLabel("You don't need to add the PL in front of each name.");
		instruct2.setMargin(4);
		instruct2.setFontSize(90, Unit.PCT);
		instruct2.setTextAlign(TextAlign.CENTER);
		modal.add(instruct2);
		iScans = scannedPlanets.iterator();
		Set<String> systemsAlready = new HashSet<>();
		while (iScans.hasNext()) {
			ScanInfo system = iScans.next();
			if (system.getPlanets().isEmpty()) {
				continue;
			}
			String systemTag = system.getX() + " " + system.getY() + " " + system.getZ();
			if (systemsAlready.contains(systemTag)) {
				continue;
			}
			systemsAlready.add(systemTag);
			MaterialPanel systemPanel = new MaterialPanel();
			systemPanel.setMargin(10);
			systemPanel.setPadding(0);
			systemPanel.setBorder("2px solid darkblue");
			modal.add(systemPanel);
			String tmp = "";
			int planetCount = system.getPlanets().size();
			tmp += "Star of stellar type " + system.getStar();
			tmp += " with " + planetCount + (planetCount == 1 ? " planet" : " planets");
			tmp += " at x=" + system.getX() + " y=" + system.getY() + " z=" + system.getZ();
			MaterialLabel coordinates = new MaterialLabel(tmp);
			coordinates.setMargin(4);
			coordinates.setFontSize(110, Unit.PCT);
			coordinates.setFontWeight(FontWeight.BOLD);
			coordinates.setTextAlign(TextAlign.CENTER);
			systemPanel.add(coordinates);
			for (PlanetScan planet : system.getPlanets()) {
				if (colonizable) {
					if (!planet.isColonizable()) {
						continue;
					}
				}
				String planetTag = systemTag + " " + planet.getId();
				MaterialPanel namePanel = new MaterialPanel();
				systemPanel.add(namePanel);
				namePanel.setMargin(5);
				namePanel.setPadding(10);
				namePanel.setBorder("2px solid gray");
				StringBuilder sb = new StringBuilder();
				sb.append("#");
				sb.append(planet.getId());
				sb.append(" LSN: ");
				sb.append(planet.getLifeSupportNumber());
				sb.append(", Mining: ");
				sb.append(planet.getMiningDifficulty());
				sb.append(", Gravity: ");
				sb.append(planet.getGravity());
				MaterialLabel planetInfoA = new MaterialLabel(sb.toString());
				if (planet.isColonizable()) {
					planetInfoA.setBackgroundColor(Color.GREEN_LIGHTEN_2);
				}
				namePanel.add(planetInfoA);
				sb.setLength(0);
				sb.append("Diameter: ");
				sb.append(planet.getDiameter());
				sb.append(", Temperature: ");
				sb.append(planet.getTempuratureClassification());
				sb.append(", Pressure: ");
				sb.append(planet.getPressureClassification());
				namePanel.add(new MaterialLabel(sb.toString()));
				sb.setLength(0);
				sb.append("Atmosphere: ");
				sb.append(planet.getAtmosphere());
				namePanel.add(new MaterialLabel(sb.toString()));
				sb.setLength(0);

				MaterialTextBox name = new MaterialTextBox();
				namePanel.add(name);

				name.addStyleName(CssName.CLEARFIX);
				if (name.getElement().getChildCount() > 0) {
					((Element) name.getElement().getChild(0)).getStyle().setFontSize(100, Unit.PCT);
				}
				name.setMargin(2);
				name.setPadding(10);
				name.setBorder("2px dashed gray");
				if (!isBlank(planet.getName())) {
					name.setValue(planet.getName());
					name.setEnabled(false);
				} else {
					name.setDataAttribute("coordinates", planetTag);
					nameInputs.add(name);
					if (gameState.containsKey(planetTag)) {
						name.setValue(gameState.get(planetTag));
					} else {
						name.setValue("");
					}
				}
				name.setOverflow(Overflow.AUTO);
			}
		}
		MaterialPanel btnPanel = new MaterialPanel();
		btnPanel.setTextAlign(TextAlign.RIGHT);
		MaterialButton cancel = new MaterialButton("CANCEL");
		cancel.setMargin(4);
		cancel.getElement().getStyle().setBackgroundColor("DarkBlue");
		cancel.addClickHandler((e) -> modal.close());
		MaterialButton submit = new MaterialButton("SUBMIT");
		submit.setMargin(4);
		submit.getElement().getStyle().setBackgroundColor("DarkBlue");
		submit.addClickHandler((e) -> {
			List<String> nameCommands = new ArrayList<>();
			Set<String> already = new HashSet<>();
			for (MaterialTextBox nameInput : nameInputs) {
				String name = nameInput.getValue().trim();
				if (isBlank(name)) {
					continue;
				}
				if (name.contains(",")) {
					fireEvent(new Event.AlertMessage("Planet names must not contain commas."));
					return;
				}
				if (!name.toLowerCase().startsWith("pl ")) {
					name = "PL " + name;
				}
				if (already.contains(name.toLowerCase())) {
					fireEvent(new Event.AlertMessage("Planet names must unique!"));
					return;
				}
				already.add(name.toLowerCase());
				String planetTag = nameInput.getDataAttribute("coordinates");
				gameState.put(planetTag, name);
				nameCommands.add(planetTag + " " + name);
			}
			fireEvent(new Event.CommandNamesAdd(nameCommands));
			modal.close();
		});
		btnPanel.add(cancel);
		btnPanel.add(submit);
		modal.add(btnPanel);
		modal.addCloseHandler((e) -> {
			modal.removeFromParent();
		});
		RootPanel.get().add(modal);
		modal.open();
	}

	private static boolean isBlank(String name) {
		return name == null || name.trim().isEmpty();
	}

	@EventHandler
	protected void postDoneUi(Event.PostDone event) {
		PostDoneUi doneUi = new PostDoneUi();
		RootPanel.get().add(doneUi);
		doneUi.open();
	}

	@EventHandler
	protected void quickMessage(Event.QuickMessage event) {
		MaterialToast.fireToast(event.getMessage(), 1000);
	}

	@EventHandler
	protected void showAboutUi(Event.ShowAbout event) {
		AboutUi about = new AboutUi();
		RootPanel.get().add(about);
		about.open();
	}

	@EventHandler
	protected void showLoginUi(Event.ShowLoginUi event) {
		LoginUi loginUi = new LoginUi();
		RootPanel.get().add(loginUi);
		loginUi.open();
	}

	@EventHandler
	protected void alertMessage(Event.AlertMessage event) {
		MaterialModal modal = new MaterialModal();
		modal.setDismissible(true);
		modal.setTitle("Alert!");
		modal.addCloseHandler((e) -> modal.removeFromParent());
		MaterialTitle title = new MaterialTitle(event.getMessage());
		title.setMarginTop(-50);
		modal.add(title);
		MaterialButton btnOk = new MaterialButton("OK");
		btnOk.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnOk.setMargin(4);
		btnOk.addClickHandler((e) -> modal.close());
		modal.add(btnOk);
		RootPanel.get().add(modal);
		modal.open();
	}

	@EventHandler
	protected void joinGame(Event.ShowGameJoinForm event) {
		replaceView(new JoinGameView());
		fireEvent(new Event.UpdateGameStats(null));
	}

	@EventHandler
	protected void helpMessage(Event.HelpMessage event) {
		MaterialModal modal = new MaterialModal();
		modal.setDismissible(true);
		modal.setTitle("Help");
		modal.addCloseHandler((e) -> modal.removeFromParent());
		MaterialTitle title = new MaterialTitle(event.getTitle());
		title.setMarginTop(-50);
		modal.add(title);
		MaterialButton btnOk = new MaterialButton("OK");
		btnOk.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnOk.setMargin(4);
		btnOk.addClickHandler((e) -> modal.close());
		modal.add(btnOk);
		HTML html = new HTML(event.getHtml());
		html.getElement().getStyle().setFontSize(150, Unit.PCT);
		modal.add(html);
		RootPanel.get().add(modal);
		modal.open();
	}

	@EventHandler
	protected void fatalError(Event.FatalError event) {
		MaterialModal modal = new MaterialModal();
		modal.setTitle("FATAL ERROR!");
		modal.setBackgroundColor(Color.RED);
		MaterialTitle title = new MaterialTitle("FATAL ERROR!");
		title.setMarginTop(-50);
		modal.add(title);
		MaterialLabel label = new MaterialLabel(event.getMessage());
		modal.add(label);
		label = new MaterialLabel("* App will reload in a moment *");
		modal.add(label);
		modal.setDismissible(false);
		modal.addCloseHandler((e) -> modal.removeFromParent());
		RootPanel.get().add(modal);
		modal.open();
	}

	@EventHandler
	protected void showLoading(Event.Loading event) {
		MaterialLoader.loading(event.isLoading());
	}

	@EventHandler
	protected void clearView(Event.ClearView event) {
		if (activeView != null) {
			activeView.removeFromParent();
		}
		activeView = null;
	}

	private Composite activeView;

	private void replaceView(Composite newView) {
		if (activeView != null) {
			activeView.removeFromParent();
		}
		activeView = newView;
		this.view.clear();
		this.view.add(newView);
	}

	@EventHandler
	protected void showView(Event.ShowView event) {
	}

}
