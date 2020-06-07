package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.google.common.base.Strings;

import java.util.Objects;
import java.util.Optional;

public class GithubPRStatusHelper {

	static final String EXPECTED_MATERIAL_TYPE = "scm";
	static final String EXPECTED_SCM_PLUGIN_ID = "github.pr";
	private static final String EXPECTED_GITHUB_PAGE = "github.com";

	public static class GithubPRInfo {
		private String url;
		private String revision;

		public String getUrl() {
			return url;
		}

		public String getRevision() {
			return revision;
		}
	}

	public static GithubPRInfo getGithubPRInfo(StageStateChange stateChange) {
		return findGithubPRBuildCause(stateChange)
				.map(GithubPRStatusHelper::toPRInfo)
				.orElse(null);
	}

	private static Optional<StageStateChange.BuildCause> findGithubPRBuildCause(StageStateChange stateChange) {
		return stateChange.pipeline.buildCause.stream()
				.filter(c -> c.material != null)
				.filter(c -> Objects.equals(EXPECTED_MATERIAL_TYPE, c.material.type))
				.filter(c -> Objects.equals(EXPECTED_SCM_PLUGIN_ID, c.material.pluginId))
				.filter(c -> c.material.configuration.url != null &&
						c.material.configuration.url.contains(EXPECTED_GITHUB_PAGE))
				.findFirst();
	}

	private static GithubPRInfo toPRInfo(StageStateChange.BuildCause cause) {
		GithubPRInfo info = new GithubPRInfo();
		info.url = cause.material.configuration.url;
		info.revision = cause.modifications == null || cause.modifications.isEmpty() ? null :
				cause.modifications.get(0).revision;
		return info;
	}

	public static String getRepoFromUrl(String gitUrl) {
		// Formats:
		//  1) https://github.com/adnovum/gong-notifier.git
		//  2) git@github.com:adnovum/gong-notifier.git
		if (gitUrl == null) {
			return null;
		}

		String[] parts = gitUrl.split("[:/]");
		if (parts.length < 2) {
			return null;
		}

		String user = parts[parts.length - 2];
		String repo = parts[parts.length - 1];

		if (repo.endsWith(".git")) {
			repo = repo.substring(0, repo.length() - 4);
		}

		if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(repo)) {
			return null;
		}

		return user + "/" + repo;
	}
}
