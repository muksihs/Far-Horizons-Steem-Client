package muksihs.steem.farhorizons.client.cache;

import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

public class CleanOutBadCachedData implements ScheduledCommand {

	public CleanOutBadCachedData() {
	}
	
	@Override
	public void execute() {
		Storage localStorageIfSupported = Storage.getLocalStorageIfSupported();
		if (localStorageIfSupported==null) {
			return;
		}
		Map<String, String> storage = new StorageMap(localStorageIfSupported);
		//purge data from previous "planet name" tracking cache data method
		cleanUp(storage, "game-", "planet:");
		cleanUp(storage, "planet:", "planet:");
	}

	private void cleanUp(Map<String, String> storage, String prefix, String contains) {
		Iterator<String> iKey = storage.keySet().iterator();
		while (iKey.hasNext()) {
			String key=iKey.next();
			if (!key.startsWith(prefix)) {
				continue;
			}
			if (!key.contains(contains)) {
				continue;
			}
			iKey.remove();
		}
	}

}
