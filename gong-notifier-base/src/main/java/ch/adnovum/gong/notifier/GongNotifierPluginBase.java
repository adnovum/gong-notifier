package ch.adnovum.gong.notifier;

import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_BUILDING;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_CANCELLED;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_FAILED;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_PASSED;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import ch.adnovum.gong.notifier.go.api.SettingsField;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public abstract class GongNotifierPluginBase implements GoPlugin {
	private static Logger LOGGER = Logger.getLoggerFor(GongNotifierPluginBase.class);

	private static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
	private static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
	private static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
	private static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
	private static final String REQUEST_STAGE_STATUS = "stage-status";
	private static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";

	private static final String EXTENSION_NAME = "notification";
	private static final List<String> goSupportedVersions = asList("1.0");

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

	protected abstract PipelineInfoProvider getPipelineInfoProvider();

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
	public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
		String requestName = request.requestName();
		switch (requestName) {
			case PLUGIN_SETTINGS_GET_CONFIGURATION:
				return handleGetPluginSettingsConfiguration();
			case PLUGIN_SETTINGS_GET_VIEW:
				return handleGetPluginSettingsView();
			case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
				return handleValidatePluginSettingsConfiguration(request);
			case REQUEST_NOTIFICATIONS_INTERESTED_IN:
				return handleNotificationsInterestedIn();
			case REQUEST_STAGE_STATUS:
				return handleStageStatus(request);
		}

		return error("Unknown request");
	}

	private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
		return ok(settingsFields);
	}

	private GoPluginApiResponse handleGetPluginSettingsView() {
		Map<String, Object> response = new HashMap<>();
		response.put("template", settingsTemplate);
		return ok(response);
	}

	private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
		List<ValidationError> errors = new LinkedList<>();
		return ok(errors);
	}

	private GoPluginApiResponse handleNotificationsInterestedIn() {
		Map<String, Object> response = new HashMap<>();
		response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
		return ok(response);
	}

	private PluginSettingsBase fetchPluginSettings() {
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("plugin-id", pluginId);
		GoApiResponse response = goApplicationAccessor.submit(request(GET_PLUGIN_SETTINGS, requestMap));
		if (response.responseBody() == null || response.responseBody().trim().isEmpty()) {
			LOGGER.info("Plugin not configured. Using defaults.");
			try {
				return settingsClass.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e) {
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
		String newState = stateChange.getState();
		String oldState = getPipelineInfoProvider().getPipelineHistory(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.flatMap(h -> h.getPreviousStageResult(stateChange.getStageName(), stateChange.getPipelineCounter()))
				.orElse(null);
		if (oldState == null) {
			LOGGER.warn("Could not get previous state of " + stateChange.getPipelineName() + "/" + stateChange.getPipelineCounter()
				+ "/" + stateChange.getStageName());
		}

		final BiConsumer<NotificationListener, StageStateChange> fn;
		switch (newState) {
			case STATUS_BUILDING:
				fn = NotificationListener::handleBuilding;
				break;
			case STATUS_PASSED:
				fn = STATUS_FAILED.equals(oldState)
						? NotificationListener::handleFixed
						: NotificationListener::handlePassed;
				break;
			case STATUS_FAILED:
				fn = STATUS_PASSED.equals(oldState)
						? NotificationListener::handleBroken
						: NotificationListener::handleFailed;
				break;
			case STATUS_CANCELLED:
				fn = NotificationListener::handleCancelled;
				break;
			default:
				LOGGER.warn("Unknown state " + newState + ". Ignoring it.");
				fn = null;
		}

		if (fn != null) {
			listeners.forEach(l -> fn.accept(l, stateChange));
		}

		return successResponse();
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

	private GoPluginApiResponse failureResponse(String... messages) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "failure");
		response.put("messages", Arrays.asList(messages));
		return error(response);
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
