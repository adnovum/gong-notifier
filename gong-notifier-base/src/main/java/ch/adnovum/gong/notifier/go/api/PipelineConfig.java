package ch.adnovum.gong.notifier.go.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public class PipelineConfig {

	public String name;

	@SerializedName("environment_variables")
	public List<EnvironmentVariable> environmentVariables = new LinkedList<>();

	public List<Material> materials = new LinkedList<>();


	public Optional<String> getEnvironmentVariableValue(String varName) {
		return environmentVariables
				.stream()
				.filter(v -> v.name.equals(varName))
				.map(v -> v.value)
				.findFirst();
	}

	public Optional<EnvironmentVariable> getEnvironmentVariable(String varName) {
		return environmentVariables
				.stream()
				.filter(v -> v.name.equals(varName))
				.findFirst();
	}

	public void addEnvironmentVariable(String name, String value) {
		EnvironmentVariable v = new EnvironmentVariable();
		v.name = name;
		v.value = value;
		environmentVariables.add(v);
	}

	public void addSecureVariable(String name, String encryptedValue) {
		EnvironmentVariable v = new EnvironmentVariable();
		v.secure = true;
		v.name = name;
		v.encryptedValue = encryptedValue;
		environmentVariables.add(v);
	}

	public static class EnvironmentVariable {
		public String name;
		public boolean secure;
		public String value;
		@SerializedName("encrypted_value")
		public String encryptedValue;
	}

	public static class Material {
		public String type;
		public Map<String, String> attributes = new HashMap<>();
	}
}
