package ch.adnovum.gong.notifier.email;

import static ch.adnovum.gong.notifier.util.GongUtil.escapeHtml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ch.adnovum.gong.notifier.*;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.HistoryService;
import ch.adnovum.gong.notifier.util.ModificationListGenerator;
import ch.adnovum.gong.notifier.services.RoutingService;
import ch.adnovum.gong.notifier.util.TemplateHelper;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class EmailNotificationListener implements NotificationListener {

	private static final String EMAIL_ENV_VARIABLE = "GONG_EMAIL";
	private static final String TARGET_SUFFIX = "_ADDRESS";
	private static final String EVENTS_SUFFIX = "_EVENTS";

	private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationListener.class);

	private final HistoryService history;
	private final RoutingService router;
	private final RoutingService.RouteConfigParams routeParams;

	private EmailSender emailSender;
	private String senderEmail;
	private String subjectTemplate;
	private String bodyTemplate;
	private String serverDisplayUrl;
	private ModificationListGenerator modListGenerator;

	public EmailNotificationListener(HistoryService history, RoutingService router, EmailSender emailSender, String senderEmail,
									 String subjectTemplate, String bodyTemplate, String serverDisplayUrl, Set<HistoricalEvent> defaultEvents,
									 ModificationListGenerator modListGenerator) {
		this.history = history;
		this.router = router;
		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.serverDisplayUrl = serverDisplayUrl;
		this.modListGenerator = modListGenerator;
		this.routeParams = new RoutingService.RouteConfigParams(EMAIL_ENV_VARIABLE,
				TARGET_SUFFIX, EVENTS_SUFFIX, defaultEvents);
	}

	public EmailNotificationListener(HistoryService history, RoutingService router, EmailSender emailSender,
									 PluginSettings settings) {
		this(history, router, emailSender, settings.getSenderEmail(), settings.getSubjectTemplate(),
				settings.getBodyTemplate(),	settings.getServerDisplayUrl(), settings.getDefaultEventsSet(),
				new ModificationListGenerator(settings.getTimezone(), true));
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		HistoricalEvent histEvent = history.determineHistoricalEvent(event, stateChange);
		Collection<String> targets = router.computeTargets(histEvent, stateChange, routeParams);
		notifyTargets(stateChange, histEvent, targets);
	}

	protected void notifyTargets(StageStateChange stateChange, HistoricalEvent histEvent, Collection<String> targets) {
		LOGGER.debug("Email for " + stateChange.getPipelineName() + ": " + String.join(",", targets));
		Map<String, Object> templateVals = new HashMap<>();
		templateVals.put("pipeline", escapeHtml(stateChange.getPipelineName()));
		templateVals.put("stage", escapeHtml(stateChange.getStageName()));
		templateVals.put("pipelineCounter", stateChange.getPipelineCounter());
		templateVals.put("stageCounter", stateChange.getStageCounter());
		templateVals.put("event", escapeHtml(histEvent.getVerbString()));
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
		return history.fetchPipelineHistory(stateChange.getPipelineName(), stateChange.getPipelineCounter())
					.flatMap(h -> h.getCurrentBuildCause(stateChange.getPipelineCounter()))
					.map(b -> b.materialRevisions)
					.map(modListGenerator::generateModificationList);
	}
}
