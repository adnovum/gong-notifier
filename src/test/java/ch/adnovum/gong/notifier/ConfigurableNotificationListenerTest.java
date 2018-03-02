package ch.adnovum.gong.notifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import ch.adnovum.gong.notifier.email.PipelineInfoProvider;
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
				"_STATES");
	}

	@Test
	public void shouldSendToSingle() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com");

		listener.handlePassed(new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed"));

		listener.assertTargets("passed", "frank@example.com");

	}

	@Test
	public void shouldSendToSingleWithMatches() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_STATES", "fixed, broken");

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
	public void shouldSendToMultipleWithMatches() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_STATES", "fixed, broken",
				"GONG_TEST_TARGET_2", "zonk@example.com",
				"GONG_TEST_TARGET_2_STATES", "failed",
				"GONG_TEST_TARGET_bla", "rop@example.com",
				"GONG_TEST_TARGET_bla_STATES", "building, failed");

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
	public void shouldSendToNoneMatches() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_TARGET_STATES", "broken");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		listener.handlePassed(change);

		assertEquals(0, listener.targets.size());
	}

	private void addMockEnvVariables(String pipelineName, String... settings) {
		PipelineConfig cfg = new PipelineConfig();
		for (int i = 0; i < settings.length; i += 2) {
			cfg.addEnvironmentVariable(settings[i], settings[i + 1]);
		}

		when(infoProvider.getPipelineConfig(pipelineName)).thenReturn(Optional.of(cfg));
	}

	private class TestConfigurableNotificationListener extends ConfigurableNotificationListener {

		private class Target {
			String target;
			String state;

			Target(String target, String state) {
				this.target = target;
				this.state = state;
			}
		}

		private List<Target> targets = new LinkedList<>();

		public TestConfigurableNotificationListener(PipelineInfoProvider pipelineInfo, String targetEnvVariablePrefix,
				String targetStatesEnvVariableSuffix) {
			super(pipelineInfo, targetEnvVariablePrefix, targetStatesEnvVariableSuffix);
		}

		@Override
		protected void notifyTargets(StageStateChange stateChange, String state, List<String> targets) {
			targets.forEach(t -> this.targets.add(new Target(t, state)));
		}

		private void assertTargets(String expectedState, String... expectedTargets) {
			Set<String> left = new HashSet<>(Arrays.asList(expectedTargets));
			for (Target t: targets) {
				if (t.state.equals(expectedState)) {
					left.remove(t.target);
				}
			}
			assertTrue("Expected following targets for state " + expectedState + ": " + String.join(", ", expectedTargets)
					+", but missing these: " + left, left.isEmpty());
		}
	}
}
