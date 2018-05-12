package muksihs.steem.farhorizons.client.rsc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface OrderFormResources extends ClientBundle {
	
	OrderFormResources INSTANCE = GWT.create(OrderFormResources.class);
	
	@Source("txt/validCommands.txt")
	TextResource validCommands();
	
	@Source("txt/commandHelp.html")
	TextResource commandHelp();
}
