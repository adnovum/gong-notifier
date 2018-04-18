package ch.adnovum.gong.notifier.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A time and size-limited cache that additionally only stores a value for a specific "session".
 * If the same key is queried in a different session, the value will be reloaded from the loader,
 * no matter whether an old session value is still cached.
 *
 * Note that it does not support multiple parallel sessions: once a new session causes a reload of the value,
 * any previous session will also cause a reload since it is no longer in the cache.
 *
 * @param <K> The type of the cache key / identifier
 * @param <V> The type of the value stored in the cache
 * @param <S> The type of the session keys
 */
public class SessionCache<K, V, S> {

	private Cache<K, SessionedEntry> cache;
	private BiFunction<K, S, Optional<V>> loader;

	public SessionCache(long duration, TimeUnit timeUnit, int maxSize, BiFunction<K, S, Optional<V>> loader) {
		this.loader = loader;
		cache = CacheBuilder.newBuilder()
				.expireAfterAccess(duration, timeUnit)
				.maximumSize(maxSize)
				.build();
	}

	public Optional<V> fetch(K identifier, S sessionKey) {
		SessionedEntry entry = cache.getIfPresent(identifier);
		if (entry == null || !entry.sessionKey.equals(sessionKey)) {
			Optional<V> valMaybe = loader.apply(identifier, sessionKey);
			valMaybe.ifPresent(v -> cache.put(identifier, new SessionedEntry(sessionKey, v)));
			return valMaybe;
		}
		else {
			return Optional.of(entry.value);
		}
	}

	private class SessionedEntry {
		S sessionKey;
		V value;

		SessionedEntry(S sessionKey, V value) {
			this.sessionKey = sessionKey;
			this.value = value;
		}
	}
}
