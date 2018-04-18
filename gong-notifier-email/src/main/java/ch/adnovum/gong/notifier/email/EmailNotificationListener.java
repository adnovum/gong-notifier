package ch.adnovum.gong.notifier.email;

import static ch.adnovum.gong.notifier.util.GongUtil.escapeHtml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ch.adnovum.gong.notifier.ConfigurableNotificationListener;
import ch.adnovum.gong.notifier.PipelineInfoProvider;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.ModificationListGenerator;
import ch.adnovum.gong.notifier.util.TemplateHelper;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class EmailNotificationListener extends ConfigurableNotificationListener {

	private static final String EMAIL_ENV_VARIABLE = "GONG_EMAIL_ADDRESS";
	private static final String EVENTS_SUFFIX = "_EVENTS";

	private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationListener.class);

	private EmailSender emailSender;
	private String senderEmail;
	private String subjectTemplate;
	private String bodyTemplate;
	private String serverDisplayUrl;
	private ModificationListGenerator modListGenerator;

	public EmailNotificationListener(PipelineInfoProvider pipelineInfo, EmailSender emailSender, String senderEmail,
			String subjectTemplate, String bodyTemplate, String serverDisplayUrl, ModificationListGenerator modListGenerator) {
		super(pipelineInfo, EMAIL_ENV_VARIABLE, EVENTS_SUFFIX);

		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.serverDisplayUrl = serverDisplayUrl;
		this.modListGenerator = modListGenerator;
	}

	public EmailNotificationListener(PipelineInfoProvider pipelineInfo, EmailSender emailSender,
			PluginSettings settings) {
		this(pipelineInfo, emailSender, settings.getSenderEmail(), settings.getSubjectTemplate(), settings.getBodyTemplate(),
				settings.getServerDisplayUrl(), new ModificationListGenerator(settings.getTimezone(), true));
	}

	@Override
	protected void notifyTargets(StageStateChange stateChange, Event event, Collection<String> targets) {
		LOGGER.debug("Email for " + stateChange.getPipelineName() + ": " + String.join(",", targets));
		Map<String, Object> templateVals = new HashMap<>();
		templateVals.put("pipeline", escapeHtml(stateChange.getPipelineName()));
		templateVals.put("stage", escapeHtml(stateChange.getStageName()));
		templateVals.put("pipelineCounter", stateChange.getPipelineCounter());
		templateVals.put("stageCounter", stateChange.getStageCounter());
		templateVals.put("event", escapeHtml(event.getVerbString()));
		templateVals.put("serverUrl", escapeHtml(serverDisplayUrl));
		templateVals.put("modificationList", generateModificationList(stateChange).orElse(""));

		String subject = TemplateHelper.fillTemplate(subjectTemplate, templateVals);
		String body = TemplateHelper.fillTemplate(bodyTemplate, templateVals);
		try {
			emailSender.sendMail(senderEmail, targets, subject, body);
		} catch (Exception e) {
			LOGGER.error("Error sending email to " + String.join(",", targets)+ ": " + e.getMessage(), e);
		}
	}

	private Optional<String> generateModificationList(StageStateChange stateChange) {
		return pipelineInfo.getPipelineHistory(stateChange.getPipelineName(), stateChange.getPipelineCounter())
					.flatMap(h -> h.getCurrentBuildCause(stateChange.getPipelineCounter()))
					.map(b -> b.materialRevisions)
					.map(modListGenerator::generateModificationList);
	}
}
