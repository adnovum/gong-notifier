package ch.adnovum.gong.notifier.go.api;

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
		pipeline.stage.name = stageName;
		pipeline.stage.state = stageState;
	}

	public static class Pipeline {
		public String name;
		public Stage stage;
		public Integer counter;
	}

	public static class Stage {
		public String name;
		public String state;
		public Integer counter;
	}
}
