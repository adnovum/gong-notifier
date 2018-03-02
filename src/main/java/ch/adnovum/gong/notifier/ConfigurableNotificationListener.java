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

	private PipelineInfoProvider pipelineInfo;
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
		handle(stateChange,"building");
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		handle(stateChange,"passed");
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		handle(stateChange,"failed");
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		handle(stateChange,"broken");
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		handle(stateChange,"fixed");
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		handle(stateChange,"cancelled");
	}

	private void handle(StageStateChange stateChange, String state) {
		List<String> targets = lookupTargets(stateChange.getPipelineName(), state);
		if (targets.isEmpty()) {
			return;
		}

		notifyTargets(stateChange, state, targets);
	}

	protected abstract void notifyTargets(StageStateChange stateChange, String state, List<String> targets);

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


}
