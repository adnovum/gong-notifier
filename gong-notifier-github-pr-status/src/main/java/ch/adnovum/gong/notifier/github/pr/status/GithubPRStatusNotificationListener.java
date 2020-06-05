package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.SecretDecryptException;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.File;

public class GithubPRStatusNotificationListener implements NotificationListener {

	private static final String STATUS_ACCESS_TOKEN = "GONG_STATUS_ACCESS_TOKEN";


	private static Logger LOGGER = Logger.getLoggerFor(GithubPRStatusNotificationListener.class);

	private final ConfigService cfgService;
	private final SecretDecryptService decryptService;

	public GithubPRStatusNotificationListener(ConfigService cfgService, SecretDecryptService decryptService) {
		this.cfgService = cfgService;
		this.decryptService = decryptService;
	}

	public static void main(String[] args) {
		GoServerApi api = new GoServerApi("http://192.168.99.100:8153/go");
		ConfigService srv = new ConfigService(api);
		SecretDecryptService decSrv = new SecretDecryptService(new File("D:\\Projects\\java\\gong-notifier\\cipher.aes"));
		GithubPRStatusNotificationListener lst = new GithubPRStatusNotificationListener(srv, decSrv);

		StageStateChange dummyChange =  new StageStateChange("pipeline1",10,
				"stage1","passed");
		lst.handle(BaseEvent.PASSED, dummyChange);
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		if (event != BaseEvent.PASSED && event != BaseEvent.FAILED) {
			// Ignore other events.
			return;
		}

		String prMaterialUrl = GithubPRStatusHelper.getGithubPRMaterialUrl(stateChange);
		if (prMaterialUrl == null) {
			LOGGER.debug("Pipeline " + stateChange.getPipelineName() + " does not have a PR material URL. Skipping.");
			return;
		}

		String repo = GithubPRStatusHelper.getRepoFromUrl(prMaterialUrl);
		if (repo == null) {
			LOGGER.debug("Pipeline " + stateChange.getPipelineName() + ": Cannot extract valid Github repo from " +
					"PR material URL " + prMaterialUrl + ". Skipping.");
			return;
		}

		EnvironmentVariable accessTokenVar = fetchAccessTokenVariable(stateChange);
		if (accessTokenVar == null) {
			LOGGER.debug("Pipeline " + stateChange.getPipelineName() + " does not have " + STATUS_ACCESS_TOKEN +
					" set. Skipping.");
			return;
		}

		String accessToken;
		try {
			accessToken = getDecryptedValue(accessTokenVar);
		} catch (SecretDecryptException ex) {
			LOGGER.error("Could not decrypt " + STATUS_ACCESS_TOKEN + " for pipeline " + stateChange.getPipelineName());
			return;
		}

		LOGGER.info(stateChange.getPipelineName() + " changed to " + event +
				". Access token: " + accessToken +
				". Github repo: " + repo);
	}

	private EnvironmentVariable fetchAccessTokenVariable(StageStateChange stateChange) {
		return cfgService.fetchPipelineConfig(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.flatMap(c -> c.getEnvironmentVariable(STATUS_ACCESS_TOKEN))
				.orElse(null);
	}

	private String getDecryptedValue(EnvironmentVariable var) throws SecretDecryptException {
		return var.secure ? decryptService.decrypt(var.encryptedValue) : var.value;
	}
}
