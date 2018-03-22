package ch.adnovum.gong.notifier.email;

import java.util.HashMap;
import java.util.Map;

import ch.adnovum.gong.notifier.PluginSettingsBase;
import ch.adnovum.gong.notifier.go.api.SettingsField;
import ch.adnovum.gong.notifier.util.GongUtil;

public class PluginSettings extends PluginSettingsBase {
	
	private static final String DEFAULT_SMTP_HOST = "localhost";
	private static final String DEFAULT_SMTP_PORT = "25";
	private static final String DEFAULT_SENDER_EMAIL = "noreply@localhost.com";
	private static final String DEFAULT_SUBJECT_TEMPLATE = "Stage [{pipeline}/{pipelineCounter}/{stage}/{stageCounter}] {event}";
	private static final String DEFAULT_BODY_TEMPLATE = GongUtil.readResourceString("/default-email-body.template.html");
	private static final String DEFAULT_TIMEZONE = null;
	
	public static final Map<String, SettingsField> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("smtpHost", new SettingsField("Smtp Host", DEFAULT_SMTP_HOST, false, false, 0));
		FIELD_CONFIG.put("smtpPort", new SettingsField("Smtp Port", DEFAULT_SMTP_PORT, false, false, 0));
		FIELD_CONFIG.put("senderEmail", new SettingsField("Sender E-mail", DEFAULT_SENDER_EMAIL, true, false, 0));
		FIELD_CONFIG.put("subjectTemplate", new SettingsField("E-mail subject template", DEFAULT_SUBJECT_TEMPLATE, false, false, 0));
		FIELD_CONFIG.put("bodyTemplate", new SettingsField("E-mail body template", DEFAULT_BODY_TEMPLATE, false, false, 0));
	}

	private String smtpHost = DEFAULT_SMTP_HOST;
	private String smtpPort = DEFAULT_SMTP_PORT;
	private String senderEmail = DEFAULT_SENDER_EMAIL;
	private String subjectTemplate = DEFAULT_SUBJECT_TEMPLATE;
	private String bodyTemplate = DEFAULT_BODY_TEMPLATE;
	private String timezone = DEFAULT_TIMEZONE;
	
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

	String getTimezone() {
		return valueOrDefault(timezone, DEFAULT_TIMEZONE);
	}

	void setTimezone(String timezone) {
		this.timezone = timezone;
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
		if (!super.equals(o)) {
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
		if (getSubjectTemplate() != null ? !getSubjectTemplate().equals(that.getSubjectTemplate())
				: that.getSubjectTemplate() != null) {
			return false;
		}
		return getBodyTemplate() != null ? getBodyTemplate().equals(that.getBodyTemplate()) : that.getBodyTemplate() == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getSmtpHost() != null ? getSmtpHost().hashCode() : 0);
		result = 31 * result + (getSmtpPort() != null ? getSmtpPort().hashCode() : 0);
		result = 31 * result + (getSenderEmail() != null ? getSenderEmail().hashCode() : 0);
		result = 31 * result + (getSubjectTemplate() != null ? getSubjectTemplate().hashCode() : 0);
		result = 31 * result + (getBodyTemplate() != null ? getBodyTemplate().hashCode() : 0);
		return result;
	}
}
