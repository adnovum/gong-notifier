package ch.adnovum.gong.notifier;

import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.RoutingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoutingServiceTest {

	private static final Set<HistoricalEvent> DEFAULT_EVENTS = set(HistoricalEvent.BROKEN, HistoricalEvent.FIXED,
			HistoricalEvent.FAILED);

	@Mock
	private ConfigService configService;

	private RoutingService router;
	private RoutingService.RouteConfigParams routeConfigParams;
	private StageStateChange dummyChange =  new StageStateChange("pipeline1",10,
			"dummyStage","failed");

	@Before
	public void setup() {
		router = new RoutingService(configService);
		routeConfigParams = new RoutingService.RouteConfigParams("GONG_TEST", "_TARGET",
				"_EVENTS", DEFAULT_EVENTS);
	}

	@Test
	public void shouldRouteToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com");

		Collection<String> targets = router.computeTargets(HistoricalEvent.BROKEN, dummyChange, routeConfigParams);

		assertEquals(targets, set("frank@example.com"));
	}

	@Test
	public void shouldRouteToMultipleTargetsInSameVar() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com, bert@example.com");

		Collection<String> targets = router.computeTargets(HistoricalEvent.BROKEN, dummyChange, routeConfigParams);

		assertEquals(targets, set("frank@example.com", "bert@example.com"));
	}

	@Test
	public void shouldRouteAllDefaultEventsToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com");

		// TODO: should be parametrized test
		Map<HistoricalEvent, Collection<String>> allExpected = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set("frank@example.com"),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set("frank@example.com"),
				HistoricalEvent.BROKEN, set("frank@example.com")
		);

		testForAllEvents(allExpected, dummyChange, routeConfigParams);
	}

	@Test
	public void shouldRouteSpecificEventsToSingleTarget() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_EVENTS", "fixed, broken");

		Map<HistoricalEvent, Collection<String>> allExpected = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set(),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set("frank@example.com"),
				HistoricalEvent.BROKEN, set("frank@example.com")
		);

		testForAllEvents(allExpected, dummyChange, routeConfigParams);
	}

	@Test
	public void shouldRouteSpecificEventsToMultipleTargets() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_EVENTS", "fixed, broken",
				"GONG_TEST_2_TARGET", "zonk@example.com",
				"GONG_TEST_2_EVENTS", "failed",
				"GONG_TEST_bla_TARGET", "rop@example.com",
				"GONG_TEST_bla_EVENTS", "building, failed");

		Map<HistoricalEvent, Collection<String>> allExpected = expectedTargetsMap(
				HistoricalEvent.BUILDING, set("rop@example.com"),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set("zonk@example.com", "rop@example.com"),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set("frank@example.com"),
				HistoricalEvent.BROKEN, set("frank@example.com")
		);

		testForAllEvents(allExpected, dummyChange, routeConfigParams);
	}

	@Test
	public void shouldRouteToNoTargetsWithoutMatching() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_EVENTS", "broken");

		Collection<String> targets = router.computeTargets(HistoricalEvent.PASSED, dummyChange, routeConfigParams);

		assertEquals(targets, set());
	}

	@Test
	public void shouldRouteToNoTargetsWithoutVars() throws Exception {
		addMockEnvVariables("pipeline1",
				"SOME_OTHER_VAR", "1234");

		Collection<String> targets = router.computeTargets(HistoricalEvent.PASSED, dummyChange, routeConfigParams);

		assertEquals(targets, set());
	}

	@Test
	public void shouldRouteForSpecificStagesAndEvents() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_EVENTS", "stage1.fixed, stage2.broken");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		StageStateChange change2 = new StageStateChange("pipeline1",
				10,
				"stage2",
				"dummy");

		StageStateChange changeOther = new StageStateChange("pipeline1",
				10,
				"otherStage",
				"dummy");

		Map<HistoricalEvent, Collection<String>> allExpected1 = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set(),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set("frank@example.com"),
				HistoricalEvent.BROKEN, set()
		);

		Map<HistoricalEvent, Collection<String>> allExpected2 = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set(),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set(),
				HistoricalEvent.BROKEN, set("frank@example.com")
		);

		Map<HistoricalEvent, Collection<String>> allExpectedOther = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set(),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set(),
				HistoricalEvent.BROKEN, set()
		);

		testForAllEvents(allExpected1, change1, routeConfigParams);
		testForAllEvents(allExpected2, change2, routeConfigParams);
		testForAllEvents(allExpectedOther, changeOther, routeConfigParams);
	}

	@Test
	public void shouldRouteForSpecificStagesAndAllDefaultEvents() throws Exception {
		addMockEnvVariables("pipeline1",
				"GONG_TEST_TARGET", "frank@example.com",
				"GONG_TEST_EVENTS", "stage2.broken, stage1.all");

		StageStateChange change1 = new StageStateChange("pipeline1",
				10,
				"stage1",
				"dummy");

		Map<HistoricalEvent, Collection<String>> allExpected = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set("frank@example.com"),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set("frank@example.com"),
				HistoricalEvent.BROKEN, set("frank@example.com")
		);

		testForAllEvents(allExpected, change1, routeConfigParams);
	}

	@Test
	public void shouldHandleInvalidRoutingConfig() throws Exception {
		// No actual targets definition
		addMockEnvVariables("pipeline1",
				"GONG_TEST_EVENTS", "broken");

		Map<HistoricalEvent, Collection<String>> allExpected = expectedTargetsMap(
				HistoricalEvent.BUILDING, set(),
				HistoricalEvent.PASSED, set(),
				HistoricalEvent.FAILED, set(),
				HistoricalEvent.CANCELLED, set(),
				HistoricalEvent.FIXED, set(),
				HistoricalEvent.BROKEN, set()
		);

		testForAllEvents(allExpected, dummyChange, routeConfigParams);
	}

	private void addMockEnvVariables(String pipelineName, String... settings) {
		PipelineConfig cfg = new PipelineConfig();
		cfg.name = pipelineName;
		for (int i = 0; i < settings.length; i += 2) {
			cfg.addEnvironmentVariable(settings[i], settings[i + 1]);
		}

		when(configService.fetchPipelineConfig(eq(pipelineName), anyInt())).thenReturn(Optional.of(cfg));
	}

	@SuppressWarnings("unchecked")
	private static Map<HistoricalEvent, Collection<String>> expectedTargetsMap(Object... eventToTargets) {
		Map<HistoricalEvent, Collection<String>> expected = new HashMap<>();
		for (int i = 0; i < eventToTargets.length; i += 2) {
			HistoricalEvent event = (HistoricalEvent) eventToTargets[i];
			Collection<String> targets = (Collection<String>) eventToTargets[i + 1];
			expected.put(event, targets);
		}
		return expected;
	}

	private void testForAllEvents(Map<HistoricalEvent, Collection<String>> allExpected,
									StageStateChange stateChange, RoutingService.RouteConfigParams routeConfigParams) {
		Arrays.stream(HistoricalEvent.values()).forEach(e -> {
			Collection<String> targets = router.computeTargets(e, stateChange, routeConfigParams);
			Collection<String> expected = allExpected.get(e);
			assertEquals("For event " + e + ", stage " + stateChange.getStageName(), expected, targets);
		});
	}

	@SafeVarargs
	private static <T> Set<T> set(T... ts) {
		return new HashSet<>(Arrays.asList(ts));
	}
}
