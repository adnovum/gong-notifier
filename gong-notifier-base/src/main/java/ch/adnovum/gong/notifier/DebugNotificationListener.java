package ch.adnovum.gong.notifier;

import com.thoughtworks.go.plugin.api.logging.Logger;
import ch.adnovum.gong.notifier.go.api.StageStateChange;

public class DebugNotificationListener implements NotificationListener {

	private static Logger LOGGER = Logger.getLoggerFor(DebugNotificationListener.class);

	@Override
	public void handleBuilding(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " building!");
	}

	@Override
	public void handlePassed(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " passed!");
	}

	@Override
	public void handleFailed(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " failed!");
	}

	@Override
	public void handleBroken(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " broke!");
	}

	@Override
	public void handleFixed(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " fixed!");
	}

	@Override
	public void handleCancelled(StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " cancelled!");
	}
}
