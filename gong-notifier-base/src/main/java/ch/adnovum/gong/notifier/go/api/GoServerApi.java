package ch.adnovum.gong.notifier.go.api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GoServerApi {

	private static final Logger LOGGER = Logger.getLoggerFor(GoServerApi.class);

	private String baseUrl;
	private String adminUser;
	private String adminPassword;

	public GoServerApi(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public GoServerApi setAdminCredentials(String username, String password) {
		adminUser = username;
		adminPassword = password;
		return this;
	}

	public Optional<PipelineConfig> fetchPipelineConfig(String pipelineName) {
		String url = baseUrl + "/api/admin/pipelines/" + pipelineName;
		return fetch(url, PipelineConfig.class, null, adminUser, adminPassword);
	}

	public Optional<PipelineHistory> fetchPipelineHistory(String pipelineName) {
		String url = baseUrl + "/api/pipelines/" + pipelineName + "/history";
		return fetch(url, PipelineHistory.class, null, adminUser, adminPassword);
	}

	public Optional<ScmConfig> fetchScmConfig(String scmName) {
		String url = baseUrl + "/api/admin/scms/" + scmName;
		return fetch(url, ScmConfig.class, null, adminUser, adminPassword);
	}

	private static <T> Optional<T> fetch(String url, Class<T> clazz, String apiVer, String user, String password) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			if (apiVer != null) {
				conn.addRequestProperty("Accept", "application/vnd.go.cd." + apiVer + "+json");
			}
			else {
				// Use latest API version (introduced in GoCD 19.8)
				conn.addRequestProperty("Accept", "application/vnd.go.cd+json");
			}
			if (user != null) {
				String auth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
				conn.addRequestProperty("Authorization", "Basic " + auth);	// NOSONAR java:S2647: http scheme is user's choice
																			// and general it's only localhost
			}
			try (InputStreamReader r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
				T t = new Gson().fromJson(r, clazz);
				return Optional.of(t);
			}
		}
		catch (Exception e) {
			LOGGER.error("Error fetching " + url + ":", e);
			return Optional.empty();
		}
	}
}
