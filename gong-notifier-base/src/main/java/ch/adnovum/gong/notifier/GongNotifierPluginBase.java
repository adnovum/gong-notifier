package ch.adnovum.gong.notifier;

import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_BUILDING;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_CANCELLED;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_FAILED;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_PASSED;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.SettingsField;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public abstract class GongNotifierPluginBase implements GoPlugin {
	private static final Logger LOGGER = Logger.getLoggerFor(GongNotifierPluginBase.class);

	private static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
	private static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
	private static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
	private static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
	private static final String REQUEST_STAGE_STATUS = "stage-status";
	private static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";

	private static final String EXTENSION_NAME = "notification";
	private static final List<String> goSupportedVersions = Collections.singletonList("1.0");

	private String pluginId;
	private Class<? extends PluginSettingsBase> settingsClass;
	private Map<String, SettingsField> settingsFields;
	private String settingsTemplate;

	private PluginSettingsBase settings;
	private GoApplicationAccessor goApplicationAccessor;
	private Gson gson = new Gson();
	private List<NotificationListener> listeners = new LinkedList<>();


	protected GongNotifierPluginBase(String pluginId, Class<? extends PluginSettingsBase> settingsClass, Map<String, SettingsField> settingsFields, String
			settingsTemplate) {
		this.pluginId = pluginId;
		this.settingsClass = settingsClass;
		this.settingsFields = new HashMap<>();
		this.settingsFields.putAll(PluginSettingsBase.BASE_FIELD_CONFIG);
		this.settingsFields.putAll(settingsFields);
		this.settingsTemplate = GongUtil.readResourceString("/plugin-settings-base.template.html") + settingsTemplate;
	}

	protected abstract void reinit();

	protected void addListener(NotificationListener listener) {
		listeners.add(listener);
	}

	protected PluginSettingsBase getSettings() {
		return settings;
	}

	@Override
	public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
		this.goApplicationAccessor = goApplicationAccessor;
	}

	@Override
	public GoPluginIdentifier pluginIdentifier() {
		return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
	}

	@Override
	public GoPluginApiResponse handle(GoPluginApiRequest request) {
		String requestName = request.requestName();
		switch (requestName) {
			case PLUGIN_SETTINGS_GET_CONFIGURATION:
				return handleGetPluginSettingsConfiguration();
			case PLUGIN_SETTINGS_GET_VIEW:
				return handleGetPluginSettingsView();
			case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
				return handleValidatePluginSettingsConfiguration();
			case REQUEST_NOTIFICATIONS_INTERESTED_IN:
				return handleNotificationsInterestedIn();
			case REQUEST_STAGE_STATUS:
				return handleStageStatus(request);
			default:
				return error("Unknown request: " + requestName);
		}
	}

	private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
		return ok(settingsFields);
	}

	private GoPluginApiResponse handleGetPluginSettingsView() {
		Map<String, Object> response = new HashMap<>();
		response.put("template", settingsTemplate);
		return ok(response);
	}

	private GoPluginApiResponse handleValidatePluginSettingsConfiguration() {
		List<ValidationError> errors = new LinkedList<>();
		return ok(errors);
	}

	private GoPluginApiResponse handleNotificationsInterestedIn() {
		Map<String, Object> response = new HashMap<>();
		response.put("notifications", Collections.singletonList(REQUEST_STAGE_STATUS));
		return ok(response);
	}

	private PluginSettingsBase fetchPluginSettings() {
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("plugin-id", pluginId);
		GoApiResponse response = goApplicationAccessor.submit(request(GET_PLUGIN_SETTINGS, requestMap));
		if (response.responseBody() == null || response.responseBody().trim().isEmpty()) {
			LOGGER.info("Plugin not configured. Using defaults.");

			try {
				return settingsClass.getDeclaredConstructor().newInstance();
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LOGGER.error("Could not create default plugins settings", e);
				return null;
			}

		}
		return new Gson().fromJson(response.responseBody(), settingsClass);
	}

	private GoPluginApiResponse handleStageStatus(GoPluginApiRequest request) {
		LOGGER.debug("handleStageStatus: " + request.requestBody());
		PluginSettingsBase currentSettings = fetchPluginSettings();
		if (settings == null || !settings.equals(currentSettings)) {
			settings = currentSettings;
			listeners.clear();
			reinit();
		}

		final StageStateChange stateChange = gson.fromJson(request.requestBody(), StageStateChange.class);

		String state = stateChange.getState();
		final BaseEvent event = mapStateToEvent(state);
		if (event == null) {
			LOGGER.warn("Unknown state " + state + ". Ignoring it.");
		}
		else {
			listeners.forEach(l -> l.handle(event, stateChange));
		}

		return successResponse();
	}

	private BaseEvent mapStateToEvent(String state) {
		switch (state) {
			case STATUS_BUILDING:
				return BaseEvent.BUILDING;
			case STATUS_PASSED:
				return BaseEvent.PASSED;
			case STATUS_FAILED:
				return BaseEvent.FAILED;
			case STATUS_CANCELLED:
				return BaseEvent.CANCELLED;
			default:
				return null;
		}
	}

	private GoPluginApiResponse ok(Object response) {
		return jsonResponse(200, response);
	}

	private GoPluginApiResponse error(Object response) {
		return jsonResponse(500, response);
	}

	private GoPluginApiResponse jsonResponse(int responseCode, Object response) {
		return new DefaultGoPluginApiResponse(responseCode, new Gson().toJson(response));
	}

	private GoPluginApiResponse successResponse() {
		Map<String, String> response = new HashMap<>();
		response.put("status", "success");
		return ok(response);
	}

	private GoApiRequest request(String api, Object request) {
		DefaultGoApiRequest req = new DefaultGoApiRequest(api, "1.0", pluginIdentifier());
		req.setRequestBody(new Gson().toJson(request));
		return req;
	}

	private class ValidationError {
		String key;
		String message;
		public ValidationError(String key, String message) {
			this.key = key;
			this.message = message;
		}
	}
}
