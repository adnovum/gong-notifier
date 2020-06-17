package ch.adnovum.gong.notifier.email;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.HistoryService;
import ch.adnovum.gong.notifier.services.RoutingService;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class EmailNotificationListener implements NotificationListener {

	private static final String EMAIL_ENV_VARIABLE = "GONG_EMAIL";
	private static final String TARGET_SUFFIX = "_ADDRESS";
	private static final String EVENTS_SUFFIX = "_EVENTS";

	private static final Logger LOGGER = Logger.getLoggerFor(EmailNotificationListener.class);

	private final HistoryService history;
	private final RoutingService router;
	private final RoutingService.RouteConfigParams routeParams;
	private final EmailTemplateService templateService;
	private EmailSender emailSender;
	private String senderEmail;

	public EmailNotificationListener(HistoryService history, RoutingService router, EmailTemplateService templateService,
			EmailSender emailSender, String senderEmail, Set<HistoricalEvent> defaultEvents) {
		this.history = history;
		this.router = router;
		this.templateService = templateService;
		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.routeParams = new RoutingService.RouteConfigParams(EMAIL_ENV_VARIABLE,
				TARGET_SUFFIX, EVENTS_SUFFIX, defaultEvents);
	}

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		HistoricalEvent histEvent = history.determineHistoricalEvent(event, stateChange);
		Collection<String> targets = router.computeTargets(histEvent, stateChange, routeParams);
		notifyTargets(stateChange, histEvent, targets);
	}

	private void notifyTargets(StageStateChange stateChange, HistoricalEvent histEvent, Collection<String> targets) {
		LOGGER.debug("Email for " + stateChange.getPipelineName() + ": " + String.join(",", targets));

		PipelineHistory.BuildCause cause = fetchBuildCause(stateChange).orElse(null);
		EmailTemplateService.InstantiatedEmail mail = templateService.instantiateEmail(stateChange, histEvent, cause);

		try {
			emailSender.sendMail(senderEmail, targets, mail.getSubject(), mail.getBody());
		} catch (Exception e) {
			LOGGER.error("Error sending email to " + String.join(",", targets)+ ": " + e.getMessage(), e);
		}
	}

	private Optional<PipelineHistory.BuildCause> fetchBuildCause(StageStateChange stateChange) {
		return history.fetchPipelineHistory(stateChange.getPipelineName(), stateChange.getPipelineCounter())
					.flatMap(h -> h.getCurrentBuildCause(stateChange.getPipelineCounter()));
	}
}
