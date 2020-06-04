package ch.adnovum.gong.notifier.services;

import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class RoutingService {

	private static Logger LOGGER = Logger.getLoggerFor(RoutingService.class);

	private final ConfigService configService;

	public static class RouteConfigParams {
		private final String envVariableBase;
		private final String targetEnvVariableSuffix;
		private final String eventsEnvVariableSuffix;
		private final Set<HistoricalEvent> defaultEvents;

		public RouteConfigParams(String envVariableBase, String targetEnvVariableSuffix, String eventsEnvVariableSuffix,
								 Set<HistoricalEvent> defaultEvents) {
			this.envVariableBase = envVariableBase;
			this.targetEnvVariableSuffix = targetEnvVariableSuffix;
			this.eventsEnvVariableSuffix = eventsEnvVariableSuffix;
			this.defaultEvents = defaultEvents;
		}
	}

	public RoutingService(ConfigService configService) {
		this.configService = configService;
	}

	public Collection<String> computeTargets(HistoricalEvent event, StageStateChange stateChange, RouteConfigParams params) {
		Collection<String> targets = lookupTargets(stateChange, event, params);
		if (targets.isEmpty()) {
			LOGGER.debug("No targets found for " + stateChange.getPipelineName() + " with event " + event.getValue());
		}

		return targets;
	}

	private Collection<String> lookupTargets(StageStateChange stateChange, HistoricalEvent event, RouteConfigParams params) {
		return 	configService.fetchPipelineConfig(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.map(cfg -> readTargetsFromPipelineConfig(cfg, params))
				.map(rules -> computeApplicableTargets(rules, stateChange, event))
				.orElse(Collections.emptySet());
	}

	private Set<String> computeApplicableTargets(Collection<TargetConfig> rules, StageStateChange stateChange,
												 HistoricalEvent event) {
		return rules.stream()
				.filter(c -> c.applies(stateChange.getStageName(), event))
				.flatMap(c -> c.targets.stream())
				.collect(Collectors.toSet());
	}

	private Collection<TargetConfig> readTargetsFromPipelineConfig(PipelineConfig cfg, RouteConfigParams params) {
		Map<String, TargetConfig> result = new HashMap<>();
		for (PipelineConfig.EnvironmentVariable v: cfg.environmentVariables) {
			if (v.name.startsWith(params.envVariableBase)) {
				if (v.name.endsWith(params.eventsEnvVariableSuffix)) {
					String cfgName = v.name.substring(0, v.name.length() - params.eventsEnvVariableSuffix.length());
					TargetConfig tgtCfg = result.computeIfAbsent(cfgName, k -> new TargetConfig(params.defaultEvents));
					tgtCfg.routingRules = parseRoutingRules(v.value, params.defaultEvents);
				}
				else if (v.name.endsWith(params.targetEnvVariableSuffix)) {
					String cfgName = v.name.substring(0, v.name.length() - params.targetEnvVariableSuffix.length());
					TargetConfig tgtCfg = result.computeIfAbsent(cfgName, k -> new TargetConfig(params.defaultEvents));
					tgtCfg.targets = new HashSet<>(Arrays.asList(v.value.split("\\s*,\\s*")));
				}
			}
		}
		// Validate the config
		for (Iterator<Map.Entry<String, TargetConfig>> it = result.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, TargetConfig> e = it.next();
			String cfgName = e.getKey();
			TargetConfig tgtCfg = e.getValue();
			if (tgtCfg.targets == null) {
				LOGGER.warn("Notification configuration " + cfgName + " is missing a list of targets. Pipeline: " + cfg.name
					+ ". There should be an environment variable " + cfgName + params.targetEnvVariableSuffix);
				it.remove();
			}
		}
		return result.values();
	}


	private List<RoutingRule> parseRoutingRules(String ruleStr, Set<HistoricalEvent> defaultEvents) {
		List<RoutingRule> result = new LinkedList<>();
		String[] rules = ruleStr.toLowerCase().split("\\s*,\\s*");
		for (String rule: rules) {
			String stage = "all";
			String event;
			String[] parts = rule.split("\\.");
			if (parts.length > 1) {
				stage = parts[0];
				event = parts[1];
			}
			else {
				event = parts[0];
			}

			result.add(new RoutingRule(event, stage, defaultEvents));
		}
		return result;
	}

	private class TargetConfig {
		Set<String> targets;
		List<RoutingRule> routingRules;
		final Set<HistoricalEvent> defaultEvents;

		public TargetConfig(Set<HistoricalEvent> defaultEvents) {
			this.defaultEvents = defaultEvents;
		}

		boolean applies(String stage, HistoricalEvent event) {
			if (routingRules == null || routingRules.isEmpty()) {
				return matchesDefaultEvent(event, defaultEvents);
			}

			for (RoutingRule rule: routingRules) {
				if (rule.applies(stage, event)) {
					return true;
				}
			}
			return false;
		}
	}

	private class RoutingRule {
		final String requiredEvent;
		final String requiredStage;
		final Set<HistoricalEvent> defaultEvents;

		RoutingRule(String requiredEvent, String requiredStage, Set<HistoricalEvent> defaultEvents) {
			this.requiredEvent = requiredEvent;
			this.requiredStage = requiredStage;
			this.defaultEvents = defaultEvents;
		}

		boolean applies(String stage, HistoricalEvent event) {
			return appliesToEvent(event) && appliesToStage(stage);
		}

		boolean appliesToEvent(HistoricalEvent event) {
			return ("all".equals(requiredEvent) && matchesDefaultEvent(event, defaultEvents)) || event.getValue().equals(requiredEvent);
		}

		boolean appliesToStage(String stage) {
			return "all".equals(requiredStage) || stage.equals(requiredStage);
		}
	}

	private boolean matchesDefaultEvent(HistoricalEvent event, Set<HistoricalEvent> defaultEvents) {
		return event != null && (defaultEvents == null || defaultEvents.isEmpty() || defaultEvents.contains(event));
	}
}
