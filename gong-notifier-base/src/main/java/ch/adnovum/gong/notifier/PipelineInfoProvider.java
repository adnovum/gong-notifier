package ch.adnovum.gong.notifier;

import java.util.Optional;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;

public interface PipelineInfoProvider {

	Optional<PipelineConfig> getPipelineConfig(String pipelineName, int pipelineCounter);
	Optional<PipelineHistory> getPipelineHistory(String pipelineName, int pipelineCounter);
}
