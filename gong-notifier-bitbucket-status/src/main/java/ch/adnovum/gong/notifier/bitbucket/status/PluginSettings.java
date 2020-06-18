package ch.adnovum.gong.notifier.bitbucket.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.adnovum.gong.notifier.PluginSettingsBase;
import ch.adnovum.gong.notifier.go.api.SettingsField;

public class PluginSettings extends PluginSettingsBase {

	
	static final Map<String, SettingsField> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("bitbucketBaseUrl", new SettingsField("Bitbucket base URL", null, true, false,
				0));
		FIELD_CONFIG.put("bitbucketClonePattern", new SettingsField("Bitbucket clone URL pattern", null, true, false,
				0));
		FIELD_CONFIG.put("bitbucketGlobalAccessToken", new SettingsField("Bitbucket global access token", null, true, true,
				0));
		FIELD_CONFIG.put("cipherKeyFile", new SettingsField("Cipher Key File", null, true, false, 0));
	}

	private String cipherKeyFile;

	private String bitbucketBaseUrl;

	private String bitbucketClonePattern;

	private String bitbucketGlobalAccessToken;

	public String getCipherKeyFile() {
		return cipherKeyFile;
	}

	public void setCipherKeyFile(String cipherKeyFile) {
		this.cipherKeyFile = cipherKeyFile;
	}

	public String getBitbucketBaseUrl() {
		return bitbucketBaseUrl;
	}

	public void setBitbucketBaseUrl(String bitbucketBaseUrl) {
		this.bitbucketBaseUrl = bitbucketBaseUrl;
	}

	public String getBitbucketClonePattern() {
		return bitbucketClonePattern;
	}

	public void setBitbucketClonePattern(String bitbucketClonePattern) {
		this.bitbucketClonePattern = bitbucketClonePattern;
	}

	public String getBitbucketGlobalAccessToken() {
		return bitbucketGlobalAccessToken;
	}

	public void setBitbucketGlobalAccessToken(String bitbucketGlobalAccessToken) {
		this.bitbucketGlobalAccessToken = bitbucketGlobalAccessToken;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		PluginSettings that = (PluginSettings) o;
		return Objects.equals(getCipherKeyFile(), that.getCipherKeyFile()) &&
				Objects.equals(getBitbucketBaseUrl(), that.getBitbucketBaseUrl()) &&
				Objects.equals(getBitbucketClonePattern(), that.getBitbucketClonePattern()) &&
				Objects.equals(getBitbucketGlobalAccessToken(), that.getBitbucketGlobalAccessToken());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getCipherKeyFile(), getBitbucketBaseUrl(), getBitbucketClonePattern(),
				getBitbucketGlobalAccessToken());
	}
}
