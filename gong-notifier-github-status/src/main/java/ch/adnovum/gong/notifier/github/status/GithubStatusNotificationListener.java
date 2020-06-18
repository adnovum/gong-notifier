package ch.adnovum.gong.notifier.github.status;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.github.status.GithubStatusHelper.GithubInfo;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.SecretDecryptException;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GithubStatusNotificationListener implements NotificationListener {


	private static final Logger LOGGER = Logger.getLoggerFor(GithubStatusNotificationListener.class);

	private final ConfigService cfgService;
	private final SecretDecryptService decryptService;
	private final GithubClient githubClient;
	private final String serverDisplayUrl;

	public GithubStatusNotificationListener(ConfigService cfgService, SecretDecryptService decryptService,
											  GithubClient githubClient, String serverDisplayUrl) {
		this.cfgService = cfgService;
		this.decryptService = decryptService;
		this.githubClient = githubClient;
		this.serverDisplayUrl = serverDisplayUrl;
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		GithubInfo ghInfo = GithubStatusHelper.getGithubInfo(stateChange);
		if (ghInfo == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a Github material. Skipping.");
			return;
		}
		if (ghInfo.getRevision() == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a Github revision. Skipping.");
			return;
		}

		String repo = GithubStatusHelper.getRepoFromUrl(ghInfo.getUrl());
		if (repo == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "cannot extract valid Github repo from " +
					"Github material URL " + ghInfo.getUrl() + ". Skipping.");
			return;
		}

		EnvironmentVariable authTokenVar = fetchAccessTokenVariable(stateChange, ghInfo);
		if (authTokenVar == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have " + GithubStatusHelper.STATUS_AUTH_TOKEN +
					" set. Skipping.");
			return;
		}

		String authToken;
		try {
			authToken = decryptValue(authTokenVar);
		} catch (SecretDecryptException ex) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not decrypt " + GithubStatusHelper.STATUS_AUTH_TOKEN);
			return;
		}

		String context = String.format("GoCD/%s/%s", stateChange.getPipelineName(), stateChange.getStageName());
		String urlToPipeline = String.format("%s/pipelines/%s/%d/%s/%d",
				serverDisplayUrl, stateChange.getPipelineName(), stateChange.getPipelineCounter(),
				stateChange.getStageName(), stateChange.getStageCounter());

		LOGGER.debug(pipelineLogPrefix(stateChange) + "changed to " + event +
				". Auth token: " + authToken.substring(0,2) + "..." + authToken.substring(authToken.length() - 2) +
				". Github repo: " + repo +
				". Revision: " + ghInfo.getRevision() +
				". Url to pipeline: " + urlToPipeline);

		try {
			githubClient.updateCommitStatus(repo, ghInfo.getRevision(), event, context,	urlToPipeline, authToken);
		} catch (GithubClient.GithubException e) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not update Github commit status", e);
		}
	}

	private EnvironmentVariable fetchAccessTokenVariable(StageStateChange stateChange, GithubInfo ghInfo) {
		return GithubStatusHelper.fetchAccessTokenVariable(stateChange, ghInfo, cfgService).orElse(null);
	}

	private String decryptValue(EnvironmentVariable var) throws SecretDecryptException {
		return var.secure ? decryptService.decrypt(var.encryptedValue) : var.value;
	}

	private static String pipelineLogPrefix(StageStateChange stateChange) {
		return "Pipeline " + stateChange.getPipelineName() + ": ";
	}
}
