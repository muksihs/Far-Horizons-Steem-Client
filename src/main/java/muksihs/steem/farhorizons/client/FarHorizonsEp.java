package muksihs.steem.farhorizons.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

import muksihs.steem.farhorizons.client.cache.CleanOutBadCachedData;

import com.google.gwt.core.client.Scheduler;

public class FarHorizonsEp implements EntryPoint, IsSdm {

	private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void onUncaughtException(Throwable e) {
			GWT.log(e.getMessage() == null ? "" : e.getMessage(), e);
		}
	};

	@Override
	public void onModuleLoad() {
		GWT.log("onModuleLoad");
		GWT.setUncaughtExceptionHandler(handler);
		Scheduler.get().scheduleDeferred(new FarHorizonsWebApp());
		Scheduler.get().scheduleDeferred(new CleanOutBadCachedData());
	}

}
