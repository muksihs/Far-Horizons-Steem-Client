package muksihs.steem.farhorizons.client.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

import muksihs.steem.farhorizons.client.GameStats;

public class GameState {
	private  Map<String, String> storage;
	private final String prefix;
	private final String userPrefix;
	public GameState(GameStats gameStats) {
		Storage localStorageIfSupported = Storage.getLocalStorageIfSupported();
		if (localStorageIfSupported == null) {
			storage = new HashMap<>();
		} else {
			storage = new StorageMap(localStorageIfSupported);
		}
		userPrefix = gameStats.getPlayer()+"-named-planet-cache-";
		prefix = userPrefix+gameStats.getGamePermLink();
		//always purge any previously cached orders from other turns or games
		Iterator<String> iKey = storage.keySet().iterator();
		while (iKey.hasNext()) {
			String key=iKey.next();
			if (!key.startsWith(userPrefix)) {
				continue;
			}
			if (key.startsWith(prefix)) {
				continue;
			}
			iKey.remove();
		}
	}
	public void put(String key, String value) {
		storage.put(prefix+key, value);
	}
	public String get(String key) {
		String value = storage.containsKey(prefix+key)?storage.get(prefix+key):"";
		return value;
	}
	public boolean containsKey(String key) {
		return storage.containsKey(prefix+key);
	}
	public void clear() {
		Iterator<String> iKey = storage.keySet().iterator();
		while (iKey.hasNext()) {
			String key=iKey.next();
			if (!key.startsWith(userPrefix)) {
				continue;
			}
			iKey.remove();
		}		
	}
}
