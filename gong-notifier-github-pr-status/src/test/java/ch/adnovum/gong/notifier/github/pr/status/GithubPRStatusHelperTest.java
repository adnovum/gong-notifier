package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.go.api.StageStateChange;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusHelper.EXPECTED_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusHelper.EXPECTED_SCM_PLUGIN_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GithubPRStatusHelperTest {

	@Test
	public void shoudGetGithubPRMaterialUrl() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, url)
		);

		String foundUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		assertEquals(url, foundUrl);
	}

	@Test
	public void shoudGetGithubPRMaterialUrl_ManyBuildCauses() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", EXPECTED_SCM_PLUGIN_ID, "different/mat/type"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, "not/a/gh/url"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, url)

		);

		String foundUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		assertEquals(url, foundUrl);
	}

	@Test
	public void shoudGetGithubPRMaterialUrl_NoMatchingBuildCause() {
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", EXPECTED_SCM_PLUGIN_ID, "different/mat/type"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, "not/a/gh/url")
		);

		String foundUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		assertNull(foundUrl);
	}

	@Test
	public void shoudGetGithubPRMaterialUrl_NoBuildCauses() {
		StageStateChange stateChange = createStageChangeWithMaterials();

		String foundUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		assertNull(foundUrl);
	}

	@Test
	public void shoudGetGithubPRMaterialUrl_BuildCauseWithoutMaterial() {
		StageStateChange stateChange = createStageChangeWithMaterials();
		stateChange.pipeline.buildCause = Collections.singletonList(new StageStateChange.BuildCause());

		String foundUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		assertNull(foundUrl);
	}

	@Test
	public void shouldGetRepoFromUrl() {
		String[] params = new String[]{
				"https://github.com/adnovum/gong-notifier.git", "adnovum/gong-notifier",
				"git@github.com:adnovum/gong-notifier.git", "adnovum/gong-notifier",
				"https://github.com/adnovum/gong-notifier", "adnovum/gong-notifier",
				"git@github.com:adnovum/gong-notifier", "adnovum/gong-notifier",
				"http://blaa@github124.de/adnovum/gong-notifier.git", "adnovum/gong-notifier",
				"blabasdgeg@github12424.de:adnovum/gong-notifier", "adnovum/gong-notifier",
				"http://github.company.com:1234/adnovum/gong-notifier.git", "adnovum/gong-notifier",
				"git@github.company.com:1234:adnovum/gong-notifier", "adnovum/gong-notifier",
				"blablabla", null,
				"//", null,
				"", null,
				null, null
		};

		for (int i = 0; i < params.length; i += 2) {
			String input = params[i];
			String output = params[i + 1];
			assertEquals("Input " + input, output, GithubPRStatusHelper.getRepoFromUrl(input));
		}
	}

	private StageStateChange createStageChangeWithMaterials(StageStateChange.Material... mats) {
		StageStateChange stateChange = new StageStateChange();
		stateChange.pipeline = new StageStateChange.Pipeline();
		stateChange.pipeline.buildCause = Arrays.stream(mats).map(mat -> {
			StageStateChange.BuildCause bc = new StageStateChange.BuildCause();
			bc.material = mat;
			return bc;
		}).collect(Collectors.toList());
		return stateChange;
	}

	private StageStateChange.Material createMaterial(String type, String pluginId, String url) {
		StageStateChange.Material mat = new StageStateChange.Material();
		mat.type = type;
		mat.pluginId = pluginId;
		mat.configuration = new StageStateChange.MaterialConfig();
		mat.configuration.url = url;
		return mat;
	}
}