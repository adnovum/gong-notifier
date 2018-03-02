package ch.adnovum.gong.notifier.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.adnovum.gong.notifier.ConfigurableNotificationListener;
import ch.adnovum.gong.notifier.TemplateHelper;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class EmailNotificationListener extends ConfigurableNotificationListener {

	private static final String EMAIL_ENV_VARIABLE = "GONG_EMAIL_ADDRESS";
	private static final String STATES_SUFFIX = "_STATES";

	private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationListener.class);

	private EmailSender emailSender;
	private String senderEmail;
	private String subjectTemplate;
	private String bodyTemplate;
	private String serverUrl;

	public EmailNotificationListener(PipelineInfoProvider pipelineInfo, EmailSender emailSender, String senderEmail,
			String subjectTemplate, String bodyTemplate, String serverUrl) {
		super(pipelineInfo, EMAIL_ENV_VARIABLE, STATES_SUFFIX);

		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.serverUrl = serverUrl;
	}

	@Override
	protected void notifyTargets(StageStateChange stateChange, String state, List<String> targets) {
		LOGGER.debug("Email for " + stateChange.getPipelineName() + ": " + String.join(",", targets));
		Map<String, Object> templateVals = new HashMap<>();
		templateVals.put("pipeline", stateChange.getPipelineName());
		templateVals.put("stage", stateChange.getStageName());
		templateVals.put("pipelineCounter", stateChange.getPipelineCounter());
		templateVals.put("stageCounter", stateChange.getStageCounter());
		templateVals.put("state", state);
		templateVals.put("serverUrl", serverUrl);

		String subject = TemplateHelper.fillTemplate(subjectTemplate, templateVals);
		String body = TemplateHelper.fillTemplate(bodyTemplate, templateVals);;
		try {
			emailSender.sendMail(senderEmail, targets, subject, body);
		} catch (Exception e) {
			LOGGER.error("Error sending email to " + String.join(",", targets)+ ": " + e.getMessage(), e);
		}
	}
}
