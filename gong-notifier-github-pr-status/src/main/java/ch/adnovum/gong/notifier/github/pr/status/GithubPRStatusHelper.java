package ch.adnovum.gong.notifier.github.pr.status;

import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.common.base.Strings;

import java.util.Objects;

public class GithubPRStatusHelper {

	// FIXME
	static final String EXPECTED_MATERIAL_TYPE = "git";
	// FIXME
	static final String EXPECTED_SCM_PLUGIN_ID = null;

	private static final String EXPECTED_GITHUB_PAGE = "github.com";

	public static String getGithubPRMaterialUrl(StageStateChange stateChange) {
		return stateChange.pipeline.buildCause.stream()
				.map(bc -> bc.material)
				.filter(m -> m != null && EXPECTED_MATERIAL_TYPE.equals(m.type))
				.filter(m -> Objects.equals(EXPECTED_SCM_PLUGIN_ID, m.pluginId))
				.map(m -> m.configuration.url)
				.filter(u -> u != null && u.contains(EXPECTED_GITHUB_PAGE))
				.findFirst()
				.orElse(null);
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
