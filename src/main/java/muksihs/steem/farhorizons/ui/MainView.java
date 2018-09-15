package muksihs.steem.farhorizons.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import gwt.material.design.client.constants.CssName;
import gwt.material.design.client.constants.Display;
import gwt.material.design.client.constants.TextAlign;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialTextArea;
import gwt.material.design.client.ui.MaterialTextArea.ResizeRule;
import gwt.material.design.client.ui.MaterialTitle;
import gwt.material.design.client.ui.html.Br;
import gwt.material.design.client.ui.html.Hr;
import muksihs.steem.farhorizons.client.Event;
import muksihs.steem.farhorizons.client.Event.ShowShipsAndBases;
import muksihs.steem.farhorizons.client.FarHorizonsWebApp;
import muksihs.steem.farhorizons.client.GameStats;
import muksihs.steem.farhorizons.client.GameStats.PlanetScan;
import muksihs.steem.farhorizons.client.GameStats.ScanInfo;
import muksihs.steem.farhorizons.client.cache.OrderFormCache;
import muksihs.steem.farhorizons.client.rsc.OrderFormResources;
import muksihs.steem.farhorizons.shared.ExtractDetailedGameInfo;
import muksihs.steem.farhorizons.shared.OrderFormPart;
import muksihs.steem.farhorizons.shared.PlanetInfo;
import muksihs.steem.farhorizons.shared.ShipLocation;

public class MainView extends EventBusComposite {

	@UiField
	protected MaterialPanel display;

