package ch.adnovum.gong.notifier.buildstatus;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class BuildStatusNotificationListener implements NotificationListener {


	private static final Logger LOGGER = Logger.getLoggerFor(BuildStatusNotificationListener.class);

	private final BuildStatusService statusService;
	private final GitHostClient gitHostClient;
	private final String serverDisplayUrl;
	private String globalAccessToken;

	public BuildStatusNotificationListener(BuildStatusService statusService,
			GitHostClient gitHostClient, String serverDisplayUrl) {
		this.statusService = statusService;
		this.gitHostClient = gitHostClient;
		this.serverDisplayUrl = serverDisplayUrl;
	}

	public void setGlobalAccessToken(String globalAccessToken) {
		this.globalAccessToken = globalAccessToken;
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		MaterialInfo materialInfo = statusService.getMaterialInfo(stateChange);
		if (materialInfo == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a compatible material. Skipping.");
			return;
		}

		String accessToken = statusService.fetchAccessToken(stateChange, materialInfo);
		if (accessToken == null && !Strings.isNullOrEmpty(globalAccessToken)) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not a valid access token. Falling back to global access token.");
			accessToken = globalAccessToken;
		}
		if (accessToken == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not a valid access token. Skipping.");
			return;
		}

		String context = String.format("GoCD/%s/%s", stateChange.getPipelineName(), stateChange.getStageName());
		String urlToPipeline = String.format("%s/pipelines/%s/%d/%s/%d",
				serverDisplayUrl, stateChange.getPipelineName(), stateChange.getPipelineCounter(),
				stateChange.getStageName(), stateChange.getStageCounter());

		LOGGER.debug(pipelineLogPrefix(stateChange) + "changed to " + event +
				". Access token: " + maskToken(accessToken) +
				". Repo: " + materialInfo.getRepoCoordinates() +
				". Revision: " + materialInfo.getRevision() +
				". Url to pipeline: " + urlToPipeline);

		try {
			gitHostClient.updateCommitStatus(materialInfo, event, context, urlToPipeline, accessToken);
		} catch (GitHostClient.GitHostClientException e) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not update commit status", e);
		}
	}

	private static String maskToken(String token) {
		return token.substring(0,2) + "..." + token.substring(token.length() - 2);
	}

	private static String pipelineLogPrefix(StageStateChange stateChange) {
		return "Pipeline " + stateChange.getPipelineName() + ": ";
	}
}
