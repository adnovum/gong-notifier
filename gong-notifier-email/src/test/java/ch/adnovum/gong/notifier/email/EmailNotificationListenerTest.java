package ch.adnovum.gong.notifier.email;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.HistoryService;
import ch.adnovum.gong.notifier.services.RoutingService;
import ch.adnovum.gong.notifier.util.ModificationListGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationListenerTest {

	private static final Set<HistoricalEvent> DEFAULT_EVENTS = new HashSet<>(Arrays.asList(
			HistoricalEvent.BROKEN, HistoricalEvent.FIXED, HistoricalEvent.FAILED
	));

	@Mock
	private HistoryService historyService;

	@Mock
	private ConfigService configService;

	@Mock
	private EmailSender emailSender;

	private EmailNotificationListener listener;

	@Before
	public void setup() {
		RoutingService routingService = new RoutingService(configService);
		EmailTemplateService templateService = new EmailTemplateService("", "", "",
				new ModificationListGenerator(null, true));
		listener = new EmailNotificationListener(historyService, routingService, templateService,
				emailSender, "noreply@example.com", DEFAULT_EVENTS);

		when(historyService.determineHistoricalEvent(eq(BaseEvent.PASSED), any())).thenReturn(HistoricalEvent.PASSED);
	}

	@Test
	public void shouldGracefullyHandleConfigFetchFailure() throws EmailSender.EmailSenderException {
		// Given
		StageStateChange change = new StageStateChange();
		change.pipeline = new StageStateChange.Pipeline();
		change.pipeline.name = "pipeline1";
		change.pipeline.counter = 7;

		when(configService.fetchPipelineConfig(anyString(), anyInt())).thenReturn(Optional.empty());

		// When
		listener.handle(BaseEvent.PASSED, change);

		// Then
		verify(emailSender, never()).sendMail(anyString(), anyCollection(), anyString(), anyString());
	}
}
