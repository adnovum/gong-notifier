package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.events.BaseEvent;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class GithubClient {

	public void updateCommitStatus(String repo, String revision, BaseEvent event,
								   String context, String urlToPipeline, String authToken) throws GithubException {
		try {
			GitHub gh = GitHub.connectUsingOAuth(authToken);
			GHRepository ghRepo = gh.getRepository(repo);
			ghRepo.createCommitStatus(revision, toCommitStatus(event), urlToPipeline, "", context);
		} catch (IOException e) {
			throw new GithubException("Could not update commit status for repo " + repo + ", revision " + revision, e);
		}
	}

	private GHCommitState toCommitStatus(BaseEvent event) {
		switch (event) {
			case PASSED: return GHCommitState.SUCCESS;
			case FAILED: return GHCommitState.FAILURE;
			case CANCELLED: return GHCommitState.ERROR;
			default: return GHCommitState.PENDING;
		}
	}

	public static class GithubException extends Exception {
		public GithubException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
