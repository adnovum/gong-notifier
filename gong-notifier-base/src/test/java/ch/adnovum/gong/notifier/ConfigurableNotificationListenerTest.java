package ch.adnovum.gong.notifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableNotificationListenerTest {

	private static final List<BiConsumer<ConfigurableNotificationListener, StageStateChange>> ALL_HANDLERS = Arrays.asList(
			ConfigurableNotificationListener::handleBroken,
			ConfigurableNotificationListener::handlePassed,
			ConfigurableNotificationListener::handleBuilding,
			ConfigurableNotificationListener::handleFailed,
			ConfigurableNotificationListener::handleFixed,
			ConfigurableNotificationListener::handleCancelled
	);

	@Mock
	private PipelineInfoProvider infoProvider;

	private TestConfigurableNotificationListener listener;

	@Before
	public void setup() {
		listener = new TestConfigurableNotificationListener(infoProvider,
				"GONG_TEST_TARGET",
				"_EVENTS");
	}

	@Test
	public void shouldRouteToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com");

		listener.handlePassed(new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed"));

		listener.assertTargets("passed", "frank@example.com");
	}

	@Test
	public void shouldRouteToMultipleTargetsInSameVar() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com, bert@example.com");

		listener.handlePassed(new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed"));

		listener.assertTargets("passed", "frank@example.com", "bert@example.com");
	}

	@Test
	public void shouldRouteAllEventsToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");
		ALL_HANDLERS.forEach(h -> h.accept(listener, change));

		assertEquals(6, listener.targets.size());
		listener.assertTargets("broken", "frank@example.com");
		listener.assertTargets("passed", "frank@example.com");
		listener.assertTargets("fixed", "frank@example.com");
		listener.assertTargets("failed", "frank@example.com");
		listener.assertTargets("cancelled", "frank@example.com");
		listener.assertTargets("building", "frank@example.com");
	}

	@Test
	public void shouldRouteSpecificEventsToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "fixed, broken");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");
		ALL_HANDLERS.forEach(h -> h.accept(listener, change));

		assertEquals(2, listener.targets.size());
		listener.assertTargets("fixed", "frank@example.com");
		listener.assertTargets("broken", "frank@example.com");
	}

	@Test
	public void shouldRouteSpecificEventsToMultipleTargets() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "fixed, broken",
				"GONG_TEST_TARGET_2", "zonk@example.com",
				"GONG_TEST_TARGET_2_EVENTS", "failed",
				"GONG_TEST_TARGET_bla", "rop@example.com",
				"GONG_TEST_TARGET_bla_EVENTS", "building, failed");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		ALL_HANDLERS.forEach(h -> h.accept(listener, change));

		assertEquals(5, listener.targets.size());
		listener.assertTargets("fixed", "frank@example.com");
		listener.assertTargets("broken", "frank@example.com");
		listener.assertTargets("failed", "zonk@example.com", "rop@example.com");
		listener.assertTargets("building", "rop@example.com");
	}

	@Test
	public void shouldRouteToNoTargetsWithoutMatching() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "broken");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		listener.handlePassed(change);

		assertEquals(0, listener.targets.size());
	}

	@Test
	public void shouldRouteToNoTargetsWithoutVars() throws Exception {
		addMockEnvVariables("pipeline1",
				"SOME_OTHER_VAR", "1234");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		listener.handlePassed(change);

		assertEquals(0, listener.targets.size());
	}

	@Test
	public void shouldRouteForSpecificStagesAndEvents() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "stage1.fixed, stage2.broken");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		StageStateChange change2 = new StageStateChange("pipeline1",
				10,
				"stage2",
				"dummy");

		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));
		ALL_HANDLERS.forEach(h -> h.accept(listener, change2));


		assertEquals(2, listener.targets.size());
		listener.assertTargetsForStage("stage1","fixed", "frank@example.com");
		listener.assertTargetsForStage("stage2","broken", "frank@example.com");
	}

	@Test
	public void shouldRouteForSpecificStagesAndAllEvents() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "stage2.broken, stage1.all");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));

		assertEquals(ALL_HANDLERS.size(), listener.targets.size());
	}

	@Test
	public void shouldHandleInvalidRoutingConfig() throws Exception {
		// No actual targets definition
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET_EVENTS", "broken");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));

		assertEquals(0, listener.targets.size());
	}

	@Test
	public void shouldUpdateConfigForDifferentRun() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "broken");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		// First run
		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));
		listener.assertTargetsForStage("stage1","broken", "frank@example.com");

		// Now change the config
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "fixed");

		// And pretend to be in the same run -> should still be using the old config
		listener.clear();
		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));
		listener.assertTargetsForStage("stage1","broken", "frank@example.com");

		// And now in a new run -> should use the new config
		StageStateChange change2 = new StageStateChange("pipeline1",
				11,
				"stage1",
				"dummy");
		listener.clear();
		ALL_HANDLERS.forEach(h -> h.accept(listener, change2));
		listener.assertTargetsForStage("stage1","fixed", "frank@example.com");
	}

	@Test
	public void shouldUpdateConfigAfterCacheExpires() throws Exception {
		listener.setConfigCacheTTL(100L, TimeUnit.MILLISECONDS);

		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "broken");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		// First run
		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));
		listener.assertTargetsForStage("stage1","broken", "frank@example.com");

		// Now change the config
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_EVENTS", "fixed");

		// And wait for the cache to expire
		Thread.sleep(300L);

		// And pretend to be in the same run -> config should be updated!
		listener.clear();
		ALL_HANDLERS.forEach(h -> h.accept(listener, change1));
		listener.assertTargetsForStage("stage1","fixed", "frank@example.com");
	}

	private void addMockEnvVariables(String pipelineName, String... settings) {
		PipelineConfig cfg = new PipelineConfig();
		cfg.name = pipelineName;
		for (int i = 0; i < settings.length; i += 2) {
			cfg.addEnvironmentVariable(settings[i], settings[i + 1]);
		}

		when(infoProvider.getPipelineConfig(eq(pipelineName), anyInt())).thenReturn(Optional.of(cfg));
	}

	private class TestConfigurableNotificationListener extends ConfigurableNotificationListener {

		private class Target {
			String target;
			String event;
			String stage;

			Target(String target, String event, String stage) {
				this.target = target;
				this.event = event;
				this.stage = stage;
			}
		}

		private List<Target> targets = new LinkedList<>();

		public TestConfigurableNotificationListener(PipelineInfoProvider pipelineInfo, String targetEnvVariablePrefix,
				String targetStatesEnvVariableSuffix) {
			super(pipelineInfo, targetEnvVariablePrefix, targetStatesEnvVariableSuffix);
		}

		@Override
		protected void notifyTargets(StageStateChange stateChange, Event event, Collection<String> targets) {
			targets.forEach(t -> this.targets.add(new Target(t, event.getValue(), stateChange.getStageName())));
		}

		private void assertTargets(String expectedState, String... expectedTargets) {
			assertTargetsForStage(null, expectedState, expectedTargets);
		}

		private void assertTargetsForStage(String expectedStage, String expectedState, String... expectedTargets) {
			Set<String> left = new HashSet<>(Arrays.asList(expectedTargets));
			for (Target t: targets) {
				if (t.event.equals(expectedState) && (expectedStage == null || t.stage.equals(expectedStage))) {
					left.remove(t.target);
				}
			}
			assertTrue("Expected following targets for event " + expectedState + ": " + String.join(", ", expectedTargets)
					+", but missing these: " + left, left.isEmpty());
		}

		private void clear() {
			targets.clear();
		}
	}
}
