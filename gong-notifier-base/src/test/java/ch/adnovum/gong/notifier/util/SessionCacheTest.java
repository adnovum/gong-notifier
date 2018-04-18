package ch.adnovum.gong.notifier.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class SessionCacheTest {

	private Map<String, String> loaderResults;
	private SessionCache<String, String, String> cache;
	private int loadCount;

	@Before
	public void setup() {
		loaderResults = new HashMap<>();
		loaderResults.put("fruit", "banana");
		cache = new SessionCache<>(100, TimeUnit.MILLISECONDS, 1000, this::apiCall);
		loadCount = 0;
	}


	@Test
	public void shouldLoadUncachedValue() {
		String val = cache.fetch("fruit", "session1").get();
		assertEquals("banana", val);
		assertEquals(1, loadCount);
	}

	@Test
	public void shouldRetrieveCachedValue() {
		cache.fetch("fruit", "session1");
		loaderResults.put("fruit", "apple");
		String val = cache.fetch("fruit", "session1").get();
		assertEquals("banana", val);
		assertEquals(1, loadCount);
	}

	@Test
	public void shouldReloadValueForDifferentSession() {
		cache.fetch("fruit", "session1");
		loaderResults.put("fruit", "apple");

		String val = cache.fetch("fruit", "session2").get();
		assertEquals("apple", val);
		assertEquals(2, loadCount);
	}

	@Test
	public void shouldInvalidateCacheAfterTTL() throws InterruptedException {
		cache.fetch("fruit", "session1");
		loaderResults.put("fruit", "apple");
		Thread.sleep(150L); // NOSONAR

		String val = cache.fetch("fruit", "session1").get();

		assertEquals("apple", val);
		assertEquals(2, loadCount);
	}

	@Test
	public void shouldHandleLoaderMiss() throws InterruptedException {

		Optional<String> valMaybe = cache.fetch("not_existing", "session1");
		assertEquals(false, valMaybe.isPresent());

		// This miss should not have been cached, so calling it again should result in another loader call.
		cache.fetch("not_existing", "session1");

		assertEquals(2, loadCount);
	}

	private Optional<String> apiCall(String identifier, String sessionKey) {
		loadCount++;
		return Optional.ofNullable(loaderResults.get(identifier));
	}
}
