package ch.adnovum.gong.notifier.email;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;

import java.util.Optional;

public interface PipelineInfoProvider {

	Optional<PipelineConfig> getPipelineConfig(String pipelineName);
	Optional<PipelineHistory> getPipelineHistory(String pipelineName);
}
