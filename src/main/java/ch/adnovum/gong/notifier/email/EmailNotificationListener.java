package ch.adnovum.gong.notifier.email;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.adnovum.gong.notifier.NotificationListener;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class EmailNotificationListener implements NotificationListener {

	private static final String EMAIL_ENV_VARIABLE = "GONG_EMAIL_ADDRESS";
	private static final String STATES_SUFFIX = "_STATES";

	private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationListener.class);

	private PipelineInfoProvider pipelineInfo;
	private EmailSender emailSender;
	private String senderEmail;
	private String subjectTemplate;

	public EmailNotificationListener(PipelineInfoProvider pipelineInfo, EmailSender emailSender, String senderEmail,
			String subjectTemplate) {
		this.pipelineInfo = pipelineInfo;
		this.emailSender = emailSender;
		this.senderEmail = senderEmail;
		this.subjectTemplate = subjectTemplate;
	}

	@Override
	public void handleBuilding(StageStateChange stateChange) {
		handle(stateChange,"building");
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		handle(stateChange,"passed");
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		handle(stateChange,"failed");
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		handle(stateChange,"broken");
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		handle(stateChange,"fixed");
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		handle(stateChange,"cancelled");
	}

	private void handle(StageStateChange stateChange, String state) {
		List<String> addrs = lookupPipelineEmails(stateChange.getPipelineName(), state);
		if (addrs.isEmpty()) {
			return;
		}

		LOGGER.info("Email for " + stateChange.getPipelineName() + ": " + String.join(",", addrs));
		Map<String, Object> templateVals = new HashMap<>();
		templateVals.put("pipeline", stateChange.getPipelineName());
		templateVals.put("stage", stateChange.getStageName());
		templateVals.put("state", state);

		String subject = TemplateHelper.fillTemplate(subjectTemplate, templateVals);
		String body = "yup";
		try {
			emailSender.sendMail(senderEmail, addrs, subject, body);
		} catch (Exception e) {
			LOGGER.error("Error sending email to " + String.join(",", addrs), e);
		}
	}

	private List<String> lookupPipelineEmails(String pipelineName, String state) {
		PipelineConfig cfg = pipelineInfo.getPipelineConfig(pipelineName).orElse(null);
		if (cfg == null) {
			LOGGER.error("Could not retrieve pipeline config for pipeline " + pipelineName);
			return new LinkedList<>();
		}

		Map<String, String> addrs = new HashMap<>();
		Set<String> notMatching = new HashSet<>();
		for (PipelineConfig.EnvironmentVariable v: cfg.environmentVariables) {
			if (v.name.startsWith(EMAIL_ENV_VARIABLE)) {
				if (v.name.endsWith(STATES_SUFFIX)) {
					// TODO: allow negating with !
					if (!v.value.toLowerCase().contains(state)) {
						notMatching.add(v.name.substring(0, v.name.length() - STATES_SUFFIX.length()));
					}
				}
				else {
					addrs.put(v.name, v.value);
				}
			}
		}
		notMatching.forEach(addrs::remove);

		return new LinkedList<>(addrs.values());
	}


}
