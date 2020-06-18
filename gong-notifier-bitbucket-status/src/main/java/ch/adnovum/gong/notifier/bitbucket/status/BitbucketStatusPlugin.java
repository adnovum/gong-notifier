package ch.adnovum.gong.notifier.bitbucket.status;

import java.io.File;

import ch.adnovum.gong.notifier.GongNotifierPluginBase;
import ch.adnovum.gong.notifier.buildstatus.BuildStatusNotificationListener;
import ch.adnovum.gong.notifier.buildstatus.BuildStatusService;
import ch.adnovum.gong.notifier.buildstatus.GitHostSpec;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;

@Extension
public class BitbucketStatusPlugin extends GongNotifierPluginBase {

	private static final Logger LOGGER = Logger.getLoggerFor(BitbucketStatusPlugin.class);
	private static final String PLUGIN_ID = "ch.adnovum.gong.notifier.github.status";

	private Gson gson = new Gson();

	public BitbucketStatusPlugin() {
		super(PLUGIN_ID,
				PluginSettings.class,
				PluginSettings.FIELD_CONFIG,
				GongUtil.readResourceString("/plugin-settings.template.html"));
	}

	@Override
	protected void reinit() {
		PluginSettings settings = (PluginSettings) getSettings();
		LOGGER.info("Re-initializing with settings: " + gson.toJson(settings)
				.replaceAll("\"restPassword\":\"[^ \"]*\"","\"restPassword\":\"***\"")
				.replaceAll("\"bitbucketGlobalAccessToken\":\"[^ \"]*\"","\"bitbucketGlobalAccessToken\":\"***\""));

		GoServerApi api = new GoServerApi(settings.getServerUrl())
						.setAdminCredentials(settings.getRestUser(), settings.getRestPassword());
		ConfigService configService = new ConfigService(api);
		SecretDecryptService decryptService = new SecretDecryptService(new File(settings.getCipherKeyFile()));
		BitbucketClient bbClient = new BitbucketClient(settings.getBitbucketBaseUrl());
		GitHostSpec bbSpec = new BitbucketSpec(settings.getBitbucketClonePattern());

		BuildStatusService statusService = new BuildStatusService(configService, decryptService, bbSpec);

		BuildStatusNotificationListener listener = new BuildStatusNotificationListener(statusService, bbClient,
			settings.getServerDisplayUrl());
		listener.setGlobalAccessToken(settings.getBitbucketGlobalAccessToken());

		addListener(listener);
	}
}
