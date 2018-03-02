package ch.adnovum.gong.notifier;

import static ch.adnovum.gong.notifier.GongUtil.readResourceString;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import javax.mail.internet.MimeMessage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GongNotifierPluginIntTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(18153));

	@Rule
	public final GreenMailRule greenMail = new GreenMailRule(new ServerSetup(30025, "localhost", "smtp"));

	@Mock
	private GoApplicationAccessor goAppAccessor;

	private GongNotifierPlugin plugin;

	@Before
	public void setup() {
		// Mock plugin settings response
		PluginSettings settings = new PluginSettings();
		settings.setServerUrl("http://localhost:18153/go");
		settings.setSmtpPort(30025);
		DefaultGoApiResponse resp = new DefaultGoApiResponse(200);
		resp.setResponseBody(new Gson().toJson(settings));
		when(goAppAccessor.submit(any(GoApiRequest.class))).thenReturn(resp);

		plugin = new GongNotifierPlugin();
		plugin.initializeGoApplicationAccessor(goAppAccessor);
	}

	@Test
	public void shouldNotifyMailingList() throws Exception {
		// Given a pipeline config with configured mailing lists.
		stubFor(get("/go/api/admin/pipelines/pipeline1")
				.willReturn(okJson(readResourceString("/test-pipeline-config-response.json", getClass()))));

		// And given the last stage execution of this pipeline failed.
		stubFor(get("/go/api/pipelines/pipeline1/history")
				.willReturn(okJson(readResourceString("/test-pipeline-history-response.json", getClass()))));

		// And given a state change request of said stage to "passed".
		DefaultGoPluginApiRequest req = new DefaultGoPluginApiRequest("", "", GongNotifierPlugin.REQUEST_STAGE_STATUS);
		req.setRequestBody(readResourceString("/test-stage-status-request.json", getClass()));

		// When the plugin handles this request
		plugin.handle(req);

		// Then a mail is sent to the mailing list to notify about the fixed pipeline.
		MimeMessage[] emails = greenMail.getReceivedMessages();
		assertEquals(1, emails.length);
		MimeMessage mail = emails[0];
		assertEquals("Stage [pipeline1/12/stage1/1] is fixed", mail.getSubject());
		assertEquals(readResourceString("/test-fixed-stage-email-body.html", getClass()), GreenMailUtil.getBody(mail));
	}
}
