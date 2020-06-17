package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.go.api.StageStateChange;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class GithubPRStatusTestHelper {

	public static StageStateChange createStageChangeWithMaterials(String pipelineName, int pipelineCounter,
																  String stageName, int stageCounter,
																  StageStateChange.Material... mats) {
		StageStateChange stateChange = new StageStateChange();
		stateChange.pipeline = new StageStateChange.Pipeline();
		stateChange.pipeline.name = pipelineName;
		stateChange.pipeline.counter = pipelineCounter;
		stateChange.pipeline.stage = new StageStateChange.Stage();
		stateChange.pipeline.stage.name = stageName;
		stateChange.pipeline.stage.counter = stageCounter;
		stateChange.pipeline.buildCause = Arrays.stream(mats).map(mat -> {
			StageStateChange.BuildCause bc = new StageStateChange.BuildCause();
			bc.material = mat;
			return bc;
		}).collect(Collectors.toList());
		return stateChange;
	}

	public static StageStateChange createStageChangeWithMaterials(StageStateChange.Material... mats) {
		return createStageChangeWithMaterials("dummyPipeline", 7, "dummyStage", 1, mats);
	}

	public static StageStateChange.Material createMaterial(String type, String pluginId, String url) {
		StageStateChange.Material mat = new StageStateChange.Material();
		mat.type = type;
		mat.pluginId = pluginId;
		mat.configuration = new StageStateChange.MaterialConfig();
		mat.configuration.url = url;
		return mat;
	}

	public static StageStateChange createStageChangeWithMaterialAndRevision(String pipelineName, int pipelineCounter,
																			String stageName, int stageCounter,
																			String revision,
																			StageStateChange.Material mat) {
		StageStateChange stateChange = createStageChangeWithMaterials(pipelineName, pipelineCounter,
				stageName, stageCounter, mat);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = singletonList(mod);
		return stateChange;
	}
}
