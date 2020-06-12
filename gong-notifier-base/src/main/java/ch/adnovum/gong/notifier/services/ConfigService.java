package ch.adnovum.gong.notifier.services;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.util.SessionCache;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class ConfigService {

	private static Logger LOGGER = Logger.getLoggerFor(ConfigService.class);

	private final SessionCache<String, PipelineConfig, Integer> pipelineConfigCache;
	private final SessionCache<String, ScmConfig, Integer> scmConfigCache;

	public ConfigService(GoServerApi api) {
		pipelineConfigCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchPipelineConfig(k));
		scmConfigCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchScmConfig(k));
	}

	public Optional<PipelineConfig> fetchPipelineConfig(String pipelineName, int pipelineCounter) {
		return pipelineConfigCache.fetch(pipelineName, pipelineCounter);
	}

	public Optional<ScmConfig> fetchScmConfig(String scmName) {
		// scm materials aren't bound to a pipeline execution, so it doesn't really make sense to
		// refresh it on new executions. Rather just use the TTL only.
		final int fixedSessionKey = 0;
		return scmConfigCache.fetch(scmName, fixedSessionKey);
	}
}
