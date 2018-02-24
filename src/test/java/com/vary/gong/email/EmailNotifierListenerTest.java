package com.vary.gong.email;

import com.vary.gong.go.api.PipelineConfig;
import com.vary.gong.go.api.StageStateChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotifierListenerTest {

	private static final List<BiConsumer<EmailNotificationListener, StageStateChange>> ALL_HANDLERS = Arrays.asList(
			EmailNotificationListener::handleBroken,
			EmailNotificationListener::handlePassed,
			EmailNotificationListener::handleBuilding,
			EmailNotificationListener::handleFailed,
			EmailNotificationListener::handleFixed,
			EmailNotificationListener::handleCancelled
	);

	@Mock
	private PipelineInfoProvider infoProvider;

	private TestEmailSender sender;

	private EmailNotificationListener listener;

	@Before
	public void setup() {
		sender = new TestEmailSender();
		listener = new EmailNotificationListener(infoProvider, sender);
	}

	@Test
	public void shouldSendToSingle() throws Exception {
		addMockEmailSettings("pipeline1",
				"GONG_EMAIL_ADDRESS", "frank@example.com");

		listener.handlePassed(new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed"));

		sender.assertMail(asList("frank@example.com"), "passed");
	}

	@Test
	public void shouldSendToSingleWithMatches() throws Exception {
		addMockEmailSettings("pipeline1",
				"GONG_EMAIL_ADDRESS", "frank@example.com",
				"GONG_EMAIL_ADDRESS_STATES", "fixed, broken");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		ALL_HANDLERS.forEach(h -> h.accept(listener, change));

		sender.assertCount(2);
		sender.assertMail(asList("frank@example.com"), "fixed");
		sender.assertMail(asList("frank@example.com"), "broken");
	}

	@Test
	public void shouldSendToMultipleWithMatches() throws Exception {
		addMockEmailSettings("pipeline1",
				"GONG_EMAIL_ADDRESS", "frank@example.com",
				"GONG_EMAIL_ADDRESS_STATES", "fixed, broken",
				"GONG_EMAIL_ADDRESS_2", "zonk@example.com",
				"GONG_EMAIL_ADDRESS_2_STATES", "failed",
				"GONG_EMAIL_ADDRESS_bla", "rop@example.com",
				"GONG_EMAIL_ADDRESS_bla_STATES", "building, failed");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		ALL_HANDLERS.forEach(h -> h.accept(listener, change));

		sender.assertCount(4);
		sender.assertMail(asList("frank@example.com"), "fixed");
		sender.assertMail(asList("frank@example.com"), "broken");
		sender.assertMail(asList("zonk@example.com", "rop@example.com"), "failed");
		sender.assertMail(asList("rop@example.com"), "building");
	}

	@Test
	public void shouldSendToNoneMatches() throws Exception {
		addMockEmailSettings("pipeline1",
				"GONG_EMAIL_ADDRESS", "frank@example.com",
				"GONG_EMAIL_ADDRESS_STATES", "broken");

		StageStateChange change = new StageStateChange("pipeline1",
				10,
				"stage1",
				"passed");
		listener.handlePassed(change);

		sender.assertCount(0);
	}

	private void addMockEmailSettings(String pipelineName, String... settings) {
		PipelineConfig cfg = new PipelineConfig();
		for (int i = 0; i < settings.length; i += 2) {
			cfg.addEnvironmentVariable(settings[i], settings[i + 1]);
		}

		when(infoProvider.getPipelineConfig(pipelineName)).thenReturn(Optional.of(cfg));
	}
}
