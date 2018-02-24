package com.vary.gong.go.api;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PipelineConfig {

	public String name;

	@SerializedName("environment_variables")
	public List<EnvironmentVariable> environmentVariables = new LinkedList<>();

	public Optional<String> getEnvironmentVariableValue(String varName) {
		return environmentVariables
				.stream()
				.filter(v -> v.name.equals(varName))
				.map(v -> v.value)
				.findFirst();
	}

	public void addEnvironmentVariable(String name, String value) {
		EnvironmentVariable v = new EnvironmentVariable();
		v.name = name;
		v.value = value;
		environmentVariables.add(v);
	}

	public static class EnvironmentVariable {
		public String name;
		public String value;
	}
}
