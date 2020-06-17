package ch.adnovum.gong.notifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.adnovum.gong.notifier.go.api.SettingsField;

public class PluginSettingsBase {

	private static final String DEFAULT_SERVER_URL = "http://localhost:8153/go";
	private static final String DEFAULT_SERVER_DISPLAY_URL = "https://localhost:8154/go";
	private static final String DEFAULT_REST_USER = null;
	private static final String DEFAULT_REST_PASSWORD = null;
	
	static final Map<String, SettingsField> BASE_FIELD_CONFIG = new HashMap<>();
	static {
		BASE_FIELD_CONFIG.put("serverUrl", new SettingsField("Server URL", DEFAULT_SERVER_URL, false, false, 0));
		BASE_FIELD_CONFIG.put("serverDisplayUrl", new SettingsField("Server Display URL", DEFAULT_SERVER_DISPLAY_URL, false, false, 0));
		BASE_FIELD_CONFIG.put("restUser", new SettingsField("Rest User", DEFAULT_REST_USER, false, false, 0));
		BASE_FIELD_CONFIG.put("restPassword", new SettingsField("Rest Password", DEFAULT_REST_PASSWORD, false, true, 0));
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PluginSettingsBase that = (PluginSettingsBase) o;
		return Objects.equals(getServerUrl(), that.getServerUrl()) &&
				Objects.equals(getServerDisplayUrl(), that.getServerDisplayUrl()) &&
				Objects.equals(getRestUser(), that.getRestUser()) &&
				Objects.equals(getRestPassword(), that.getRestPassword());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getServerUrl(), getServerDisplayUrl(), getRestUser(), getRestPassword());
	}
}
