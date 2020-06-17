package ch.adnovum.gong.notifier.email;

import java.util.*;
import java.util.stream.Collectors;

import ch.adnovum.gong.notifier.PluginSettingsBase;
import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.SettingsField;
import ch.adnovum.gong.notifier.util.GongUtil;

public class PluginSettings extends PluginSettingsBase {

	private static final String DEFAULT_EVENTS = "broken, fixed, failed";
	private static final String DEFAULT_SMTP_HOST = "localhost";
	private static final String DEFAULT_SMTP_PORT = "25";
	private static final String DEFAULT_SENDER_EMAIL = "noreply@localhost.com";
	private static final String DEFAULT_SUBJECT_TEMPLATE = "Stage [{pipeline}/{pipelineCounter}/{stage}/{stageCounter}] {event}";
	private static final String DEFAULT_BODY_TEMPLATE = GongUtil.readResourceString("/default-email-body.template.html");
	private static final String DEFAULT_TIMEZONE = null;
	
	static final Map<String, SettingsField> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("defaultEvents", new SettingsField("Default notification events", DEFAULT_EVENTS, false, false,
				0));
		FIELD_CONFIG.put("smtpHost", new SettingsField("Smtp Host", DEFAULT_SMTP_HOST, false, false, 0));
		FIELD_CONFIG.put("smtpPort", new SettingsField("Smtp Port", DEFAULT_SMTP_PORT, false, false, 0));
		FIELD_CONFIG.put("senderEmail", new SettingsField("Sender E-mail", DEFAULT_SENDER_EMAIL, true, false, 0));
		FIELD_CONFIG.put("subjectTemplate", new SettingsField("E-mail subject template", DEFAULT_SUBJECT_TEMPLATE, false, false, 0));
		FIELD_CONFIG.put("bodyTemplate", new SettingsField("E-mail body template", DEFAULT_BODY_TEMPLATE, false, false, 0));
	}

	private String defaultEvents = DEFAULT_EVENTS;
	private String smtpHost = DEFAULT_SMTP_HOST;
	private String smtpPort = DEFAULT_SMTP_PORT;
	private String senderEmail = DEFAULT_SENDER_EMAIL;
	private String subjectTemplate = DEFAULT_SUBJECT_TEMPLATE;
	private String bodyTemplate = DEFAULT_BODY_TEMPLATE;
	private String timezone = DEFAULT_TIMEZONE;

	public String getDefaultEvents() {
		return valueOrDefault(defaultEvents, DEFAULT_EVENTS);
	}

	public Set<HistoricalEvent> getDefaultEventsSet() {
		return Arrays.stream(getDefaultEvents().split("\\s*,\\s*"))
				.map(String::toUpperCase)
				.map(HistoricalEvent::valueOf)
				.collect(Collectors.toSet());
	}

	public void setDefaultEvents(String defaultEvents) {
		this.defaultEvents = defaultEvents;
	}
	
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		PluginSettings that = (PluginSettings) o;
		return Objects.equals(getDefaultEvents(), that.getDefaultEvents()) &&
				Objects.equals(getSmtpHost(), that.getSmtpHost()) &&
				Objects.equals(getSmtpPort(), that.getSmtpPort()) &&
				Objects.equals(getSenderEmail(), that.getSenderEmail()) &&
				Objects.equals(getSubjectTemplate(), that.getSubjectTemplate()) &&
				Objects.equals(getBodyTemplate(), that.getBodyTemplate()) &&
				Objects.equals(getTimezone(), that.getTimezone());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getDefaultEvents(), getSmtpHost(), getSmtpPort(), getSenderEmail(), getSubjectTemplate(), getBodyTemplate(), getTimezone());
	}
}
