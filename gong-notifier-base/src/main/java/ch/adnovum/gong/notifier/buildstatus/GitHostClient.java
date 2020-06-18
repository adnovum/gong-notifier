package ch.adnovum.gong.notifier.buildstatus;

import ch.adnovum.gong.notifier.events.BaseEvent;

public interface GitHostClient {

	void updateCommitStatus(MaterialInfo materialInfo, BaseEvent event, String context, String urlToPipeline, String accessToken)
			throws GitHostClientException;

	class GitHostClientException extends Exception {
		public GitHostClientException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
