package ch.adnovum.gong.notifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.SessionCache;
import com.thoughtworks.go.plugin.api.logging.Logger;

public abstract class ConfigurableNotificationListener implements NotificationListener {

	private static Logger LOGGER = Logger.getLoggerFor(ConfigurableNotificationListener.class);

	protected PipelineInfoProvider pipelineInfo;
	private String targetEnvVariablePrefix;
	private String targetEventsEnvVariableSuffix;
	private SessionCache<String, Collection<TargetConfig>, Integer> routingConfigs;

	public ConfigurableNotificationListener(PipelineInfoProvider pipelineInfo, String targetEnvVariablePrefix,
			String targetEventsEnvVariableSuffix) {
		this.pipelineInfo = pipelineInfo;
		this.targetEnvVariablePrefix = targetEnvVariablePrefix;
		this.targetEventsEnvVariableSuffix = targetEventsEnvVariableSuffix;
		this.routingConfigs = new SessionCache<>(5, TimeUnit.MINUTES, 1000, this::fetchPipelineTargetConfig);
	}

	public void setConfigCacheTTL(long duration, TimeUnit timeUnit) {
		this.routingConfigs = new SessionCache<>(duration, timeUnit, 1000, this::fetchPipelineTargetConfig);
	}

	@Override
	public void handleBuilding(StageStateChange stateChange) {
		handle(stateChange,Event.BUILDING);
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		handle(stateChange,Event.PASSED);
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		handle(stateChange,Event.FAILED);
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		handle(stateChange,Event.BROKEN);
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		handle(stateChange,Event.FIXED);
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		handle(stateChange,Event.CANCELLED);
	}

	private void handle(StageStateChange stateChange, Event event) {
		Collection<String> targets = lookupTargets(stateChange, event.getValue());
		if (targets.isEmpty()) {
			LOGGER.debug("No targets found for " + stateChange.getPipelineName() + " with event " + event.getValue());
			return;
		}

		notifyTargets(stateChange, event, targets);
	}

	protected abstract void notifyTargets(StageStateChange stateChange, Event event, Collection<String> targets);

	private Collection<String> lookupTargets(StageStateChange stateChange, String event) {
		return routingConfigs.fetch(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.map(rules -> computeApplicableTargets(rules, stateChange, event))
				.orElse(Collections.emptySet());
	}

	private Set<String> computeApplicableTargets(Collection<TargetConfig> rules, StageStateChange stateChange, String event) {
		return rules.stream()
				.filter(c -> c.applies(stateChange.getStageName(), event))
				.flatMap(c -> c.targets.stream())
				.collect(Collectors.toSet());
	}

	private Optional<Collection<TargetConfig>> fetchPipelineTargetConfig(String pipelineName, int pipelineCounter) {
		return pipelineInfo.getPipelineConfig(pipelineName, pipelineCounter)
				.map(this::readTargetsFromPipelineConfig);
	}

	private String computeSessionKey(StageStateChange stateChange) {
		return String.valueOf(stateChange.getPipelineCounter());
	}

	private Collection<TargetConfig> readTargetsFromPipelineConfig(PipelineConfig cfg) {
		Map<String, TargetConfig> result = new HashMap<>();
		for (PipelineConfig.EnvironmentVariable v: cfg.environmentVariables) {
			if (v.name.startsWith(targetEnvVariablePrefix)) {
				if (v.name.endsWith(targetEventsEnvVariableSuffix)) {
					String cfgName = v.name.substring(0, v.name.length() - targetEventsEnvVariableSuffix.length());
					TargetConfig tgtCfg = result.computeIfAbsent(cfgName, k -> new TargetConfig());
					tgtCfg.routingRules = parseRoutingRules(v.value);
				}
				else {
					String cfgName = v.name;
					TargetConfig tgtCfg = result.computeIfAbsent(cfgName, k -> new TargetConfig());
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
				LOGGER.warn("Notification configuration " + cfgName + " is missing a list of targets. Pipeline: " + cfg.name);
				it.remove();
			}
		}
		return result.values();
	}


	private static List<RoutingRule> parseRoutingRules(String ruleStr) {
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

			result.add(new RoutingRule(event, stage));
		}
		return result;
	}

	private static class PipelineTargetConfig {
		String sessionKey;
		Collection<TargetConfig> targetConfigs;

		PipelineTargetConfig(String sessionKey,
				Collection<TargetConfig> targetConfigs) {
			this.sessionKey = sessionKey;
			this.targetConfigs = targetConfigs;
		}
	}

	private static class TargetConfig {
		Set<String> targets;
		List<RoutingRule> routingRules;

		boolean applies(String stage, String event) {
			if (routingRules == null || routingRules.isEmpty()) {
				return true;
			}

			for (RoutingRule rule: routingRules) {
				if (rule.applies(stage, event)) {
					return true;
				}
			}
			return false;
		}
	}

	private static class RoutingRule {
		String requiredEvent;
		String requiredStage;

		RoutingRule(String requiredEvent, String requiredStage) {
			this.requiredEvent = requiredEvent;
			this.requiredStage = requiredStage;
		}

		boolean applies(String stage, String event) {
			return ("all".equals(requiredEvent) || event.equals(requiredEvent)) &&
					("all".equals(requiredStage) || stage.equals(requiredStage));
		}
	}

	public enum Event {
		BUILDING("building", "is building"),
		PASSED("passed", "passed"),
		FAILED("failed", "failed"),
		FIXED("fixed", "is fixed"),
		BROKEN("broken", "is broken"),
		CANCELLED("cancelled", "is cancelled");

		private String value;
		private String verbString;

		Event(String value, String verbString) {
			this.value = value;
			this.verbString = verbString;
		}

		public String getValue() {
			return value;
		}

		public String getVerbString() {
			return verbString;
		}
	}
}
