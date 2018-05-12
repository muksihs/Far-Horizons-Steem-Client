package muksihs.steem.farhorizons.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

public class GameState implements Map<String, String> {
	private final Map<String, String> store;

	public GameState() {
		Storage localStorageIfSupported = Storage.getLocalStorageIfSupported();
		if (localStorageIfSupported != null) {
			store = new StorageMap(localStorageIfSupported);
		} else {
			store = new HashMap<>();
		}
	}

	@Override
	public void clear() {
		store.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return store.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return store.containsValue(value);
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return store.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		return store.equals(o);
	}

	@Override
	public String get(Object key) {
		return store.get(key);
	}

	@Override
	public int hashCode() {
		return store.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return store.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return store.keySet();
	}

	@Override
	public String put(String key, String value) {
		return store.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		store.putAll(m);
	}

	@Override
	public String remove(Object key) {
		return store.remove(key);
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public String toString() {
		return store.toString();
	}

	@Override
	public Collection<String> values() {
		return store.values();
	}
}
