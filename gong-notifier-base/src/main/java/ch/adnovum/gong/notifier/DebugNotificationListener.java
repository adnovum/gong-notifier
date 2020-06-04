package ch.adnovum.gong.notifier;

import ch.adnovum.gong.notifier.events.BaseEvent;
import com.thoughtworks.go.plugin.api.logging.Logger;
import ch.adnovum.gong.notifier.go.api.StageStateChange;

public class DebugNotificationListener implements NotificationListener {

	private static Logger LOGGER = Logger.getLoggerFor(DebugNotificationListener.class);

	@Override
	public void handle(BaseEvent event, StageStateChange stateChange) {
		LOGGER.debug(stateChange.getPipelineName() + "." + stateChange.getStageName() + " " +  event.getVerbString() + " !");
	}
}
