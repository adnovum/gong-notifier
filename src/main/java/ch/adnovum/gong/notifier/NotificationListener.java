package ch.adnovum.gong.notifier;

import ch.adnovum.gong.notifier.go.api.StageStateChange;

public interface NotificationListener {

	void handleBuilding(StageStateChange stateChange);
	void handlePassed(StageStateChange stateChange);
	void handleFailed(StageStateChange stateChange);
	void handleBroken(StageStateChange stateChange);
	void handleFixed(StageStateChange stateChange);
	void handleCancelled(StageStateChange stateChange);
}
