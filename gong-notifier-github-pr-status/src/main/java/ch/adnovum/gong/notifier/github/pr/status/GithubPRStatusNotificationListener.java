package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.SecretDecryptException;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GithubPRStatusNotificationListener implements NotificationListener {


	private static final Logger LOGGER = Logger.getLoggerFor(GithubPRStatusNotificationListener.class);

	private final ConfigService cfgService;
	private final SecretDecryptService decryptService;
	private final GithubClient githubClient;
	private final String serverDisplayUrl;

	public GithubPRStatusNotificationListener(ConfigService cfgService, SecretDecryptService decryptService,
											  GithubClient githubClient, String serverDisplayUrl) {
		this.cfgService = cfgService;
		this.decryptService = decryptService;
		this.githubClient = githubClient;
		this.serverDisplayUrl = serverDisplayUrl;
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		GithubPRStatusHelper.GithubPRInfo prInfo = GithubPRStatusHelper.getGithubPRInfo(stateChange);
		if (prInfo == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a Github PR material. Skipping.");
			return;
		}
		if (prInfo.getRevision() == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a Github PR revision. Skipping.");
			return;
		}

		String repo = GithubPRStatusHelper.getRepoFromUrl(prInfo.getUrl());
		if (repo == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "cannot extract valid Github repo from " +
					"PR material URL " + prInfo.getUrl() + ". Skipping.");
			return;
		}

		EnvironmentVariable authTokenVar = fetchAccessTokenVariable(stateChange);
		if (authTokenVar == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have " + GithubPRStatusHelper.STATUS_AUTH_TOKEN +
					" set. Skipping.");
			return;
		}

		String authToken;
		try {
			authToken = decryptValue(authTokenVar);
		} catch (SecretDecryptException ex) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not decrypt " + GithubPRStatusHelper.STATUS_AUTH_TOKEN);
			return;
		}

		String context = String.format("GoCD/%s/%s", stateChange.getPipelineName(), stateChange.getStageName());
		String urlToPipeline = String.format("%s/pipelines/%s/%d/%s/%d",
				serverDisplayUrl, stateChange.getPipelineName(), stateChange.getPipelineCounter(),
				stateChange.getStageName(), stateChange.getStageCounter());

		LOGGER.debug(pipelineLogPrefix(stateChange) + "changed to " + event +
				". Auth token: " + authToken.substring(0,2) + "..." + authToken.substring(authToken.length() - 2) +
				". Github repo: " + repo +
				". Revision: " + prInfo.getRevision() +
				". Url to pipeline: " + urlToPipeline);

		try {
			githubClient.updateCommitStatus(repo, prInfo.getRevision(), event, context,	urlToPipeline, authToken);
		} catch (GithubClient.GithubException e) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not update Github commit status", e);
		}
	}

	private EnvironmentVariable fetchAccessTokenVariable(StageStateChange stateChange) {
		return GithubPRStatusHelper.fetchAccessTokenVariable(stateChange, cfgService).orElse(null);
	}

	private String decryptValue(EnvironmentVariable var) throws SecretDecryptException {
		return var.secure ? decryptService.decrypt(var.encryptedValue) : var.value;
	}

	private static String pipelineLogPrefix(StageStateChange stateChange) {
		return "Pipeline " + stateChange.getPipelineName() + ": ";
	}
}
