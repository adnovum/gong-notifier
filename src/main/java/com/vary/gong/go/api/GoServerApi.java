package com.vary.gong.go.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GoServerApi {

	private static Logger LOGGER = Logger.getLoggerFor(GoServerApi.class);

	private String baseUrl;

	public GoServerApi(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Optional<PipelineConfig> fetchPipelineConfig(String pipelineName) {
		String url = baseUrl + "/api/admin/pipelines/" + pipelineName;
		return fetch(url, "v5", PipelineConfig.class);
	}

	public Optional<PipelineHistory> fetchPipelineHistory(String pipelineName) {
		String url = baseUrl + "/api/pipelines/" + pipelineName + "/history";
		return fetch(url, null, PipelineHistory.class);
	}

	private static <T> Optional<T> fetch(String url, String apiVer, Class<T> clazz) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			if (apiVer != null) {
				conn.addRequestProperty("Accept", "application/vnd.go.cd." + apiVer + "+json");
			}
			try (InputStreamReader r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
				T t = new Gson().fromJson(r, clazz);
				return Optional.of(t);
			}
		}
		catch (IOException e) {
			LOGGER.error("Error fetching " + url + ":", e);
			return Optional.empty();
		}
	}

	public static void main(String[] args) throws IOException {
//		new GoServerApi("http://localhost:8153/go")
//				.fetchPipelineHistory("pipeline2")
//				.flatMap(h -> h.getPreviousStageResult("stage1")).ifPresent(c -> System.out.println(c));
	}
}
