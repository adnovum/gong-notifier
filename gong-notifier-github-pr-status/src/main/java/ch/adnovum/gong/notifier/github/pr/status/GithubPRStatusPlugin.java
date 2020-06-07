package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.GongNotifierPluginBase;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.File;

@Extension
public class GithubPRStatusPlugin extends GongNotifierPluginBase {

	private static Logger LOGGER = Logger.getLoggerFor(GithubPRStatusPlugin.class);
	private static final String PLUGIN_ID = "ch.adnovum.gong.notifier.email";

	private Gson gson = new Gson();
	private GoServerApi api;

	public GithubPRStatusPlugin() {
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

		api = new GoServerApi(settings.getServerUrl())
						.setAdminCredentials(settings.getRestUser(), settings.getRestPassword());
		ConfigService srv = new ConfigService(api);
		SecretDecryptService decSrv = new SecretDecryptService(new File(settings.getCipherKeyFile()));
		GithubClient ghClient = new GithubClient();

		addListener(new GithubPRStatusNotificationListener(srv, decSrv, ghClient, settings.getServerDisplayUrl()));
	}
}
