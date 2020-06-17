package ch.adnovum.gong.notifier.github.pr.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.adnovum.gong.notifier.PluginSettingsBase;
import ch.adnovum.gong.notifier.go.api.SettingsField;

public class PluginSettings extends PluginSettingsBase {

	
	static final Map<String, SettingsField> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("cipherKeyFile", new SettingsField("Cipher Key File", null, true, false, 0));
	}

	private String cipherKeyFile;

	public String getCipherKeyFile() {
		return cipherKeyFile;
	}

	public void setCipherKeyFile(String cipherKeyFile) {
		this.cipherKeyFile = cipherKeyFile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		PluginSettings that = (PluginSettings) o;
		return Objects.equals(getCipherKeyFile(), that.getCipherKeyFile());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getCipherKeyFile());
	}
}
