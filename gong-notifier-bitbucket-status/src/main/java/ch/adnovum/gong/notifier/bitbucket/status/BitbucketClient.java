package ch.adnovum.gong.notifier.bitbucket.status;

import java.io.IOException;

import ch.adnovum.gong.notifier.buildstatus.GitHostClient;
import ch.adnovum.gong.notifier.buildstatus.MaterialInfo;
import ch.adnovum.gong.notifier.events.BaseEvent;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BitbucketClient implements GitHostClient {

	private static final MediaType JSON	= MediaType.get("application/json; charset=utf-8");

	private final String bitbucketBaseUrl;
	private OkHttpClient httpClient = new OkHttpClient();

	BitbucketClient(String bitbucketBaseUrl) {
		this.bitbucketBaseUrl = bitbucketBaseUrl;
	}

	public void updateCommitStatus(MaterialInfo materialInfo, BaseEvent event,
								   String context, String urlToPipeline, String accessToken) throws GitHostClientException {

		String url = String.format("%s/rest/build-status/1.0/commits/%s", bitbucketBaseUrl, materialInfo.getRevision());

		BuildStatusModel status = new BuildStatusModel();
		status.key = context;
		status.name = context;
		status.state = toBuildStatus(event);
		status.url = urlToPipeline;

		RequestBody body = RequestBody.create(new Gson().toJson(status), JSON);
		Request request = new Request.Builder()
				.url(url)
				.header("Authorization", "Bearer " + accessToken)
				.post(body)
				.build();
		try (Response resp = httpClient.newCall(request).execute()){
			if (resp.code() >= 400) {
				ResponseBody respBody = resp.body();
				String respText = respBody == null ? "" : respBody.string();
				throw new IOException("HTTP " + resp.code() + ": " + respText);
			}
		}
		catch (IOException e) {
			throw new GitHostClientException("Could not update commit status for commit " + materialInfo.getRepoCoordinates() +
					", revision " +	materialInfo.getRevision(), e);
		}
	}

	private BuildStatus toBuildStatus(BaseEvent event) {
		switch (event) {
			case PASSED: return BuildStatus.SUCCESSFUL;
			case FAILED: return BuildStatus.FAILED;
			case CANCELLED: return BuildStatus.FAILED;
			default: return BuildStatus.INPROGRESS;
		}
	}

	private enum BuildStatus {
		INPROGRESS,
		SUCCESSFUL,
		FAILED
	}

	private class BuildStatusModel {
		BuildStatus state;
		String key;
		String name;
		String url;
		String description;
	}
}
