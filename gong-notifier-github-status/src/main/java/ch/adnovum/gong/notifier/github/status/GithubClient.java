package ch.adnovum.gong.notifier.github.status;

import java.io.IOException;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo;
import ch.adnovum.gong.notifier.buildstatus.GitHostClient;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GithubClient implements GitHostClient {

	public void updateCommitStatus(MaterialInfo materialInfo, BaseEvent event,
								   String context, String urlToPipeline, String accessToken) throws GitHostClientException {
		String repoIdentifier = materialInfo.getRepoCoordinates().getProject() + "/" + materialInfo.getRepoCoordinates().getRepo();
		try {
			GitHub gh = GitHub.connectUsingOAuth(accessToken);
			GHRepository ghRepo = gh.getRepository(repoIdentifier);
			ghRepo.createCommitStatus(materialInfo.getRevision(), toCommitStatus(event), urlToPipeline, "", context);
		} catch (IOException e) {
			throw new GitHostClientException("Could not update commit status for repo " + repoIdentifier + ", revision " +
					materialInfo.getRevision(), e);
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


}
