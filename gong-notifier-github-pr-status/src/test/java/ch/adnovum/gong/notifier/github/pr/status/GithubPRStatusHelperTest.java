package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.go.api.StageStateChange;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusHelper.*;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createMaterial;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createStageChangeWithMaterials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GithubPRStatusHelperTest {

	@Test
	public void shoudGetGithubPRInfo() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, url)
		);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = Collections.singletonList(mod);

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertEquals(url, info.getUrl());
		assertEquals(revision, info.getRevision());
	}

	@Test
	public void shoudGetGithubPRInfo_WithoutRevision() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, url)
		);

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertEquals(url, info.getUrl());
		assertNull(info.getRevision());
	}

	@Test
	public void shoudGetGithubPRInfo_ManyBuildCauses() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", EXPECTED_SCM_PLUGIN_ID, "different/mat/type"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, "not/a/gh/url"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, url)

		);

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertEquals(url, info.getUrl());
	}

	@Test
	public void shoudGetGithubPRInfo_NoMatchingBuildCause() {
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(EXPECTED_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", EXPECTED_SCM_PLUGIN_ID, "different/mat/type"),
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, "not/a/gh/url")
		);

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertNull(info);
	}

	@Test
	public void shoudGetGithubPRInfo_NoBuildCauses() {
		StageStateChange stateChange = createStageChangeWithMaterials();

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertNull(info);
	}

	@Test
	public void shoudgetGithubPRInfo_BuildCauseWithoutMaterial() {
		StageStateChange stateChange = createStageChangeWithMaterials();
		stateChange.pipeline.buildCause = Collections.singletonList(new StageStateChange.BuildCause());

		GithubPRInfo info = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		assertNull(info);
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
}