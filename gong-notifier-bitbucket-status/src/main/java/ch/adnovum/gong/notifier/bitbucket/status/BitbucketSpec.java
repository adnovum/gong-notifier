package ch.adnovum.gong.notifier.bitbucket.status;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.adnovum.gong.notifier.buildstatus.GitHostSpec;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import com.google.common.base.Strings;

public class BitbucketSpec implements GitHostSpec  {

	static final String PR_MATERIAL_TYPE = "scm";
	static final String PR_PLUGIN_ID = "stash.pr";
	static final String GIT_MATERIAL_TYPE = "git";
	static final String GIT_PLUGIN_ID = null;

	private final Pattern bitbucketUrlPattern;

	public BitbucketSpec(String bitbucketUrlPattern) {
		this.bitbucketUrlPattern = Pattern.compile(bitbucketUrlPattern);
	}

	@Override
	public Optional<MaterialType> matchMaterial(StageStateChange.Material mat) {

		if (mat.configuration.url == null || !bitbucketUrlPattern.matcher(mat.configuration.url).find()) {
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
		if (url == null) {
			return Optional.empty();
		}

		Matcher m = bitbucketUrlPattern.matcher(url);
		if (!m.find() || m.groupCount() < 2) {
			return Optional.empty();
		}

		String project = m.group(1);
		String repo = m.group(2);

		if (Strings.isNullOrEmpty(project) || Strings.isNullOrEmpty(repo)) {
			return Optional.empty();
		}

		return Optional.of(new MaterialInfo.RepoCoordinates(project, repo));
	}
}
