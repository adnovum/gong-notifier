package ch.adnovum.gong.notifier.buildstatus;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.RepoCoordinates;
import ch.adnovum.gong.notifier.go.api.StageStateChange;

public class BuildStatusTestHelper {

	static final String GIT_TYPE = "git";
	static final String GIT_PLUGIN = "git.plugin";
	static final String PR_TYPE = "scm";
	static final String PR_PLUGIN = "my.pr.plugin";

	public static GitHostSpec getFakeGitHostSpec() {
		return new GitHostSpec() {
			@Override
			public Optional<MaterialType> matchMaterial(StageStateChange.Material mat) {
				if (GIT_TYPE.equals(mat.type) && GIT_PLUGIN.equals(mat.pluginId)) {
					return Optional.of(MaterialType.GIT);
				}
				else if (PR_TYPE.equals(mat.type) && PR_PLUGIN.equals(mat.pluginId)) {
					return Optional.of(MaterialType.SCM);
				}
				return Optional.empty();
			}

			@Override
			public Optional<RepoCoordinates> extractRepoCoordinates(String url) {
				return Optional.of(new RepoCoordinates(url.split("/")[0], url.split("/")[1]));
			}
		};
	}

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
