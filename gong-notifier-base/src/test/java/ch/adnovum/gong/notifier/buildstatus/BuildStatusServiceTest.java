package ch.adnovum.gong.notifier.buildstatus;

import static ch.adnovum.gong.notifier.buildstatus.BuildStatusService.STATUS_AUTH_TOKEN;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.GIT_PLUGIN;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.GIT_TYPE;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.PR_PLUGIN;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.PR_TYPE;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.createMaterial;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.createStageChangeWithMaterials;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.getFakeGitHostSpec;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.RepoCoordinates;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.SecretDecryptException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildStatusServiceTest {

	@Mock
	private ConfigService cfgService;

	@Mock
	private SecretDecryptService decryptService;

	private BuildStatusService statusService;

	@Before
	public void setup() {
		statusService = new BuildStatusService(cfgService, decryptService, getFakeGitHostSpec());
	}

	@Test
	public void shoudGetMaterialInfo_ForPRMaterial() {
		final String url = "adnovum/gong-notifier";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(PR_TYPE, PR_PLUGIN, url)
		);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = Collections.singletonList(mod);

		MaterialInfo info = statusService.getMaterialInfo(stateChange);

		assertEquals(MaterialType.SCM, info.getMaterialType());
		assertEquals(url, info.getUrl());
		assertEquals("adnovum", info.getRepoCoordinates().getProject());
		assertEquals("gong-notifier", info.getRepoCoordinates().getRepo());
		assertEquals(revision, info.getRevision());
	}

	@Test
	public void shoudGetMaterialInfo_ForGitMaterial() {
		final String url = "adnovum/gong-notifier";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(GIT_TYPE, GIT_PLUGIN, url)
		);
		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(0).modifications = Collections.singletonList(mod);

		MaterialInfo info = statusService.getMaterialInfo(stateChange);

		assertEquals(MaterialType.GIT, info.getMaterialType());
		assertEquals(url, info.getUrl());
		assertEquals("adnovum", info.getRepoCoordinates().getProject());
		assertEquals("gong-notifier", info.getRepoCoordinates().getRepo());
		assertEquals(revision, info.getRevision());
	}

	@Test
	public void shoudGetMaterialInfo_WithoutRevision() {
		final String url = "adnovum/gong-notifier";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(PR_TYPE, PR_PLUGIN, url)
		);

		MaterialInfo info = statusService.getMaterialInfo(stateChange);
		assertNull(info);
	}

	@Test
	public void shoudGetMaterialInfo_ManyBuildCauses() {
		final String url = "adnovum/gong-notifier";
		final String revision = "12345";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(PR_TYPE, "different.plugin.id", url),
				createMaterial("different-type", PR_PLUGIN, url),
				createMaterial(PR_TYPE, PR_PLUGIN, url)
		);

		StageStateChange.Modification mod = new StageStateChange.Modification();
		mod.revision = revision;
		stateChange.pipeline.buildCause.get(2).modifications = Collections.singletonList(mod);

		MaterialInfo info = statusService.getMaterialInfo(stateChange);

		assertEquals(MaterialType.SCM, info.getMaterialType());
		assertEquals(url, info.getUrl());
		assertEquals("adnovum", info.getRepoCoordinates().getProject());
		assertEquals("gong-notifier", info.getRepoCoordinates().getRepo());
		assertEquals(revision, info.getRevision());
	}


	@Test
	public void shoudGetMaterialInfo_NoBuildCauses() {
		StageStateChange stateChange = createStageChangeWithMaterials();

		MaterialInfo info = statusService.getMaterialInfo(stateChange);

		assertNull(info);
	}

	@Test
	public void shoudGetMaterialInfo_BuildCauseWithoutMaterial() {
		StageStateChange stateChange = createStageChangeWithMaterials();
		stateChange.pipeline.buildCause = Collections.singletonList(new StageStateChange.BuildCause());

		MaterialInfo info = statusService.getMaterialInfo(stateChange);

		assertNull(info);
	}

	@Test
	public void shouldFetchAccessToken_FromPipelineConfig() throws SecretDecryptException {
		// Given
		final String encryptedToken = "AES:12345";
		final String decryptedToken = "abc";
		final String url = "adnovum/gong-notifier";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(PR_TYPE, PR_PLUGIN, url)
		);

		MaterialInfo matInfo = new MaterialInfo();
		matInfo.setMaterialType(MaterialType.SCM);
		matInfo.setRepoCoordinates(new RepoCoordinates("adnovum", "gong-notifier"));
		matInfo.setRevision("1234");
		matInfo.setUrl(url);

		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.addSecureVariable(STATUS_AUTH_TOKEN, encryptedToken);
		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));

		when(decryptService.decrypt(encryptedToken)).thenReturn(decryptedToken);

		// When
		String token = statusService.fetchAccessToken(stateChange, matInfo);

		// Then
		assertEquals(decryptedToken, token);
	}

	@Test
	public void shouldFetchAccessTokenVariable_FromScmMaterial() throws SecretDecryptException {
		// Given
		final String encryptedToken = "AES:12345";
		final String decryptedToken = "abc";
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(PR_TYPE, PR_PLUGIN, url)
		);

		MaterialInfo matInfo = new MaterialInfo();
		matInfo.setMaterialType(MaterialType.SCM);
		matInfo.setRepoCoordinates(new RepoCoordinates("adnovum", "gong-notifier"));
		matInfo.setRevision("1234");
		matInfo.setUrl(url);

		final String matName = "foo.bar.pr";
		PipelineConfig.Material material = new PipelineConfig.Material();
		material.type = "plugin";
		material.attributes.put("ref", matName);
		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.materials.add(material);

		ScmConfig matConfig = scmConfigForPR(matName, encryptedToken);

		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));
		when(cfgService.fetchScmConfig(matName)).thenReturn(Optional.of(matConfig));

		when(decryptService.decrypt(encryptedToken)).thenReturn(decryptedToken);

		// When
		String token = statusService.fetchAccessToken(stateChange, matInfo);

		// Then
		assertEquals(decryptedToken, token);
	}

	@Test
	public void shouldFetchAccessTokenVariable_FromGitMaterial() throws SecretDecryptException {
		// Given
		final String encryptedToken = "AES:12345";
		final String decryptedToken = "abc";
		final String url = "https://github.com/adnovum/gong-notifier.git";
		StageStateChange stateChange = createStageChangeWithMaterials(
				createMaterial(GIT_TYPE, GIT_PLUGIN, url)
		);

		MaterialInfo matInfo = new MaterialInfo();
		matInfo.setMaterialType(MaterialType.GIT);
		matInfo.setRepoCoordinates(new RepoCoordinates("adnovum", "gong-notifier"));
		matInfo.setRevision("1234");
		matInfo.setUrl(url);

		PipelineConfig.Material material = new PipelineConfig.Material();
		material.type = "git";
		material.attributes.put("url", url);
		material.attributes.put("encrypted_password", encryptedToken);
		PipelineConfig pipelineConfig = new PipelineConfig();
		pipelineConfig.materials.add(material);

		when(cfgService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.of(pipelineConfig));

		when(decryptService.decrypt(encryptedToken)).thenReturn(decryptedToken);

		// When
		String token = statusService.fetchAccessToken(stateChange, matInfo);

		// Then
		assertEquals(decryptedToken, token);
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
