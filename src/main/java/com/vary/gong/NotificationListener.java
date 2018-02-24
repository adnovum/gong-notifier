package com.vary.gong;

import com.vary.gong.go.api.StageStateChange;

public interface NotificationListener {

	void handleBuilding(StageStateChange stateChange);
	void handlePassed(StageStateChange stateChange);
	void handleFailed(StageStateChange stateChange);
	void handleBroken(StageStateChange stateChange);
	void handleFixed(StageStateChange stateChange);
	void handleCancelled(StageStateChange stateChange);
}
