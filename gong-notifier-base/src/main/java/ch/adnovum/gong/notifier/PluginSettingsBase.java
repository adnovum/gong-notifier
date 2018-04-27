package ch.adnovum.gong.notifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.adnovum.gong.notifier.go.api.SettingsField;

public class PluginSettingsBase {

	private static final String DEFAULT_SERVER_URL = "http://localhost:8153/go";
	private static final String DEFAULT_SERVER_DISPLAY_URL = "https://localhost:8154/go";
	private static final String DEFAULT_REST_USER = null;
	private static final String DEFAULT_REST_PASSWORD = null;
	private static final String DEFAULT_EVENTS = "broken, fixed, failed";
	
	public static final Map<String, SettingsField> BASE_FIELD_CONFIG = new HashMap<>();
	static {
		BASE_FIELD_CONFIG.put("serverUrl", new SettingsField("Server URL", DEFAULT_SERVER_URL, false, false, 0));
		BASE_FIELD_CONFIG.put("serverDisplayUrl", new SettingsField("Server Display URL", DEFAULT_SERVER_DISPLAY_URL, false, false, 0));
		BASE_FIELD_CONFIG.put("restUser", new SettingsField("Rest User", DEFAULT_REST_USER, false, false, 0));
		BASE_FIELD_CONFIG.put("restPassword", new SettingsField("Rest Password", DEFAULT_REST_PASSWORD, false, true, 0));
		BASE_FIELD_CONFIG.put("defaultEvents", new SettingsField("Default notification events", DEFAULT_EVENTS, false, false,
				0));
	}

	private String serverUrl = DEFAULT_SERVER_URL;
	private String serverDisplayUrl = DEFAULT_SERVER_DISPLAY_URL;
	private String restUser = DEFAULT_REST_USER;
	private String restPassword = DEFAULT_REST_PASSWORD;
	private String defaultEvents = DEFAULT_EVENTS;

	public String getServerDisplayUrl() {
		return valueOrDefault(serverDisplayUrl, DEFAULT_SERVER_DISPLAY_URL);
	}

	public void setServerDisplayUrl(String serverDisplayUrl) {
		this.serverDisplayUrl = serverDisplayUrl;
	}

	public String getServerUrl() {
		return valueOrDefault(serverUrl, DEFAULT_SERVER_URL);
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getRestUser() {
		return valueOrDefault(restUser, DEFAULT_REST_USER);
	}

	public void setRestUser(String restUser) {
		this.restUser = restUser;
	}

	public String getRestPassword() {
		return valueOrDefault(restPassword, DEFAULT_REST_PASSWORD);
	}

	public void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}

	public String getDefaultEvents() {
		return valueOrDefault(defaultEvents, DEFAULT_EVENTS);
	}

	public Set<String> getDefaultEventsSet() {
		return new HashSet<>(Arrays.asList(getDefaultEvents().split("\\s*,\\s*")));
	}

	public void setDefaultEvents(String defaultEvents) {
		this.defaultEvents = defaultEvents;
	}

	private static String valueOrDefault(String value, String defaultValue) {
		return value == null || value.isEmpty() ? defaultValue : value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PluginSettingsBase that = (PluginSettingsBase) o;

		if (getServerUrl() != null ? !getServerUrl().equals(that.getServerUrl()) : that.getServerUrl() != null) {
			return false;
		}
		if (getServerDisplayUrl() != null ? !getServerDisplayUrl().equals(that.getServerDisplayUrl())
				: that.getServerDisplayUrl() != null) {
			return false;
		}
		if (getRestUser() != null ? !getRestUser().equals(that.getRestUser()) : that.getRestUser() != null) {
			return false;
		}
		return getRestPassword() != null ? getRestPassword().equals(that.getRestPassword()) : that.getRestPassword() == null;
	}

	@Override
	public int hashCode() {
		int result = getServerUrl() != null ? getServerUrl().hashCode() : 0;
		result = 31 * result + (getServerDisplayUrl() != null ? getServerDisplayUrl().hashCode() : 0);
		result = 31 * result + (getRestUser() != null ? getRestUser().hashCode() : 0);
		result = 31 * result + (getRestPassword() != null ? getRestPassword().hashCode() : 0);
		return result;
	}
}
