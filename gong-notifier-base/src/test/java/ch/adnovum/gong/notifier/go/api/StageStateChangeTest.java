package ch.adnovum.gong.notifier.go.api;

import com.google.gson.Gson;
import org.junit.Test;

import static ch.adnovum.gong.notifier.util.GongUtil.readResourceString;
import static org.junit.Assert.assertEquals;

public class StageStateChangeTest {

	@Test
	public void shouldDeserializeNotification() {
		Gson gson = new Gson();
		String notificationString = readResourceString("/notification.json");

		StageStateChange stateChange = gson.fromJson(notificationString, StageStateChange.class);

		assertEquals("pipeline1", stateChange.pipeline.name);
		assertEquals(Integer.valueOf(6), stateChange.pipeline.counter);
		assertEquals(1, stateChange.pipeline.buildCause.size());
		StageStateChange.BuildCause buildCause = stateChange.pipeline.buildCause.get(0);
		assertEquals("git", buildCause.material.type);
		assertEquals("https://github.com/adnovum/gong-notifier.git", buildCause.material.configuration.url);
	}
}
