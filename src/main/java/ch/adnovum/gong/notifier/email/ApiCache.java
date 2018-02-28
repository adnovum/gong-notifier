package ch.adnovum.gong.notifier.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ApiCache<S, T> {

	private Map<S, CacheEntry> cache = new HashMap<>();
	private long ttl;
	private Function<S, Optional<T>> fetcher;

	public ApiCache(long ttl, Function<S, Optional<T>> fetcher) {
		this.ttl = ttl;
		this.fetcher = fetcher;
	}

	public Optional<T> fetch(S identifier) {
		CacheEntry cached = cache.get(identifier);
		if (cached != null && System.currentTimeMillis() - cached.timestamp < ttl) {
			return Optional.of(cached.t);
		}

		return fetcher.apply(identifier).map(t -> {
			cache.put(identifier, new CacheEntry(System.currentTimeMillis(), t));
			return t;
		});
	}

	private class CacheEntry {
		long timestamp;
		T t;

		CacheEntry(long timestamp, T t) {
			this.timestamp = timestamp;
			this.t = t;
		}
	}
}
