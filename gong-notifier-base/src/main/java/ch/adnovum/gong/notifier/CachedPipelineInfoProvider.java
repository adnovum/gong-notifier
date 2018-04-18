package ch.adnovum.gong.notifier;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import ch.adnovum.gong.notifier.util.SessionCache;

public class CachedPipelineInfoProvider implements PipelineInfoProvider {

	private SessionCache<String, PipelineConfig, Integer> configCache;
	private SessionCache<String, PipelineHistory, Integer> historyCache;

	public CachedPipelineInfoProvider(GoServerApi api) {
		configCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchPipelineConfig(k));
		historyCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchPipelineHistory(k));
	}

	@Override
	public Optional<PipelineConfig> getPipelineConfig(String pipelineName, int pipelineCounter) {
		return configCache.fetch(pipelineName, pipelineCounter);
	}

	@Override
	public Optional<PipelineHistory> getPipelineHistory(String pipelineName, int pipelineCounter) {
		return historyCache.fetch(pipelineName, pipelineCounter);
	}
}
