package ch.adnovum.gong.notifier.go.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StageStateChange {

	public Pipeline pipeline;

	public String getState() {
		return pipeline.stage.state;
	}

	public String getPipelineName() {
		return pipeline.name;
	}

	public String getStageName() {
		return pipeline.stage.name;
	}

	public Integer getPipelineCounter() {
		return pipeline.counter;
	}

	public Integer getStageCounter() {
		return pipeline.stage.counter;
	}

	public StageStateChange() {
	}

	public StageStateChange(String pipelineName, int counter, String stageName, String stageState) {
		pipeline = new Pipeline();
		pipeline.name = pipelineName;
		pipeline.stage = new Stage();
		pipeline.counter = counter;
		pipeline.stage.name = stageName;
		pipeline.stage.state = stageState;
	}

	public static class Pipeline {
		public String name;
		public Stage stage;
		public Integer counter;
		@SerializedName("build-cause")
		public List<BuildCause> buildCause;
	}

	public static class Stage {
		public String name;
		public String state;
		public Integer counter;
	}

	public static class BuildCause {
		public Material material;
	}

	public static class Material {

		public String type;

		@SerializedName("plugin-id")
		public String pluginId;

		@SerializedName(value = "scm-configuration", alternate = {"git-configuration"})
		public MaterialConfig configuration;
	}

	public static class MaterialConfig {
		public String url;
	}
}
