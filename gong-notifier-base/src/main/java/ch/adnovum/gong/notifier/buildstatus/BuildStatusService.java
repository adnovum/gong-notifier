package ch.adnovum.gong.notifier.buildstatus;

import java.util.Optional;

import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.go.api.PipelineConfig;
import ch.adnovum.gong.notifier.go.api.PipelineConfig.EnvironmentVariable;
import ch.adnovum.gong.notifier.go.api.ScmConfig;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.services.ConfigService;
import ch.adnovum.gong.notifier.services.SecretDecryptService;
import ch.adnovum.gong.notifier.util.SecretDecryptException;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class BuildStatusService {

	public static final String STATUS_AUTH_TOKEN = "GONG_STATUS_AUTH_TOKEN";

	private static final Logger LOGGER = Logger.getLoggerFor(BuildStatusService.class);

	private static final String PASSWORD_KEY = "password";
	private static final String ENCRYPTED_PASSWORD_KEY = "encrypted_password";

	private final ConfigService cfgService;
	private final SecretDecryptService decryptService;
	private final GitHostSpec gitHostSpec;


	public BuildStatusService(ConfigService cfgService, SecretDecryptService decryptService, GitHostSpec gitHostSpec) {
		this.decryptService = decryptService;
		this.cfgService = cfgService;
		this.gitHostSpec = gitHostSpec;
	}


	public MaterialInfo getMaterialInfo(StageStateChange stateChange) {
		MaterialInfo materialInfo = findMaterialInfo(stateChange);
		if (materialInfo != null && validateMaterialInfo(materialInfo, stateChange)) {
			return materialInfo;
		}
		return null;
	}

	private MaterialInfo findMaterialInfo(StageStateChange stateChange) {
		for (StageStateChange.BuildCause cause: stateChange.pipeline.buildCause) {
			StageStateChange.Material material = cause.material;
			if (material == null) {
				continue;
			}

			MaterialType matchingMatType = gitHostSpec.matchMaterial(material).orElse(null);
			if (matchingMatType != null) {
				return toMaterialInfo(cause, matchingMatType);
			}
		}
		return null;
	}

	private MaterialInfo toMaterialInfo(StageStateChange.BuildCause cause, MaterialType materialType) {
		MaterialInfo info = new MaterialInfo();
		info.setMaterialType(materialType);
		info.setUrl(cause.material.configuration.url);
		info.setRepoCoordinates(gitHostSpec.extractRepoCoordinates(info.getUrl()).orElse(null));

		String rev = cause.modifications == null || cause.modifications.isEmpty() ? null :
				cause.modifications.get(0).revision;
		info.setRevision(rev);

		return info;
	}

	private boolean validateMaterialInfo(MaterialInfo materialInfo, StageStateChange stateChange) {
		if (materialInfo.getRevision() == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have a revision. Skipping.");
			return false;
		}

		if (materialInfo.getRepoCoordinates() == null ||
				Strings.isNullOrEmpty(materialInfo.getRepoCoordinates().getProject()) ||
				Strings.isNullOrEmpty(materialInfo.getRepoCoordinates().getRepo())) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "cannot extract valid repo coordinates from " +
					"material URL " + materialInfo.getUrl() + ". Skipping.");
			return false;
		}

		return true;
	}

	public String fetchAccessToken(StageStateChange stateChange, MaterialInfo materialInfo) {
		EnvironmentVariable var = fetchAccessTokenVariable(stateChange, materialInfo).orElse(null);

		if (var == null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have an access token. Skipping.");
			return null;
		}

		if (!var.secure) {
			return var.value;

		}

		try {
			return decryptService.decrypt(var.encryptedValue);
		}
		catch (SecretDecryptException e) {
			LOGGER.error(pipelineLogPrefix(stateChange) + "could not decrypt access token");
			return null;
		}
	}

	private Optional<EnvironmentVariable> fetchAccessTokenVariable(StageStateChange stateChange, MaterialInfo materialInfo) {
		PipelineConfig cfg = cfgService.fetchPipelineConfig(stateChange.getPipelineName(), stateChange.getPipelineCounter())
				.orElse(null);
		if (cfg == null) {
			return Optional.empty();
		}

		EnvironmentVariable envVar = cfg.getEnvironmentVariable(STATUS_AUTH_TOKEN).orElse(null);
		if (envVar != null) {
			LOGGER.debug(pipelineLogPrefix(stateChange) + "has " + STATUS_AUTH_TOKEN + " variable configured."
					+ "Using that.");
			return Optional.of(envVar);
		}

		// Fallback to password that might be set for the material itself.
		LOGGER.debug(pipelineLogPrefix(stateChange) + "does not have " + STATUS_AUTH_TOKEN + " variable configured."
				+ "Trying to fallback to token configured on the material.");

		switch (materialInfo.getMaterialType()) {
			case SCM: return fetchAccessTokenFromSCMMaterial(cfg, cfgService);
			case GIT: return fetchAccessTokenFromGitMaterial(cfg, materialInfo);
			default: return Optional.empty();
		}
	}

	private static Optional<EnvironmentVariable> fetchAccessTokenFromSCMMaterial(PipelineConfig cfg, ConfigService cfgService) {
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
				.map(BuildStatusService::scmToEnvVar);
	}

	private static EnvironmentVariable scmToEnvVar(ScmConfig.ConfigEntry scmCfgEntry) {
		EnvironmentVariable var = new EnvironmentVariable();
		var.name = scmCfgEntry.key;
		var.secure = scmCfgEntry.encryptedValue != null;
		var.value = scmCfgEntry.value;
		var.encryptedValue = scmCfgEntry.encryptedValue;
		return var;
	}

	private static Optional<EnvironmentVariable> fetchAccessTokenFromGitMaterial(PipelineConfig cfg, MaterialInfo materialInfo) {
		return cfg.materials.stream()
				.filter(m -> "git".equals(m.type))
				.filter(m -> materialInfo.getUrl().equals(m.attributes.get("url")))
				.filter(m -> m.attributes.containsKey(ENCRYPTED_PASSWORD_KEY) || m.attributes.containsKey(PASSWORD_KEY))
				.findFirst()
				.map(BuildStatusService::gitMatToEnvVar);
	}

	private static EnvironmentVariable gitMatToEnvVar(PipelineConfig.Material mat) {
		EnvironmentVariable var = new EnvironmentVariable();
		var.name = "password";
		var.secure = mat.attributes.containsKey(ENCRYPTED_PASSWORD_KEY);
		var.value = mat.attributes.get(PASSWORD_KEY);
		var.encryptedValue = mat.attributes.get(ENCRYPTED_PASSWORD_KEY);
		return var;
	}

	private static String pipelineLogPrefix(StageStateChange stateChange) {
		return "Pipeline " + stateChange.getPipelineName() + ": ";
	}
}
