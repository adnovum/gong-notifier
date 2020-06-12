package ch.adnovum.gong.notifier.github.pr.status;

import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusHelper.EXPECTED_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusHelper.EXPECTED_SCM_PLUGIN_ID;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createMaterial;
import static ch.adnovum.gong.notifier.github.pr.status.GithubPRStatusTestHelper.createStageChangeWithMaterialAndRevision;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
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
public class GithubPRStatusNotificationListenerTest {

	@Mock
	private ConfigService cfgService;

	@Mock
	private SecretDecryptService decryptService;

	@Mock
	private GithubClient ghClient;

	private GithubPRStatusNotificationListener prStatusListener;

	private final String pipeline = "pipeline1";
	private final int pipelineCounter = 7;
	private final String stage = "build";
	private final int stageCounter = 1;
	private final String revision = "12345";
	private final String encryptedAuthToken = "AES:123:456789";
	private final String authToken = "secret1!";
	private final BaseEvent event = BaseEvent.PASSED;
	private final String expectedContext = String.format("GoCD/%s/%s", pipeline, stage);
	private final String expectedUrlToPipeline = String.format("http://ci.localhost/go/pipelines/%s/%d/%s/%d", pipeline,
			pipelineCounter, stage, stageCounter);

	private StageStateChange stateChange;
	private PipelineConfig pipelineConfig;

	@Before
	public void setup() throws SecretDecryptException {
		prStatusListener = new GithubPRStatusNotificationListener(cfgService,
				decryptService, ghClient, "http://ci.localhost/go");

		stateChange = createStageChangeWithMaterialAndRevision(pipeline, pipelineCounter, stage,
				stageCounter, revision,
				createMaterial(EXPECTED_MATERIAL_TYPE, EXPECTED_SCM_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"));

		pipelineConfig = new PipelineConfig();
		pipelineConfig.addSecureVariable(GithubPRStatusHelper.STATUS_AUTH_TOKEN, encryptedAuthToken);
		when(cfgService.fetchPipelineConfig(pipeline, pipelineCounter)).thenReturn(Optional.of(pipelineConfig));
		when(decryptService.decrypt(encryptedAuthToken)).thenReturn(authToken);
	}

	@Test
	public void shouldHandleStatusUpdate() throws Exception {
		prStatusListener.handle(event, stateChange);


		verify(ghClient).updateCommitStatus("adnovum/gong-notifier", revision, event, expectedContext,
				expectedUrlToPipeline, authToken);
	}

	@Test
	public void shouldHandleStatusUpdate_NoAuthToken() throws Exception {
		pipelineConfig.environmentVariables = emptyList();

		prStatusListener.handle(event, stateChange);

		verify(ghClient, never()).updateCommitStatus(anyString(), anyString(), any(), anyString(), anyString(),
				anyString());
	}

	@Test
	public void shouldHandleStatusUpdate_NoPR() throws Exception {
		stateChange = createStageChangeWithMaterialAndRevision(pipeline, pipelineCounter, stage,
				stageCounter, revision,
				createMaterial("some-other-type", null, "https://github.com/adnovum/gong-notifier.git"));

		prStatusListener.handle(event, stateChange);

		verify(cfgService, never()).fetchPipelineConfig(anyString(), anyInt());
		verify(ghClient, never()).updateCommitStatus(anyString(), anyString(), any(), anyString(), anyString(),
				anyString());
	}

	@Test
	public void shouldHandleStatusUpdate_BadEncryptedAuthToken() throws Exception {
		when(decryptService.decrypt(encryptedAuthToken)).thenThrow(new SecretDecryptException("bad"));

		prStatusListener.handle(event, stateChange);

		verify(ghClient, never()).updateCommitStatus(anyString(), anyString(), any(), anyString(), anyString(),
				anyString());
	}
}
