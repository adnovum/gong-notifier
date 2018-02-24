package com.vary.gong;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.vary.gong.go.api.StageStateChange;

public class DebugNotificationListener implements NotificationListener {

	private static Logger LOGGER = Logger.getLoggerFor(DebugNotificationListener.class);

	@Override
	public void handleBuilding(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " building!");
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " passed!");
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " failed!");
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " broke!");
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " fixed!");
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		LOGGER.info(stateChange.getPipelineName() + "." + stateChange.getStageName() + " cancelled!");
	}
}
