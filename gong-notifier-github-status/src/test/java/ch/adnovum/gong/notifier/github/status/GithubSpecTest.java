package ch.adnovum.gong.notifier.github.status;

import static ch.adnovum.gong.notifier.github.status.GithubSpec.GIT_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.status.GithubSpec.GIT_PLUGIN_ID;
import static ch.adnovum.gong.notifier.github.status.GithubSpec.PR_MATERIAL_TYPE;
import static ch.adnovum.gong.notifier.github.status.GithubSpec.PR_PLUGIN_ID;
import static org.junit.Assert.assertEquals;

import ch.adnovum.gong.notifier.buildstatus.MaterialInfo;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.MaterialType;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.go.api.StageStateChange.Material;
import org.junit.Test;

public class GithubSpecTest {

	@Test
	public void shouldExtractRepoCoordinates() {
		String[] params = new String[]{
				"https://github.com/adnovum/gong-notifier.git", "adnovum", "gong-notifier",
				"git@github.com:adnovum/gong-notifier.git", "adnovum", "gong-notifier",
				"https://github.com/adnovum/gong-notifier", "adnovum", "gong-notifier",
				"git@github.com:adnovum/gong-notifier", "adnovum", "gong-notifier",
				"http://blaa@github124.de/adnovum/gong-notifier.git", "adnovum", "gong-notifier",
				"blabasdgeg@github12424.de:adnovum/gong-notifier", "adnovum", "gong-notifier",
				"http://github.company.com:1234/adnovum/gong-notifier.git", "adnovum", "gong-notifier",
				"git@github.company.com:1234:adnovum/gong-notifier", "adnovum", "gong-notifier",
				"blablabla", null, null,
				"//", null, null,
				"", null, null,
				null, null, null,
		};

		GithubSpec spec = new GithubSpec();

		for (int i = 0; i < params.length; i += 3) {
			String input = params[i];
			String expectedProject = params[i + 1];
			String expectedRepo = params[i + 2];
			MaterialInfo.RepoCoordinates coords = spec.extractRepoCoordinates(input).orElse(null);
			assertEquals("Expected project for " + input, expectedProject, coords == null ? null : coords.getProject());
			assertEquals("Expected repo for " + input, expectedRepo, coords == null ? null : coords.getRepo());
		}
	}

	@Test
	public void shouldMatchMaterials() {
		Object[] params = new Object[]{
				material(PR_MATERIAL_TYPE, PR_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"), MaterialType.SCM,
				material(GIT_MATERIAL_TYPE, GIT_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"), MaterialType.GIT,
				material(GIT_MATERIAL_TYPE, PR_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"), null,
				material(PR_MATERIAL_TYPE, GIT_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"), null,
				material("other type", GIT_PLUGIN_ID, "https://github.com/adnovum/gong-notifier.git"), null,
				material(GIT_MATERIAL_TYPE, "other plugin", "https://github.com/adnovum/gong-notifier.git"), null,
				material(GIT_MATERIAL_TYPE, GIT_PLUGIN_ID, "example.com"), null,
				material(null, null, null), null,
		};

		GithubSpec spec = new GithubSpec();

		for (int i = 0; i < params.length; i += 2) {
			Material mat = (Material) params[i];
			MaterialType expectedType = (MaterialType) params[i + 1];
			MaterialType type = spec.matchMaterial(mat).orElse(null);
			assertEquals("Expected material type for " + mat, expectedType, type);
		}
	}

	private static Material material(String type, String pluginId, String url) {
		Material mat = new Material();
		mat.type = type;
		mat.pluginId = pluginId;
		mat.configuration = new StageStateChange.MaterialConfig();
		mat.configuration.url = url;
		return mat;
	}
}
