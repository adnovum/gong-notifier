package ch.adnovum.gong.notifier.go.api;

@SuppressWarnings("java:S1104")
public class SettingsField {

	public String displayName;
	public String defaultValue;
	public boolean isRequired;
	public boolean isSecure;
	public int displayOrder;

	public SettingsField(String displayName, String defaultValue, boolean isRequired, boolean isSecure,
			int displayOrder) {
		this.displayName = displayName;
		this.defaultValue = defaultValue;
		this.isRequired = isRequired;
		this.isSecure = isSecure;
		this.displayOrder = displayOrder;
	}
}
