package ch.adnovum.gong.notifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.adnovum.gong.notifier.email.PipelineInfoProvider;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.thoughtworks.go.plugin.api.logging.Logger;

public abstract class ConfigurableNotificationListener implements NotificationListener {

	private static Logger LOGGER = Logger.getLoggerFor(ConfigurableNotificationListener.class);

	protected PipelineInfoProvider pipelineInfo;
	private String targetEnvVariablePrefix;
	private String targetStatesEnvVariableSuffix;

	public ConfigurableNotificationListener(PipelineInfoProvider pipelineInfo, String targetEnvVariablePrefix,
			String targetStatesEnvVariableSuffix) {
		this.pipelineInfo = pipelineInfo;
		this.targetEnvVariablePrefix = targetEnvVariablePrefix;
		this.targetStatesEnvVariableSuffix = targetStatesEnvVariableSuffix;
	}

	@Override
	public void handleBuilding(StageStateChange stateChange) {
		handle(stateChange,TransitionState.BUILDING);
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		handle(stateChange,TransitionState.PASSED);
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		handle(stateChange,TransitionState.FAILED);
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		handle(stateChange,TransitionState.BROKEN);
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		handle(stateChange,TransitionState.FIXED);
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		handle(stateChange,TransitionState.CANCELLED);
	}

	private void handle(StageStateChange stateChange, TransitionState state) {
		List<String> targets = lookupTargets(stateChange.getPipelineName(), state.getValue());
		if (targets.isEmpty()) {
			LOGGER.info("No targets found for " + stateChange.getPipelineName() + " with state " + state.getValue());
			return;
		}

		notifyTargets(stateChange, state, targets);
	}

	protected abstract void notifyTargets(StageStateChange stateChange, TransitionState state, List<String> targets);

	private List<String> lookupTargets(String pipelineName, String state) {
		PipelineConfig cfg = pipelineInfo.getPipelineConfig(pipelineName).orElse(null);
		if (cfg == null) {
			LOGGER.error("Could not retrieve pipeline config for pipeline " + pipelineName);
			return new LinkedList<>();
		}

		Map<String, String> targets = new HashMap<>();
		Set<String> notMatching = new HashSet<>();
		for (PipelineConfig.EnvironmentVariable v: cfg.environmentVariables) {
			if (v.name.startsWith(targetEnvVariablePrefix)) {
				if (v.name.endsWith(targetStatesEnvVariableSuffix)) {
					// TODO: allow negating with !
					if (!v.value.toLowerCase().contains(state)) {
						notMatching.add(v.name.substring(0, v.name.length() - targetStatesEnvVariableSuffix.length()));
					}
				}
				else {
					targets.put(v.name, v.value);
				}
			}
		}
		notMatching.forEach(targets::remove);

		return new LinkedList<>(targets.values());
	}

	public enum TransitionState {
		BUILDING("building", "is building"),
		PASSED("passed", "passed"),
		FAILED("failed", "failed"),
		FIXED("fixed", "is fixed"),
		BROKEN("broken", "is broken"),
		CANCELLED("cancelled", "is cancelled");

		private String value;
		private String verbString;

		TransitionState(String value, String verbString) {
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
