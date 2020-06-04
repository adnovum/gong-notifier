package ch.adnovum.gong.notifier.services;

import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.util.SessionCache;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ConfigService {

	private static Logger LOGGER = Logger.getLoggerFor(ConfigService.class);

	private final SessionCache<String, PipelineConfig, Integer> configCache;

	public ConfigService(GoServerApi api) {
		configCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchPipelineConfig(k));
	}

	public Optional<PipelineConfig> fetchPipelineConfig(String pipelineName, int pipelineCounter) {
		return configCache.fetch(pipelineName, pipelineCounter);
	}
}