	public MaterialPanel getPanel() {
		return display;
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	interface MyEventBinder extends EventBinder<MainView> {
	};

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	private final List<MaterialTextArea> orderFormParts = new ArrayList<>();
	private MaterialButton submitBtn;
	private MaterialButton resetBtn;
	private MaterialButton loadPreviousOrdersBtn;
	private MaterialPanel orderForm;
	private GameStats gameStats;

	private OrderFormCache orderCache;

	@EventHandler
	protected void updateGameStatus(Event.UpdateGameStats event) {
		this.gameStats = event.getGameStats();
		orderCache = new OrderFormCache(gameStats);
	}

	@EventHandler
	protected void commandNamesAdd(Event.CommandNamesAdd event) {
		if (orderFormParts.isEmpty() || orderForm == null) {
			return;
		}
		for (MaterialTextArea part : orderFormParts) {
			if (!part.getDataAttribute("section").equals("PRE-DEPARTURE")) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			for (String cmd : event.getNameCommands()) {
				sb.append("NAME ");
				sb.append(cmd);
				sb.append("\n");
			}
			String cmdText = part.getValue();
			String cmds[] = StringUtils.splitPreserveAllTokens(cmdText, "\n");
			for (String cmd : cmds) {
				if (cmd.trim().toLowerCase().startsWith("name")) {
					continue;
				}
				sb.append(cmd);
				sb.append("\n");
			}
			part.setValue(sb.toString(), true);
			part.triggerAutoResize();
		}
	}

	@EventHandler
	protected void clearOutOrders(Event.LoadTurnResults event) {
		orderForm = null;
		orderFormParts.clear();
	}

	@EventHandler
	protected void showOrdersForm(Event.ShowOrdersForm event) {
		if (event.getOrderFormParts() == null || event.getOrderFormParts().isEmpty()) {
			return;
		}
		/*
		 * additional automatic work to have tr2 or pb ships always scan
		 */
		postArrivalScanFix: for (OrderFormPart part : event.getOrderFormParts()) {
			if (!part.getSection().equalsIgnoreCase("POST-ARRIVAL")) {
				continue postArrivalScanFix;
			}
			ListIterator<String> iLines = Arrays.asList(StringUtils.splitPreserveAllTokens(part.getOrders(), "\n"))
					.listIterator();
			List<String> lines = new ArrayList<>();
			// remove existing "scan" orders for "tr1", "tr2", and "pb" ships.
			stripAutos: while (iLines.hasNext()) {
				String line = iLines.next();
				String lcLine = line.trim().toLowerCase();
				if (lcLine.startsWith("scan ")) {
					lcLine = lcLine.substring("scan ".length()).trim();
					if (lcLine.startsWith("tr1 ") || lcLine.startsWith("tr2 ") || lcLine.startsWith("pb ")) {
						continue stripAutos;
					}
				}
				lines.add(line);
			}
			// add new "scan" orders for "tr2" and "pb" ships.
			for (ShipLocation ship : gameStats.getShipLocations()) {
				String name = ship.getName();
				String lcName = name.toLowerCase();
				if (lcName.startsWith("tr1 ") || lcName.startsWith("tr2 ") || lcName.startsWith("pb ")) {
					lines.add("     Scan " + name + "; AUTO SCAN");
				}
			}
			part.setOrders(StringUtils.join(lines, "\n"));
			break postArrivalScanFix;
		}

		/*
		 * comment bogus auto created jump commands that have ships jumping to a system
		 * they are alreading located at, also comment out jump commands with "???" as
		 * the destination
		 */
		jumpsFix: for (OrderFormPart part : event.getOrderFormParts()) {
			if (!part.getSection().equalsIgnoreCase("JUMPS")) {
				continue jumpsFix;
			}
			ListIterator<String> iLines = Arrays.asList(StringUtils.splitPreserveAllTokens(part.getOrders(), "\n"))
					.listIterator();
			List<String> lines = new ArrayList<>();
			jumpOrders: while (iLines.hasNext()) {
				boolean wasCommented = false;
				String line = iLines.next();
				String lcLine = StringUtils.normalizeSpace(line.trim().toLowerCase());
				if (lcLine.startsWith(";jump")) {
					line = line.substring(1);
					lcLine = lcLine.substring(1);
					wasCommented = true;
				}
				if (!lcLine.startsWith("jump ")) {
					lines.add(line);
					continue jumpOrders;
				}
				String jump = line;
				if (jump.contains(";")) {
					jump = StringUtils.substringBefore(jump, ";");
				}
				jump = jump.replace(",", ", ").trim().toLowerCase();
				jump = StringUtils.normalizeSpace(jump);
				// bad jump check
				for (ShipLocation ship : gameStats.getShipLocations()) {
					String badJump = "jump " + ship.getName() + ", " + ship.getPlanet();
					badJump = StringUtils.normalizeSpace(badJump.trim().toLowerCase());
					if (badJump.equals(jump)) {
						line = ";" + line + "; === ALREADY AT THIS LOCATION";
						lines.add(line);
						continue jumpOrders;
					}
				}
				if (line.contains(";")) {
					line = StringUtils.substringBeforeLast(line, ";");
				}
				// no jump check, add current location, make sure is commented out
				if (line.contains(", ???")) {
					for (ShipLocation ship : gameStats.getShipLocations()) {
						String noJump = "jump " + ship.getName() + ", ???";
						noJump = StringUtils.normalizeSpace(noJump.trim().toLowerCase());
						if (noJump.equals(jump)) {
							String planet = ship.getPlanet();
							planet += " [" + ship.getX() + " " + ship.getY() + " " + ship.getZ() + " "+ ship.getP() + "]";
							line = ";" + line.trim() + "; " + planet;
							lines.add(line);
							continue jumpOrders;
						}
					}
				}
				// add current location
				for (ShipLocation ship : gameStats.getShipLocations()) {
					String anyJump = "jump " + ship.getName() + ",";
					anyJump = StringUtils.normalizeSpace(anyJump.trim().toLowerCase());
					if (jump.startsWith(anyJump)) {
						String planet = ship.getPlanet();
						planet += " [" + ship.getX() + " " + ship.getY() + " " + ship.getZ() + " " + ship.getP() + "]";
						line = line.trim() + "; " + planet;
						if (wasCommented) {
							line = ";" + line;
						}
						lines.add(line);
						continue jumpOrders;
					}
				}
				// pass through existing jump command
				if (wasCommented) {
					line = ";" + line;
				}
				lines.add(line);
			}
			part.setOrders(StringUtils.join(lines, "\n"));
			break;
		}

		/*
		 * display orders form
		 */
		if (orderFormParts.isEmpty() || orderForm == null) {
			orderForm = new MaterialPanel();
			for (OrderFormPart part : event.getOrderFormParts()) {
				MaterialPanel orderFormSection = new MaterialPanel();
				orderFormSection.setMargin(10);
				orderFormSection.setPadding(10);
				orderFormSection.setBorder("2px solid gray");
				orderFormSection.setFontSize(175, Unit.PCT);
				orderForm.add(orderFormSection);
				String sectionName = part.getSection();
				MaterialLabel materialLabel = new MaterialLabel(sectionName);
				materialLabel.getElement().getStyle().setColor("darkred");
				materialLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
				MaterialLabel section = materialLabel;
				MaterialTextArea input = new MaterialTextArea();
				input.addStyleName(CssName.CLEARFIX);
				orderFormParts.add(input);
				if (input.getElement().getChildCount() > 0) {
					((Element) input.getElement().getChild(0)).getStyle().setFontSize(100, Unit.PCT);
				}
				input.setMargin(2);
				input.setPadding(10);
				input.setBorder("2px dashed gray");
				input.setDataAttribute("section", sectionName);
				if (orderCache.containsKey(sectionName)) {
					input.setValue(orderCache.get(sectionName), false);
				} else {
					input.setValue(part.getOrders(), true);
				}
				input.addValueChangeHandler(cacheOrdersValueChangeHandler(part.getSection(), input));
				input.addKeyPressHandler(cacheOrdersKeyPressHandler(part.getSection(), input));
				input.setResizeRule(ResizeRule.AUTO);
				input.setOverflow(Overflow.AUTO);
				input.triggerAutoResize();
				MaterialPanel box = new MaterialPanel();
				box.addStyleName(CssName.CLEARFIX);
				MaterialPanel left = new MaterialPanel();
				MaterialPanel right = new MaterialPanel();
				right.setTextAlign(TextAlign.RIGHT);
				left.add(section);
				MaterialPanel displayBox = new MaterialPanel();
				displayBox.addStyleName(CssName.CLEARFIX);
				MaterialButton btnCommands = new MaterialButton("Show Commands");
				btnCommands.getElement().getStyle().setBackgroundColor("darkgreen");
				btnCommands.setMargin(4);
				btnCommands.addClickHandler((e) -> showCommandHelp(btnCommands, sectionName, displayBox));
				right.add(btnCommands);
				left.setDisplay(Display.INLINE_BLOCK);
				right.setDisplay(Display.INLINE_BLOCK);
				left.setFloat(Style.Float.LEFT);
				right.setFloat(Style.Float.RIGHT);
				box.add(left);
				box.add(right);
				orderFormSection.add(box);
				orderFormSection.add(displayBox);
				orderFormSection.add(input);
			}
		}

		MaterialPanel submitPanel = new MaterialPanel();
		submitPanel.setTextAlign(TextAlign.RIGHT);
		submitPanel.setMarginTop(2);

		loadPreviousOrdersBtn = new MaterialButton("LOAD PREVIOUS ORDERS");
		loadPreviousOrdersBtn.setMargin(2);
		loadPreviousOrdersBtn.getElement().getStyle().setBackgroundColor("darkblue");
		loadPreviousOrdersBtn.addClickHandler(this::loadPreviousOrderForm);
		submitPanel.add(loadPreviousOrdersBtn);

		resetBtn = new MaterialButton("RESET ORDER FORM");
		resetBtn.setMargin(2);
		resetBtn.getElement().getStyle().setBackgroundColor("darkblue");
		resetBtn.addClickHandler(this::resetOrderForm);
		submitPanel.add(resetBtn);
		submitBtn = new MaterialButton("SUBMIT ORDERS");
		submitBtn.setMargin(2);
		submitBtn.getElement().getStyle().setBackgroundColor("darkblue");
		submitBtn.addClickHandler(this::submitOrders);
		submitPanel.add(submitBtn);
		display.clear();
		MaterialPanel helpers = new MaterialPanel();
		helpers.setTextAlign(TextAlign.CENTER);

		if (gameStats.getShipLocations() != null && !gameStats.getShipLocations().isEmpty()) {
			MaterialButton shipsBtn = new MaterialButton("Ships and Bases");
			shipsBtn.addClickHandler((e) -> {
				fireEvent(
						new Event.ShowShipsAndBases(gameStats.getShipLocations(), ShowShipsAndBases.SortOrder.ByName));
			});
			shipsBtn.setMargin(2);
			shipsBtn.getElement().getStyle().setBackgroundColor("DarkBlue");
			helpers.add(shipsBtn);
		}

		List<ScanInfo> scannedPlanets = gameStats.getScannedPlanets();
		if (!scannedPlanets.isEmpty()) {
			boolean showNamePlanetButton = false;
			boolean showColonizablePlanetButton = false;
			Set<String> alreadyColonized = new HashSet<>();
			for (PlanetInfo info : gameStats.getPlanetInfo()) {
				if (info.isColony()) {
					alreadyColonized.add(info.getName());
				}
			}
			infoScanCheck: for (ScanInfo scanned : scannedPlanets) {
				for (PlanetScan scannedPlanet : scanned.getPlanets()) {
					String name = scannedPlanet.getName();
					boolean colonizable = scannedPlanet.isColonizable();
					if (isBlank(name)) {
						showNamePlanetButton = true;
					}
					if (colonizable && !alreadyColonized.contains(name)) {
						showColonizablePlanetButton = true;
					}
					if (showNamePlanetButton && showColonizablePlanetButton) {
						//no more checking needed
						break infoScanCheck;
					}
				}
			}
			if (showColonizablePlanetButton) {
				MaterialButton colonizePlanetsBtn = new MaterialButton("Colonizable Planets Detected");
				colonizePlanetsBtn.setMargin(2);
				colonizePlanetsBtn.getElement().getStyle().setBackgroundColor("darkgreen");
				colonizePlanetsBtn
				.addClickHandler((e) -> fireEvent(new Event.HelperNamePlanets(gameStats, scannedPlanets, true)));
				helpers.add(colonizePlanetsBtn);
			}
			if (showNamePlanetButton) {
				MaterialButton namePlanetsBtn = new MaterialButton("Unknown Planets Detected");
				namePlanetsBtn.setMargin(2);
				namePlanetsBtn.getElement().getStyle().setBackgroundColor("darkred");
				namePlanetsBtn
						.addClickHandler((e) -> fireEvent(new Event.HelperNamePlanets(gameStats, scannedPlanets, false)));
				helpers.add(namePlanetsBtn);
			}
		}
		if (!gameStats.getPlanetInfo().isEmpty()) {

		}
		display.add(helpers);
		display.add(orderForm);
		display.add(submitPanel);
	}

	private KeyPressHandler cacheOrdersKeyPressHandler(String section, MaterialTextArea input) {
		return (event) -> {
			Scheduler.get().scheduleDeferred(() -> orderCache.put(section, input.getValue()));
		};
	}

	private ValueChangeHandler<String> cacheOrdersValueChangeHandler(final String section,
			final MaterialTextArea input) {
		return (event) -> {
			Scheduler.get().scheduleDeferred(() -> orderCache.put(section, input.getValue()));
		};
	}

	private static boolean isBlank(String name) {
		return name == null || name.trim().isEmpty();
	}

	private Void showCommandHelp(MaterialButton btnCommands, String sectionName, MaterialPanel displayBox) {
		DomGlobal.console.log("Command help toggle: " + sectionName);
		if (sectionName == null || sectionName.trim().isEmpty()) {
			return null;
		}
		if (displayBox.getChildren().size() > 0) {
			btnCommands.setText("Show Commands");
			displayBox.clear();
			return null;
		}

		String cmdText = OrderFormResources.INSTANCE.validCommands().getText();
		if (!cmdText.contains("*")) {
			return null;
		}
		String[] cmdSections = StringUtils.split(cmdText, "*");
		for (String section : cmdSections) {
			if (!section.toLowerCase().startsWith(sectionName.toLowerCase() + "\n")) {
				continue;
			}
			section = StringUtils.substringAfter(section, sectionName);
			String[] cmds = StringUtils.split(section, "\n");
			MaterialPanel btnPanel = new MaterialPanel();
			btnPanel.setTextAlign(TextAlign.CENTER);
			for (String cmd : cmds) {
				cmd = cmd.trim();
				String info;
				if (cmd.contains(" ")) {
					info = StringUtils.substringAfter(cmd, " ").trim();
				} else {
					info = "";
				}
				MaterialButton btnCmd = new MaterialButton(cmd);
				btnCmd.setMargin(4);
				btnCmd.getElement().getStyle().setBackgroundColor("gray");
				if (!info.trim().isEmpty()) {
					btnCmd.setHoverable(true);
					btnCmd.setTitle(info);
				}
				final String tmpCmd = cmd;
				btnCmd.addClickHandler((e) -> showHelp(btnCmd, sectionName, tmpCmd));
				btnPanel.add(btnCmd);
			}
			displayBox.add(btnPanel);
			btnCommands.setText("Hide Commands");
			break;
		}
		return null;
	}

	private Void showHelp(MaterialButton btnCmd, String sectionName, String cmd) {
		fireEvent(new Event.ShowCommandHelp(sectionName, cmd));
		return null;
	}

	private void submitOrders(ClickEvent event) {
		submitBtn.setEnabled(false);
		resetBtn.setEnabled(false);
		for (MaterialTextArea part : orderFormParts) {
			part.setEnabled(false);
		}
		List<OrderFormPart> orderFormSections = new ArrayList<>();
		for (MaterialTextArea part : orderFormParts) {
			OrderFormPart ofp = new OrderFormPart();
			ofp.setSection(part.getDataAttribute("section"));
			ofp.setOrders(part.getValue());
			orderFormSections.add(ofp);
		}
		fireEvent(new Event.SubmitOrders(orderFormSections));
	}

	private void resetOrderForm(ClickEvent event) {
		display.clear();
		orderFormParts.clear();
		orderForm.clear();
		orderForm = null;
		orderCache.clear();
		fireEvent(new Event.WantShowOrdersForm(false));
	}

	private void loadPreviousOrderForm(ClickEvent event) {
		display.clear();
		orderFormParts.clear();
		orderForm.clear();
		orderForm = null;
		orderCache.clear();
		fireEvent(new Event.WantShowOrdersForm(true));
	}

	@EventHandler
	protected void onOrdersSubmitIncomplete(Event.OrdersSubmitIncomplete event) {
		resetBtn.setEnabled(true);
		submitBtn.setEnabled(true);
		for (MaterialTextArea part : orderFormParts) {
			part.setEnabled(true);
		}
	}

	@EventHandler
	protected void onJoinGameSubmitDone(Event.JoinGameSubmitDone event) {
		MaterialModal modal = new MaterialModal();
		modal.setDismissible(true);
		modal.setTitle("JOIN REQUEST SUBMITTED!");
		modal.addCloseHandler((e) -> modal.removeFromParent());
		MaterialTitle title = new MaterialTitle("JOIN REQUEST SUBMITTED!");
		title.setMarginTop(-50);
		modal.add(title);
		String orders = event.getOrders();
		orders = orders.replace("  ", "&nbsp; ");
		orders = orders.replace("  ", "&nbsp; ");
		orders = orders.replace("\n", "<br/>");
		orders = "<div style='font-family: monospace;'>" + orders + "</div>";
		MaterialPanel panel = new MaterialPanel();
		panel.setFontSize(175, Unit.PCT);
		panel.add(new HTML(orders));
		modal.add(panel);
		MaterialButton btnOk = new MaterialButton("OK");
		btnOk.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnOk.setMargin(4);
		btnOk.addClickHandler((e) -> modal.close());
		modal.add(btnOk);
		RootPanel.get().add(modal);
		modal.open();
	}

	@EventHandler
	protected void onOrdersSubmitDone(Event.OrdersSubmitDone event) {
		MaterialModal modal = new MaterialModal();
		modal.setFullscreen(true);
		modal.setDismissible(true);
		modal.setTitle("ORDERS SUBMITTED!");
		modal.addCloseHandler((e) -> modal.removeFromParent());
		MaterialTitle title = new MaterialTitle("ORDERS SUBMITTED!");
		title.setMarginTop(-50);
		modal.add(title);
		String orders = event.getOrders();
		orders = orders.replace("  ", "&nbsp; ");
		orders = orders.replace("  ", "&nbsp; ");
		orders = orders.replace("\n", "<br/>");
		orders = "<div style='font-family: monospace;'>" + orders + "</div>";
		MaterialPanel panel = new MaterialPanel();
		panel.setFontSize(175, Unit.PCT);
		panel.add(new HTML(orders));
		MaterialPanel btnPanel = new MaterialPanel();
		btnPanel.setTextAlign(TextAlign.RIGHT);
		MaterialButton btnDismiss = new MaterialButton("DISMISS");
		btnDismiss.getElement().getStyle().setBackgroundColor("DarkBlue");
		btnDismiss.setMargin(4);
		btnDismiss.addClickHandler((e) -> {
			resetBtn.setEnabled(true);
			submitBtn.setEnabled(true);
			for (MaterialTextArea part : orderFormParts) {
				part.setEnabled(true);
			}
			modal.close();
		});
		btnPanel.add(btnDismiss);
		modal.add(btnPanel);
		modal.add(panel);
		RootPanel.get().add(modal);
		modal.open();
	}

	@EventHandler
	protected void showTurnResult(Event.ShowTurnResult event) {
		display.clear();
		String report = event.getReport();

		MaterialPanel reportPanel = new MaterialPanel();
		MaterialPanel showPartBtns = new MaterialPanel();
		showPartBtns.setTextAlign(TextAlign.CENTER);
		showPartBtns.add(new Hr());

		String notice;
		if (report.contains("Combat orders:")) {
			notice = StringUtils.substringBefore(report, "Combat orders:");
		} else {
			notice = StringUtils.substringBefore(report, "SPECIES STATUS");
		}
		addReportPart(reportPanel, showPartBtns, "Notice", notice);

		String combatOrders = StringUtils.substringBetween(report, "Combat orders:", "Pre-departure orders:");
		addReportPart(reportPanel, showPartBtns, "Combat Results", combatOrders);

		String preDepartureOrders = StringUtils.substringBetween(report, "Pre-departure orders:", "Jump orders:");
		addReportPart(reportPanel, showPartBtns, "Pre-Departure Results", preDepartureOrders);

		String jumpOrders = StringUtils.substringBetween(report, "Jump orders:", "Production orders:");
		addReportPart(reportPanel, showPartBtns, "Jump Results", jumpOrders);

		String productionOrders = StringUtils.substringBetween(report, "Production orders:", "Post-arrival orders:");
		addReportPart(reportPanel, showPartBtns, "Production Results", productionOrders);

		String postArrivalOrders = StringUtils.substringBetween(report, "Post-arrival orders:", "Strike orders:");
		addReportPart(reportPanel, showPartBtns, "Post Arrival Results", postArrivalOrders);

		String strikeOrders = StringUtils.substringBetween(report, "Strike orders:", "Other events:");
		addReportPart(reportPanel, showPartBtns, "Strike Results", strikeOrders);

		String otherEvents;
		if (report.contains("SPECIES STATUS")) {
			otherEvents = StringUtils.substringBetween(report, "Other events:", "SPECIES STATUS");
		} else {
			otherEvents = StringUtils.substringAfter(report, "Other events:");
		}
		addReportPart(reportPanel, showPartBtns, "Other Events", otherEvents);
		showPartBtns.add(new Hr());

		display.add(showPartBtns);
		display.add(reportPanel);

	}

	private void addReportPart(MaterialPanel reportPanel, MaterialPanel showPartBtns, String btnText, String report) {
		if (StringUtils.isBlank(report)) {
			return;
		}
		MaterialButton btn = new MaterialButton();
		showPartBtns.add(btn);
		btn.setMargin(2);
		btn.getElement().getStyle().setBackgroundColor("DarkGreen");
		if (btnText.toLowerCase().contains("combat") || btnText.toLowerCase().contains("strike")) {
			btn.getElement().getStyle().setBackgroundColor("DarkRed");
		}
		btn.setText(btnText);
		String tmpPart = report;
		tmpPart = FarHorizonsWebApp.basicEscape(tmpPart);
		tmpPart = tmpPart.replace("\t", "     ");
		tmpPart = tmpPart.replace("  ", "&nbsp; ");
		tmpPart = tmpPart.replace("  ", "&nbsp; ");
		tmpPart = tmpPart.replace("\n", "<br/>");
		tmpPart = highlightErrors(tmpPart);
		tmpPart = highlightNumbers(tmpPart);
		MaterialPanel panel = new MaterialPanel();
		panel.add(new HTML("<div style='font-size: 175%; font-family: monospace;'>" + tmpPart + "</div>"));
		btn.addClickHandler((e) -> {
			reportPanel.clear();
			reportPanel.add(panel);
		});
		/*
		 * go ahead and show notices or production results by default
		 */
		// special notices
		if (btn.getText().toLowerCase().startsWith("notice")) {
			reportPanel.clear();
			reportPanel.add(panel);
		}
		// special notices
		if (btn.getText().toLowerCase().startsWith("production")) {
			reportPanel.clear();
			reportPanel.add(panel);
		}

	}

	@EventHandler
	protected void showSpeciesStatus(Event.ShowSpeciesStatus event) {
		if (isBlank(event.getReport())) {
			return;
		}
		gameStats = event.getGameStats();
		String report = event.getReport();
		report = report.replace("\t", "     ");
		report = StringUtils.substringAfter(report, "SPECIES STATUS").trim();
		if (isBlank(report)) {
			return;
		}
		report = StringUtils.substringBeforeLast(report, "ORDER SECTION.");
		if (isBlank(report)) {
			return;
		}
		report = report.trim();

		display.clear();
		MaterialPanel reportPanel = new MaterialPanel();
		/*
		 * extract sections and add section display buttons with appropriate panel
		 * views.
		 */
		MaterialPanel showPartBtns = new MaterialPanel();
		showPartBtns.setTextAlign(TextAlign.CENTER);
		showPartBtns.add(new Hr());
		String tmp = report;
		List<MaterialButton> planetBtns = new ArrayList<>();
		while (!tmp.isEmpty()) {
			final String part = StringUtils.substringBefore(tmp, ExtractDetailedGameInfo.DIVIDER).trim();
			tmp = StringUtils.substringAfter(tmp, ExtractDetailedGameInfo.DIVIDER);
			if (part.isEmpty()) {
				continue;
			}
			MaterialButton btn = new MaterialButton();
			showPartBtns.add(btn);
			btn.setMargin(2);
			btn.getElement().getStyle().setBackgroundColor("DarkGreen");
			String btnText = StringUtils.substringBefore(part, "\n").trim();
			if (btnText.startsWith("HOME PLANET: PL ")) {
				btnText = StringUtils.substringAfter(btnText, "PLANET: PL ");
				showPartBtns.remove(btn);
				planetBtns.add(btn);
			}
			if (btnText.contains(" PLANET: PL ")) {
				btnText = StringUtils.substringAfter(btnText, "PLANET: PL ");
				btnText = "PL " + btnText;
				showPartBtns.remove(btn);
				planetBtns.add(btn);
			}
			if (btnText.contains(" COLONY: PL ")) {
				btnText = StringUtils.substringAfter(btnText, "COLONY: PL ");
				btnText = "PL " + btnText;
				showPartBtns.remove(btn);
				planetBtns.add(btn);
			}
			if (btnText.toLowerCase().startsWith("aliens at")) {
				btnText = "Aliens Detected";
				btn.getElement().getStyle().setBackgroundColor("DarkRed");
			}
			if (btnText.toLowerCase().startsWith("other planets")) {
				showPartBtns.add(btn);// move button after Br by deattach/reattach
				btn.getElement().getStyle().setBackgroundColor("DarkBlue");
			}
			if (btnText.toLowerCase().startsWith("start of turn")) {
				btnText = StringUtils.substringBetween(part, "Species name:", "\n").trim();
				btnText += " STATS";
				btn.getElement().getStyle().setBackgroundColor("DarkBlue");
			}
			btnText = StringUtils.strip(btnText, ":");
			btn.setText(btnText);
			String tmpPart = part;
			tmpPart = tmpPart.replace("\t", "     ");
			tmpPart = tmpPart.replace("  ", "&nbsp; ");
			tmpPart = tmpPart.replace("  ", "&nbsp; ");
			tmpPart = tmpPart.replace("\n", "<br/>");
			tmpPart = highlightErrors(tmpPart);
			tmpPart = highlightNumbers(tmpPart);
			MaterialPanel panel = new MaterialPanel();
			panel.add(new HTML("<div style='font-size: 175%; font-family: monospace;'>" + tmpPart + "</div>"));
			btn.addClickHandler((e) -> {
				reportPanel.clear();
				reportPanel.add(panel);
			});
			/*
			 * go ahead and show STATS by default
			 */
			if (btn.getText().toUpperCase().endsWith(" STATS")) {
				reportPanel.clear();
				reportPanel.add(panel);
			}
		}
		if (gameStats.getShipLocations() != null && !gameStats.getShipLocations().isEmpty()) {
			MaterialButton shipsBtn = new MaterialButton("Ships and Bases");
			shipsBtn.addClickHandler((e) -> {
				fireEvent(
						new Event.ShowShipsAndBases(gameStats.getShipLocations(), ShowShipsAndBases.SortOrder.ByName));
			});
			shipsBtn.setMargin(2);
			shipsBtn.getElement().getStyle().setBackgroundColor("DarkBlue");
			showPartBtns.add(shipsBtn);
		}
		if (!planetBtns.isEmpty()) {
			showPartBtns.add(new Br());
		}
		for (MaterialButton planetBtn : planetBtns) {
			showPartBtns.add(planetBtn);
		}
		showPartBtns.add(new Hr());
		display.add(showPartBtns);
		display.add(reportPanel);

	}

	private String highlightNumbers(String report) {
		return report.replaceAll("(\\W)(\\d+%?)", "$1<span style='color: blue'>$2</span>");
	}

	private String highlightErrors(String report) {
		return report.replaceAll("(\\s*!!![^<]*)", "<span style='color:red;font-weight:bold;'>$1</span>");
	}
}
