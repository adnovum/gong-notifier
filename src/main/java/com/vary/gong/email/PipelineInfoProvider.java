package com.vary.gong.email;

import com.vary.gong.go.api.PipelineConfig;
import com.vary.gong.go.api.PipelineHistory;

import java.util.Optional;

public interface PipelineInfoProvider {

	Optional<PipelineConfig> getPipelineConfig(String pipelineName);
	Optional<PipelineHistory> getPipelineHistory(String pipelineName);
}
