package ch.adnovum.gong.notifier.github.pr.status;

import static ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.GIT_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.GIT_PLUGIN_ID;
import static ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.SCM_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.PR_PLUGIN_ID;
import static ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.GithubInfo;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createMaterial;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createStageChangeWithMaterials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import ch.adnovum.gong.notifier.github.pr.status.GithubStatusHelper.GithubMaterialType;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GithubStatusHelperTest {

	@Mock
	private ConfigService cfgService;

	@Test
	public void shoudGetGithubInfo_ForPRMaterial() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, url)
		);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = Collections.singletonList(mod);

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertEquals(url, info.getUrl());
		assertEquals(revision, info.getRevision());
	}

	@Test
	public void shoudGetGithubInfo_ForGitMaterial() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(GIT_MATERIAL_TYPE, GIT_PLUGIN_ID, url)
		);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = Collections.singletonList(mod);

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertEquals(url, info.getUrl());
		assertEquals(revision, info.getRevision());
	}

	@Test
	public void shoudGetGithubInfo_WithoutRevision() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, url)
		);

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertEquals(url, info.getUrl());
		assertNull(info.getRevision());
	}

	@Test
	public void shoudGetGithubInfo_ManyBuildCauses() {
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", PR_PLUGIN_ID, "different/mat/type"),
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, "not/a/gh/url"),
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, url)

		);

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertEquals(url, info.getUrl());
	}

	@Test
	public void shoudGetGithubInfo_NoMatchingBuildCause() {
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, "different.plugin.id", "different/plugin/url"),
				createMaterial("different-type", PR_PLUGIN_ID, "different/mat/type"),
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, "not/a/gh/url")
		);

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertNull(info);
	}

	@Test
	public void shoudGetGithubInfo_NoBuildCauses() {
		StageStateChange stateChange = createStageChangeWithMaterials();

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
		assertNull(info);
	}

	@Test
	public void shoudgetGithubInfo_BuildCauseWithoutMaterial() {
		StageStateChange stateChange = createStageChangeWithMaterials();
		stateChange.pipeline.buildCause = Collections.singletonList(new StageStateChange.BuildCause());

		GithubInfo info = GithubStatusHelper.getGithubInfo(stateChange);
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
			assertEquals("Input " + input, output, GithubStatusHelper.getRepoFromUrl(input));
		}
	}

	@Test
	public void shouldFetchAccessTokenVariable_FromPipelineConfig() {
		// Given
		final String encryptedToken = "AES:12345";
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, url)
		);

		GithubInfo ghInfo = new GithubInfo(GithubMaterialType.PR, url, "1234");

		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.addSecureVariable(GithubStatusHelper.STATUS_AUTH_TOKEN, encryptedToken);
		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));

		// When
		EnvironmentVariable tokenVar = GithubStatusHelper.fetchAccessTokenVariable(stateChange, ghInfo, cfgService).orElse(null);

		// Then
		assertEquals(encryptedToken, tokenVar.encryptedValue);
	}

	@Test
	public void shouldFetchAccessTokenVariable_FromPRMaterial() {
		// Given
		final String encryptedToken = "AES:12345";
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(SCM_MATERIAL_TYPE, PR_PLUGIN_ID, url)
		);

		GithubInfo ghInfo = new GithubInfo(GithubMaterialType.PR, url, "1234");

		final String matName = "foo.bar.pr";
		PipelineConfig.Material material = new PipelineConfig.Material();
		material.type = "plugin";
		material.attributes.put("ref", matName);
		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.materials.add(material);

		ScmConfig matConfig = scmConfigForPR(matName, encryptedToken);

		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));
		when(cfgService.fetchScmConfig(matName)).thenReturn(Optional.of(matConfig));

		// When
		EnvironmentVariable tokenVar = GithubStatusHelper.fetchAccessTokenVariable(stateChange, ghInfo, cfgService).orElse(null);

		// Then
		assertEquals(encryptedToken, tokenVar.encryptedValue);
	}

	@Test
	public void shouldFetchAccessTokenVariable_FromGitMaterial() {
		// Given
		final String encryptedToken = "AES:12345";
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(GIT_MATERIAL_TYPE, GIT_PLUGIN_ID, url)
		);

		GithubInfo ghInfo = new GithubInfo(GithubMaterialType.GIT, url, "1234");

		PipelineConfig.Material material = new PipelineConfig.Material();
		material.type = "git";
		material.attributes.put("url", url);
		material.attributes.put("encrypted_password", encryptedToken);
		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.materials.add(material);

		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));

		// When
		EnvironmentVariable tokenVar = GithubStatusHelper.fetchAccessTokenVariable(stateChange, ghInfo, cfgService).orElse(null);

		// Then
		assertEquals(encryptedToken, tokenVar.encryptedValue);
	}

	private ScmConfig scmConfigForPR(String matName, String password) {
		ScmConfig matConfig = new ScmConfig();
		matConfig.id = matName;
		matConfig.name = matName;
		ScmConfig.ConfigEntry cfgEntry = new ScmConfig.ConfigEntry();
		cfgEntry.key = "password";
		cfgEntry.encryptedValue = password;
		matConfig.configuration.add(cfgEntry);
		return matConfig;
	}
}
