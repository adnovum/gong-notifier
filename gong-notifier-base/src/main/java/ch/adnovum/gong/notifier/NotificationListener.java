package ch.adnovum.gong.notifier;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.go.api.StageStateChange;

public interface NotificationListener {

	void handle(BaseEvent event, StageStateChange stateChange);
}
