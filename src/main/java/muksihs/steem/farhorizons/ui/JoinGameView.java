package muksihs.steem.farhorizons.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialIntegerBox;
import gwt.material.design.client.ui.MaterialTextBox;
import muksihs.steem.farhorizons.client.Event;
import muksihs.steem.farhorizons.shared.JoinGameInfo;

public class JoinGameView extends EventBusComposite {

	private static JoinGameViewUiBinder uiBinder = GWT.create(JoinGameViewUiBinder.class);

	interface JoinGameViewUiBinder extends UiBinder<Widget, JoinGameView> {
	}

	public JoinGameView() {
		initWidget(uiBinder.createAndBindUi(this));
		btnJoin.addClickHandler(this::joinGame);
	}
	
	private void joinGame(ClickEvent event) {
		JoinGameInfo info = new JoinGameInfo(
		speciesName.getValue().trim(),
		homeName.getValue().trim(),
		govName.getValue().trim(),
		govType.getValue().trim(),
		ml.getValue(),
		gv.getValue(),
		ls.getValue(),
		bi.getValue());
		fireEvent(new Event.ValidateThenSubmitJoinGame(info));
	}

	@UiField
	protected MaterialTextBox speciesName;
	@UiField
	protected MaterialTextBox homeName;
	@UiField
	protected MaterialTextBox govName;
	@UiField
	protected MaterialTextBox govType;
	@UiField
	protected MaterialIntegerBox ml;
	@UiField
	protected MaterialIntegerBox gv;
	@UiField
	protected MaterialIntegerBox ls;
	@UiField
	protected MaterialIntegerBox bi;
	@UiField
	protected MaterialButton btnJoin;

	@EventHandler
	protected void enable(Event.EnableOrderForm event) {
		GWT.log("Event.TurnResultsMenuEnable: "+event.isEnable());
		speciesName.setEnabled(event.isEnable());
		homeName.setEnabled(event.isEnable());
		govName.setEnabled(event.isEnable());
		govType.setEnabled(event.isEnable());
		ml.setEnabled(event.isEnable());
		gv.setEnabled(event.isEnable());
		ls.setEnabled(event.isEnable());
		bi.setEnabled(event.isEnable());
		btnJoin.setEnabled(event.isEnable());
	}

	interface MyEventBinder extends EventBinder<JoinGameView> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
