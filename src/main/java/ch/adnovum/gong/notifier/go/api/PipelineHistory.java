package ch.adnovum.gong.notifier.go.api;

import java.util.List;
import java.util.Optional;

public class PipelineHistory {

	public List<Pipeline> pipelines;

	public Optional<String> getPreviousStageResult(String stageName, int currentPipelineCounter) {
		return pipelines.stream()
				.filter(p -> p.counter < currentPipelineCounter)
				.flatMap(p -> p.stages.stream())
				.filter(s -> s.name.equals(stageName) && !GoApiConstants.STATUS_UNKNOWN.equals(s.result))
				.map(s -> s.result)
				.findFirst();
	}

	public static class Pipeline {
		public List<Stage> stages;
		public Integer counter;
	}

	public static class Stage {
		public String name;
		public String result;
	}
}
