package ch.adnovum.gong.notifier.email;

import ch.adnovum.gong.notifier.DebugNotificationListener;
import ch.adnovum.gong.notifier.GongNotifierPluginBase;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.HistoryService;
import ch.adnovum.gong.notifier.services.RoutingService;
import ch.adnovum.gong.notifier.util.GongUtil;
import ch.adnovum.gong.notifier.util.ModificationListGenerator;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;

@Extension
public class GongNotifierEmailPlugin extends GongNotifierPluginBase {

	private static final Logger LOGGER = Logger.getLoggerFor(GongNotifierEmailPlugin.class);
	private static final String PLUGIN_ID = "ch.adnovum.gong.notifier.email";

	private Gson gson = new Gson();
	private GoServerApi api;

	public GongNotifierEmailPlugin() {
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

		ModificationListGenerator modListGenerator = new ModificationListGenerator(settings.getTimezone(), true);

		EmailTemplateService templateService = new EmailTemplateService(settings.getSubjectTemplate(),
				settings.getBodyTemplate(), settings.getServerDisplayUrl(), modListGenerator);

		EmailSender sender = new JavaxEmailSender(settings.getSmtpHost(), settings.getSmtpPort());

		HistoryService history = new HistoryService(api);

		RoutingService router = new RoutingService(new ConfigService(api));

		addListener(new DebugNotificationListener());
		addListener(new EmailNotificationListener(history, router, templateService, sender, settings.getSenderEmail(),
				settings.getDefaultEventsSet()));
	}
}
