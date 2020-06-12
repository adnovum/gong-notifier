package ch.adnovum.gong.notifier.github.pr.status;

import java.util.Objects;
import java.util.Optional;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GithubPRStatusHelper {

	static final String STATUS_AUTH_TOKEN = "GONG_STATUS_AUTH_TOKEN";

	private static Logger LOGGER = Logger.getLoggerFor(GithubPRStatusHelper.class);

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

	public static Optional<EnvironmentVariable> fetchAccessTokenVariable(StageStateChange stateChange, ConfigService cfgService) {
		PipelineConfig cfg = cfgService.fetchPipelineConfig(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.orElse(null);
		if (cfg == null) {
			return Optional.empty();
		}

		EnvironmentVariable envVar = cfg.getEnvironmentVariable(STATUS_AUTH_TOKEN).orElse(null);
		if (envVar != null) {
			LOGGER.debug("Pipeline " + stateChange.getPipelineName() + " has " + STATUS_AUTH_TOKEN + " variable configured."
					+ "Using that.");
			return Optional.of(envVar);
		}

		// Fallback to the auth token used for the PR material.
		// This is best effort: we only check the first matching material in the pipeline,
		// otherwise we'd have to N+1 load all materials from the API. This is sadly necessary
		// because the notification doesn't mention the material ID, so we have to guess based on all
		// the configured materials.
		LOGGER.debug("Pipeline " + stateChange.getPipelineName() + " does not have " + STATUS_AUTH_TOKEN + " variable configured."
				+ "Trying to fallback to token configured on the PR material.");

		Optional<String> prMatName = cfg.materials.stream()
				.filter(m -> "plugin".equals(m.type))
				.filter(m -> m.attributes.containsKey("ref"))
				.map(m -> m.attributes.get("ref"))
				.findFirst();

		Optional<ScmConfig> prMatCfg = prMatName
				.flatMap(cfgService::fetchScmConfig);

		return prMatCfg
				.flatMap(c -> c.getConfigurationEntry("password"))
				.map(GithubPRStatusHelper::toEnvVar);
	}

	private static EnvironmentVariable toEnvVar(ScmConfig.ConfigEntry scmCfgEntry) {
		EnvironmentVariable var = new EnvironmentVariable();
		var.name = scmCfgEntry.key;
		var.secure = scmCfgEntry.encryptedValue != null;
		var.value = scmCfgEntry.value;
		var.encryptedValue = scmCfgEntry.encryptedValue;
		return var;
	}
}
