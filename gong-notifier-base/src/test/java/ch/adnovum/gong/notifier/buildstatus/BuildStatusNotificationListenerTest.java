package ch.adnovum.gong.notifier.buildstatus;

import static ch.adnovum.gong.notifier.buildstatus.BuildStatusService.STATUS_AUTH_TOKEN;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.PR_PLUGIN;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.PR_TYPE;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.createMaterial;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.createStageChangeWithMaterialAndRevision;
import static ch.adnovum.gong.notifier.buildstatus.BuildStatusTestHelper.getFakeGitHostSpec;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildStatusNotificationListenerTest {

	@Mock
	private ConfigService cfgService;

	@Mock
	private SecretDecryptService decryptService;

	@Mock
	private GitHostClient hostClient;

	@Captor
	private ArgumentCaptor<MaterialInfo> matInfo;

	private BuildStatusNotificationListener statusStatusListener;

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
		BuildStatusService statusService = new BuildStatusService(cfgService, decryptService, getFakeGitHostSpec());

		statusStatusListener = new BuildStatusNotificationListener(statusService, hostClient, "http://ci.localhost/go");

		stateChange = createStageChangeWithMaterialAndRevision(pipeline, pipelineCounter, stage,
				stageCounter, revision,
				createMaterial(PR_TYPE, PR_PLUGIN, "adnovum/gong-notifier"));

		pipelineConfig = new PipelineConfig();
		pipelineConfig.addSecureVariable(STATUS_AUTH_TOKEN, encryptedAuthToken);
		when(cfgService.fetchPipelineConfig(pipeline, pipelineCounter)).thenReturn(Optional.of(pipelineConfig));
		when(decryptService.decrypt(encryptedAuthToken)).thenReturn(authToken);
	}

	@Test
	public void shouldHandleStatusUpdate() throws Exception {
		statusStatusListener.handle(event, stateChange);


		verify(hostClient).updateCommitStatus(matInfo.capture(), eq(event), eq(expectedContext), eq(expectedUrlToPipeline),
				eq(authToken));
		assertEquals("adnovum", matInfo.getValue().getRepoCoordinates().getProject());
		assertEquals("gong-notifier", matInfo.getValue().getRepoCoordinates().getRepo());
		assertEquals(revision, matInfo.getValue().getRevision());
	}

	@Test
	public void shouldHandleStatusUpdate_NoAuthToken() throws Exception {
		pipelineConfig.environmentVariables = emptyList();

		statusStatusListener.handle(event, stateChange);

		verify(hostClient, never()).updateCommitStatus(any(), any(), anyString(), anyString(), anyString());
	}

	@Test
	public void shouldHandleStatusUpdate_NoPR() throws Exception {
		stateChange = createStageChangeWithMaterialAndRevision(pipeline, pipelineCounter, stage,
				stageCounter, revision,
				createMaterial("some-other-type", null, "https://github.com/adnovum/gong-notifier.git"));

		statusStatusListener.handle(event, stateChange);

		verify(cfgService, never()).fetchPipelineConfig(anyString(), anyInt());
		verify(hostClient, never()).updateCommitStatus(any(), any(), anyString(), anyString(), anyString());
	}

	@Test
	public void shouldHandleStatusUpdate_BadEncryptedAuthToken() throws Exception {
		when(decryptService.decrypt(encryptedAuthToken)).thenThrow(new SecretDecryptException("bad"));

		statusStatusListener.handle(event, stateChange);

		verify(hostClient, never()).updateCommitStatus(any(), any(), anyString(), anyString(), anyString());
	}
}
