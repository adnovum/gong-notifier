package ch.adnovum.gong.notifier;

import java.util.HashMap;
import java.util.Map;

import ch.adnovum.gong.notifier.util.GongUtil;

public class PluginSettings {
	
	private static final String DEFAULT_SMTP_HOST = "localhost";
	private static final String DEFAULT_SMTP_PORT = "25";
	private static final String DEFAULT_SENDER_EMAIL = "gocd@noreply.com";
	private static final String DEFAULT_SERVER_URL = "http://localhost:8153/go";
	private static final String DEFAULT_SERVER_DISPLAY_URL = "https://localhost:8154/go";
	private static final String DEFAULT_REST_USER = null;
	private static final String DEFAULT_REST_PASSWORD = null;
	private static final String DEFAULT_SUBJECT_TEMPLATE = "Stage [{pipeline}/{pipelineCounter}/{stage}/{stageCounter}] "
			+ "{transition}";
	private static final String DEFAULT_BODY_TEMPLATE = GongUtil.readResourceString("/default-email-body.template.html");
	
	public static final Map<String, Field> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("smtpHost", new Field("Smtp Host", DEFAULT_SMTP_HOST, false, false, 0));
		FIELD_CONFIG.put("smtpPort", new Field("Smtp Port", DEFAULT_SMTP_PORT, false, false, 0));
		FIELD_CONFIG.put("senderEmail", new Field("Sender E-mail", DEFAULT_SENDER_EMAIL, true, false, 0));
		FIELD_CONFIG.put("serverUrl", new Field("Server URL", DEFAULT_SERVER_URL, false, false, 0));
		FIELD_CONFIG.put("serverDisplayUrl", new Field("Server Display URL", DEFAULT_SERVER_DISPLAY_URL, false, false, 0));
		FIELD_CONFIG.put("subjectTemplate", new Field("E-mail subject template", DEFAULT_SUBJECT_TEMPLATE, false, false, 0));
		FIELD_CONFIG.put("bodyTemplate", new Field("E-mail body template", DEFAULT_BODY_TEMPLATE, false, false, 0));
		FIELD_CONFIG.put("restUser", new Field("Rest User", DEFAULT_REST_USER, false, false, 0));
		FIELD_CONFIG.put("restPassword", new Field("Rest Password", DEFAULT_REST_PASSWORD, false, true, 0));
	}

	private String smtpHost = DEFAULT_SMTP_HOST;
	private String smtpPort = DEFAULT_SMTP_PORT;
	private String senderEmail = DEFAULT_SENDER_EMAIL;
	private String serverUrl = DEFAULT_SERVER_URL;
	private String serverDisplayUrl = DEFAULT_SERVER_DISPLAY_URL;
	private String restUser = DEFAULT_REST_USER;
	private String restPassword = DEFAULT_REST_PASSWORD;
	private String subjectTemplate = DEFAULT_SUBJECT_TEMPLATE;
	private String bodyTemplate = DEFAULT_BODY_TEMPLATE;
	
	public String getSmtpHost() {
		return valueOrDefault(smtpHost, DEFAULT_SMTP_HOST);
	}
	
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	
	public Integer getSmtpPort() {
		return Integer.parseInt(valueOrDefault(smtpPort, DEFAULT_SMTP_PORT));
	}
	
	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = Integer.toString(smtpPort);
	}
	
	public String getSenderEmail() {
		return valueOrDefault(senderEmail, DEFAULT_SENDER_EMAIL);
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail; 
	}

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

	public String getSubjectTemplate() {
		return valueOrDefault(subjectTemplate, DEFAULT_SUBJECT_TEMPLATE);
	}

	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}

	public String getBodyTemplate() {
		return valueOrDefault(bodyTemplate, DEFAULT_BODY_TEMPLATE);
	}

	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
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

		PluginSettings that = (PluginSettings) o;

		if (getSmtpHost() != null ? !getSmtpHost().equals(that.getSmtpHost()) : that.getSmtpHost() != null) {
			return false;
		}
		if (getSmtpPort() != null ? !getSmtpPort().equals(that.getSmtpPort()) : that.getSmtpPort() != null) {
			return false;
		}
		if (getSenderEmail() != null ? !getSenderEmail().equals(that.getSenderEmail()) : that.getSenderEmail() != null) {
			return false;
		}
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
		if (getRestPassword() != null ? !getRestPassword().equals(that.getRestPassword()) : that.getRestPassword() != null) {
			return false;
		}
		if (getSubjectTemplate() != null ? !getSubjectTemplate().equals(that.getSubjectTemplate())
				: that.getSubjectTemplate() != null) {
			return false;
		}
		return getBodyTemplate() != null ? getBodyTemplate().equals(that.getBodyTemplate()) : that.getBodyTemplate() == null;
	}

	@Override
	public int hashCode() {
		int result = getSmtpHost() != null ? getSmtpHost().hashCode() : 0;
		result = 31 * result + (getSmtpPort() != null ? getSmtpPort().hashCode() : 0);
		result = 31 * result + (getSenderEmail() != null ? getSenderEmail().hashCode() : 0);
		result = 31 * result + (getServerUrl() != null ? getServerUrl().hashCode() : 0);
		result = 31 * result + (getServerDisplayUrl() != null ? getServerDisplayUrl().hashCode() : 0);
		result = 31 * result + (getRestUser() != null ? getRestUser().hashCode() : 0);
		result = 31 * result + (getRestPassword() != null ? getRestPassword().hashCode() : 0);
		result = 31 * result + (getSubjectTemplate() != null ? getSubjectTemplate().hashCode() : 0);
		result = 31 * result + (getBodyTemplate() != null ? getBodyTemplate().hashCode() : 0);
		return result;
	}

	public static class Field {
		public String displayName;
		public String defaultValue;
		public boolean isRequired;
		public boolean isSecure;
		public int displayOrder;
		public Field(String displayName, String defaultValue, boolean isRequired, boolean isSecure,
				int displayOrder) {
			this.displayName = displayName;
			this.defaultValue = defaultValue;
			this.isRequired = isRequired;
			this.isSecure = isSecure;
			this.displayOrder = displayOrder;
		}
	}

}
