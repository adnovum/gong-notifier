package ch.adnovum.gong.notifier.events;

public enum HistoricalEvent {
	BUILDING(BaseEvent.BUILDING.getValue(), BaseEvent.BUILDING.getVerbString()),
	PASSED(BaseEvent.PASSED.getValue(), BaseEvent.PASSED.getVerbString()),
	FAILED(BaseEvent.FAILED.getValue(), BaseEvent.FAILED.getVerbString()),
	CANCELLED(BaseEvent.CANCELLED.getValue(), BaseEvent.CANCELLED.getVerbString()),
	FIXED("fixed", "is fixed"),
	BROKEN("broken", "is broken");

	private String value;
	private String verbString;

	HistoricalEvent(String value, String verbString) {
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
