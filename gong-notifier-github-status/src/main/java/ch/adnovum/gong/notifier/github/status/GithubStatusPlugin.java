package ch.adnovum.gong.notifier.github.status;

import java.io.File;

import ch.adnovum.gong.notifier.GongNotifierPluginBase;
import ch.adnovum.gong.notifier.buildstatus.GitHostSpec;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.buildstatus.BuildStatusNotificationListener;
import ch.adnovum.gong.notifier.buildstatus.BuildStatusService;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;

@Extension
public class GithubStatusPlugin extends GongNotifierPluginBase {

	private static final Logger LOGGER = Logger.getLoggerFor(GithubStatusPlugin.class);
	private static final String PLUGIN_ID = "ch.adnovum.gong.notifier.github.status";

	private Gson gson = new Gson();

	public GithubStatusPlugin() {
		super(PLUGIN_ID,
				PluginSettings.class,
				PluginSettings.FIELD_CONFIG,
				GongUtil.readResourceString("/plugin-settings.template.html"));
	}

	@Override
	protected void reinit() {
		PluginSettings settings = (PluginSettings) getSettings();
		LOGGER.info("Re-initializing with settings: " + gson.toJson(settings).
				replaceAll("\"restPassword\":\"[^ \"]*\"","\"restPassword\":\"***\""));

		GoServerApi api = new GoServerApi(settings.getServerUrl())
						.setAdminCredentials(settings.getRestUser(), settings.getRestPassword());
		ConfigService configService = new ConfigService(api);
		SecretDecryptService decryptService = new SecretDecryptService(new File(settings.getCipherKeyFile()));
		GithubClient ghClient = new GithubClient();
		GitHostSpec ghSpec = new GithubSpec();

		BuildStatusService statusService = new BuildStatusService(configService, decryptService, ghSpec);

		addListener(new BuildStatusNotificationListener(statusService, ghClient, settings.getServerDisplayUrl()));
	}
}
