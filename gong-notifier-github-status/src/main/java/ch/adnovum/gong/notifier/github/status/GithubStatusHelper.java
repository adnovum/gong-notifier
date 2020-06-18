package ch.adnovum.gong.notifier.github.status;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GithubStatusHelper {

	static final String STATUS_AUTH_TOKEN = "GONG_STATUS_AUTH_TOKEN";

	private static final Logger LOGGER = Logger.getLoggerFor(GithubStatusHelper.class);

	static final String SCM_MATERIAL_TYPE = "scm";
	static final String PR_PLUGIN_ID = "github.pr";
	static final String GIT_MATERIAL_TYPE = "git";
	static final String GIT_PLUGIN_ID = null;

	private static final String PASSWORD_KEY = "password";
	private static final String ENCRYPTED_PASSWORD_KEY = "encrypted_password";

	private static final List<Predicate<StageStateChange.Material>> VALID_MATERIALS = Arrays.asList(
			m -> Objects.equals(m.type, SCM_MATERIAL_TYPE) && Objects.equals(m.pluginId, PR_PLUGIN_ID),
			m -> Objects.equals(m.type, GIT_MATERIAL_TYPE) && Objects.equals(m.pluginId, GIT_PLUGIN_ID)
	);

	private static final String EXPECTED_GITHUB_PAGE = "github.com";

	private GithubStatusHelper() {
		// Static utility class.
	}

	public static class GithubInfo {
		private GithubMaterialType materialType;
		private String url;
		private String revision;

		public GithubInfo() {
			// Default
		}

		GithubInfo(GithubMaterialType materialType, String url, String revision) {
			this.materialType = materialType;
			this.url = url;
			this.revision = revision;
		}

		public String getUrl() {
			return url;
		}

		public String getRevision() {
			return revision;
		}

		public GithubMaterialType getMaterialType() {
			return materialType;
		}
	}

	public enum GithubMaterialType {
		GIT,
		PR
	}

	public static GithubInfo getGithubInfo(StageStateChange stateChange) {
		return findGithubBuildCause(stateChange)
				.map(GithubStatusHelper::toGithubInfo)
				.orElse(null);
	}

	private static Optional<StageStateChange.BuildCause> findGithubBuildCause(StageStateChange stateChange) {
		return stateChange.pipeline.buildCause.stream()
				.filter(c -> isValidMaterial(c.material))
				.filter(c -> c.material.configuration.url != null &&
						c.material.configuration.url.contains(EXPECTED_GITHUB_PAGE))
				.findFirst();
	}

	private static boolean isValidMaterial(StageStateChange.Material mat) {
		return mat != null && VALID_MATERIALS.stream().anyMatch(f -> f.test(mat));
	}

	private static GithubInfo toGithubInfo(StageStateChange.BuildCause cause) {
		GithubInfo info = new GithubInfo();
		info.materialType = GIT_MATERIAL_TYPE.equals(cause.material.type) ? GithubMaterialType.GIT : GithubMaterialType.PR;
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

	public static Optional<EnvironmentVariable> fetchAccessTokenVariable(StageStateChange stateChange, GithubInfo ghInfo,
			ConfigService cfgService) {
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

		// Fallback to password that might be set for the material itself.
		LOGGER.debug("Pipeline " + stateChange.getPipelineName() + " does not have " + STATUS_AUTH_TOKEN + " variable configured."
				+ "Trying to fallback to token configured on the material.");

		switch (ghInfo.getMaterialType()) {
			case PR: return fetchAccessTokenFromPRMaterial(cfg, cfgService);
			case GIT: return fetchAccessTokenFromGitMaterial(cfg, ghInfo);
			default: return Optional.empty();
		}
	}

	private static Optional<EnvironmentVariable> fetchAccessTokenFromPRMaterial(PipelineConfig cfg, ConfigService cfgService) {
		// This is best effort: we only check the first matching material in the pipeline,
		// otherwise we'd have to N+1 load all materials from the API. This is sadly necessary
		// because the notification doesn't mention the material ID, so we have to guess based on all
		// the configured materials.
		Optional<String> prMatName = cfg.materials.stream()
				.filter(m -> "plugin".equals(m.type))
				.filter(m -> m.attributes.containsKey("ref"))
				.map(m -> m.attributes.get("ref"))
				.findFirst();

		Optional<ScmConfig> prMatCfg = prMatName
				.flatMap(cfgService::fetchScmConfig);

		return prMatCfg
				.flatMap(c -> c.getConfigurationEntry(PASSWORD_KEY))
				.map(GithubStatusHelper::scmToEnvVar);
	}

	private static EnvironmentVariable scmToEnvVar(ScmConfig.ConfigEntry scmCfgEntry) {
		EnvironmentVariable var = new EnvironmentVariable();
		var.name = scmCfgEntry.key;
		var.secure = scmCfgEntry.encryptedValue != null;
		var.value = scmCfgEntry.value;
		var.encryptedValue = scmCfgEntry.encryptedValue;
		return var;
	}

	private static Optional<EnvironmentVariable> fetchAccessTokenFromGitMaterial(PipelineConfig cfg, GithubInfo ghInfo) {
		return cfg.materials.stream()
				.filter(m -> GIT_MATERIAL_TYPE.equals(m.type))
				.filter(m -> ghInfo.url.equals(m.attributes.get("url")))
				.filter(m -> m.attributes.containsKey(ENCRYPTED_PASSWORD_KEY) || m.attributes.containsKey(PASSWORD_KEY))
				.findFirst()
				.map(GithubStatusHelper::gitMatToEnvVar);
	}

	private static EnvironmentVariable gitMatToEnvVar(PipelineConfig.Material mat) {
		EnvironmentVariable var = new EnvironmentVariable();
		var.name = "password";
		var.secure = mat.attributes.containsKey(ENCRYPTED_PASSWORD_KEY);
		var.value = mat.attributes.get(PASSWORD_KEY);
		var.encryptedValue = mat.attributes.get(ENCRYPTED_PASSWORD_KEY);
		return var;
	}

}
