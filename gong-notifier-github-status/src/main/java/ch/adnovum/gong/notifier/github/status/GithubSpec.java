package ch.adnovum.gong.notifier.github.status;

import java.util.Objects;
import java.util.Optional;

import ch.adnovum.gong.notifier.buildstatus.GitHostSpec;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.google.common.base.Strings;

public class GithubSpec implements GitHostSpec  {

	static final String PR_MATERIAL_TYPE = "scm";
	static final String PR_PLUGIN_ID = "github.pr";
	static final String GIT_MATERIAL_TYPE = "git";
	static final String GIT_PLUGIN_ID = null;

	private static final String GITHUB_DOT_COM = "github.com";

	@Override
	public Optional<MaterialType> matchMaterial(StageStateChange.Material mat) {
		if (mat.configuration.url == null || !mat.configuration.url.contains(GITHUB_DOT_COM)) {
			return Optional.empty();
		}

		if (Objects.equals(mat.type, PR_MATERIAL_TYPE) && Objects.equals(mat.pluginId, PR_PLUGIN_ID)) {
			return Optional.of(MaterialType.SCM);
		}

		if (Objects.equals(mat.type, GIT_MATERIAL_TYPE) && Objects.equals(mat.pluginId, GIT_PLUGIN_ID)) {
			return Optional.of(MaterialType.GIT);
		}

		return Optional.empty();
	}

	@Override
	public Optional<MaterialInfo.RepoCoordinates> extractRepoCoordinates(String url) {
		// Formats:
		//  1) https://github.com/adnovum/gong-notifier.git
		//  2) git@github.com:adnovum/gong-notifier.git
		if (url == null) {
			return Optional.empty();
		}

		String[] parts = url.split("[:/]");
		if (parts.length < 2) {
			return Optional.empty();
		}

		String user = parts[parts.length - 2];
		String repo = parts[parts.length - 1];

		if (repo.endsWith(".git")) {
			repo = repo.substring(0, repo.length() - 4);
		}

		if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(repo)) {
			return Optional.empty();
		}

		return Optional.of(new MaterialInfo.RepoCoordinates(user, repo));
	}
}
