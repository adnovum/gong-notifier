package ch.adnovum.gong.notifier.go.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("java:S1104")
public class ScmConfig {

	public String id;
	public String name;
	public List<ConfigEntry> configuration = new LinkedList<>();

	public Optional<ConfigEntry> getConfigurationEntry(String key) {
		return configuration.stream()
				.filter(e -> key.equals(e.key))
				.findFirst();
	}

	public static class ConfigEntry {
		public String key;
		public String value;
		@SerializedName("encrypted_value")
		public String encryptedValue;
	}
}
