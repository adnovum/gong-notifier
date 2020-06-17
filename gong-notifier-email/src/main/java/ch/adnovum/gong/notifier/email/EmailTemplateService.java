package ch.adnovum.gong.notifier.email;

import static ch.adnovum.gong.notifier.util.GongUtil.escapeHtml;

import java.util.HashMap;
import java.util.Map;

import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.ModificationListGenerator;
import ch.adnovum.gong.notifier.util.TemplateHelper;

public class EmailTemplateService {

	private final String subjectTemplate;
	private final String bodyTemplate;
	private final String serverDisplayUrl;
	private final ModificationListGenerator modListGenerator;

	EmailTemplateService(String subjectTemplate, String bodyTemplate, String serverDisplayUrl,
			ModificationListGenerator modListGenerator) {
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.serverDisplayUrl = serverDisplayUrl;
		this.modListGenerator = modListGenerator;
	}

	InstantiatedEmail instantiateEmail(StageStateChange stateChange, HistoricalEvent histEvent,
			PipelineHistory.BuildCause cause) {
		Map<String, Object> templateVals = new HashMap<>();
		templateVals.put("pipeline", escapeHtml(stateChange.getPipelineName()));
		templateVals.put("stage", escapeHtml(stateChange.getStageName()));
		templateVals.put("pipelineCounter", stateChange.getPipelineCounter());
		templateVals.put("stageCounter", stateChange.getStageCounter());
		templateVals.put("event", escapeHtml(histEvent.getVerbString()));
		templateVals.put("serverUrl", escapeHtml(serverDisplayUrl));
		templateVals.put("modificationList", generateModificationList(cause));

		InstantiatedEmail mail = new InstantiatedEmail();
		mail.subject = TemplateHelper.fillTemplate(subjectTemplate, templateVals);
		mail.body = TemplateHelper.fillTemplate(bodyTemplate, templateVals);
		return mail;
	}

	private String generateModificationList(PipelineHistory.BuildCause cause) {
		if (cause == null) {
			return "";
		}
		return modListGenerator.generateModificationList(cause.materialRevisions);
	}

	class InstantiatedEmail {
		private String subject;
		private String body;

		String getSubject() {
			return subject;
		}

		String getBody() {
			return body;
		}
	}
}
