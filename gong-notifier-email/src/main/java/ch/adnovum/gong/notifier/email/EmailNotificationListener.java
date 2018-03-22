package ch.adnovum.gong.notifier.email;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import ch.adnovum.gong.notifier.ConfigurableNotificationListener;
import ch.adnovum.gong.notifier.PipelineInfoProvider;
import ch.adnovum.gong.notifier.util.TemplateHelper;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
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
	// TODO: extract the modification list generator and then pass timezone to its constructor instead.
	private String timezone;

	public EmailNotificationListener(PipelineInfoProvider pipelineInfo, EmailSender emailSender, String senderEmail,
			String subjectTemplate, String bodyTemplate, String serverDisplayUrl, String timezone) {
		super(pipelineInfo, EMAIL_ENV_VARIABLE, EVENTS_SUFFIX);

		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.serverDisplayUrl = serverDisplayUrl;
		this.timezone = timezone;
	}

	@Override
	protected void notifyTargets(StageStateChange stateChange, Event event, List<String> targets) {
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
		final SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (timezone != null) {
			dtFmt.setTimeZone(TimeZone.getTimeZone(timezone));
		}

		final String nl = "\n<br/>";

		Optional<PipelineHistory.BuildCause> res =
				pipelineInfo.getPipelineHistory(stateChange.getPipelineName())
					.flatMap(h -> h.getCurrentBuildCause(stateChange.getPipelineCounter()));
		if (!res.isPresent()) {
			return Optional.empty();
		}

		StringBuilder sb = new StringBuilder();
		PipelineHistory.BuildCause buildCause = res.get();
		for (PipelineHistory.MaterialRevision matRev: buildCause.materialRevisions) {
			for (PipelineHistory.Modification mod: matRev.modifications) {
				sb
						.append(matRev.material.type + ": "  + escapeHtml(matRev.material.description)).append(nl)
						.append("revision: " + escapeHtml(mod.revision)).append(", modified by " + escapeHtml(mod.userName))
						.append(" on " + dtFmt.format(mod.getModifiedTime())).append(nl)
						.append(escapeHtml(mod.comment).replaceAll("\n", nl))
						.append(nl).append(nl);
			}
		}
		return Optional.of(sb.toString());
	}

	private static String escapeHtml(String str) {
		return str == null ? "" : str
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
