package ch.adnovum.gong.notifier;

import java.util.HashMap;
import java.util.Map;

import ch.adnovum.gong.notifier.go.api.SettingsField;

public class PluginSettingsBase {

	private static final String DEFAULT_SERVER_URL = "http://localhost:8153/go";
	private static final String DEFAULT_SERVER_DISPLAY_URL = "https://localhost:8154/go";
	private static final String DEFAULT_REST_USER = null;
	private static final String DEFAULT_REST_PASSWORD = null;
	
	public static final Map<String, SettingsField> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("serverUrl", new SettingsField("Server URL", DEFAULT_SERVER_URL, false, false, 0));
		FIELD_CONFIG.put("serverDisplayUrl", new SettingsField("Server Display URL", DEFAULT_SERVER_DISPLAY_URL, false, false, 0));
		FIELD_CONFIG.put("restUser", new SettingsField("Rest User", DEFAULT_REST_USER, false, false, 0));
		FIELD_CONFIG.put("restPassword", new SettingsField("Rest Password", DEFAULT_REST_PASSWORD, false, true, 0));
	}

	private String serverUrl = DEFAULT_SERVER_URL;
	private String serverDisplayUrl = DEFAULT_SERVER_DISPLAY_URL;
	private String restUser = DEFAULT_REST_USER;
	private String restPassword = DEFAULT_REST_PASSWORD;

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
