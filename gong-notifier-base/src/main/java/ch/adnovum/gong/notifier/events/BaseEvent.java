package ch.adnovum.gong.notifier.events;

public enum BaseEvent {
	BUILDING("building", "is building"),
	PASSED("passed", "passed"),
	FAILED("failed", "failed"),
	CANCELLED("cancelled", "is cancelled");

	private String value;
	private String verbString;

	BaseEvent(String value, String verbString) {
		this.value = value;
		this.verbString = verbString;
	}

	public String getValue() {
		return value;
	}

	public String getVerbString() {
		return verbString;
	}
}
