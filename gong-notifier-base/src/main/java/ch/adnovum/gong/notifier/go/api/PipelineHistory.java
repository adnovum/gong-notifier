package ch.adnovum.gong.notifier.go.api;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public class PipelineHistory {

	public List<Pipeline> pipelines;

	public Optional<String> getPreviousStageResult(String stageName, int currentPipelineCounter) {
		return pipelines.stream()
				.filter(p -> p.counter < currentPipelineCounter)
				.flatMap(p -> p.stages.stream())
				.filter(s -> s.name.equals(stageName) && s.result != null && !GoApiConstants.STATUS_UNKNOWN.equals(s.result))
				.map(s -> s.result)
				.findFirst();
	}

	public Optional<BuildCause> getCurrentBuildCause(int currentPipelineCounter) {
		return pipelines.stream()
				.filter(p -> p.counter == currentPipelineCounter && p.buildCause != null)
				.map(p -> p.buildCause)
				.findFirst();
	}


	public static class Pipeline {
		public List<Stage> stages;
		public Integer counter;
		@SerializedName("build_cause")
		public BuildCause buildCause;
	}

	public static class Stage {
		public String name;
		public String result;
	}

	public static class BuildCause {
		@SerializedName("material_revisions")
		public List<MaterialRevision> materialRevisions = new LinkedList<>();
	}

	public static class MaterialRevision {
		public Material material;
		public List<Modification> modifications = new LinkedList<>();
	}

	public static class Material {
		public String description;
		public String type;
	}

	public static class Modification {
		@SerializedName("modified_time")
		public long modifiedTime;

		@SerializedName("user_name")
		public String userName;

		public String revision;

		public String comment;

		public Date getModifiedTime() {
			return new Date(modifiedTime);
		}
	}
}
