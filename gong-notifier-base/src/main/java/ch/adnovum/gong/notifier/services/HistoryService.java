package ch.adnovum.gong.notifier.services;

import ch.adnovum.gong.notifier.events.BaseEvent;
import ch.adnovum.gong.notifier.events.HistoricalEvent;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import ch.adnovum.gong.notifier.go.api.StageStateChange;
import ch.adnovum.gong.notifier.util.SessionCache;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_FAILED;
import static ch.adnovum.gong.notifier.go.api.GoApiConstants.STATUS_PASSED;

public class HistoryService {

	private static Logger LOGGER = Logger.getLoggerFor(HistoryService.class);

	private final SessionCache<String, PipelineHistory, Integer> historyCache;

	public HistoryService(GoServerApi api) {
		historyCache = new SessionCache<>(5, TimeUnit.MINUTES, 1000, (k, v) -> api.fetchPipelineHistory(k));
	}

	public Optional<PipelineHistory> fetchPipelineHistory(String pipelineName, int pipelineCounter) {
		return historyCache.fetch(pipelineName, pipelineCounter);
	}

	public HistoricalEvent determineHistoricalEvent(BaseEvent baseEvent, StageStateChange stateChange) {
		String oldState = fetchOldState(stateChange);

		switch (baseEvent) {
			case BUILDING:
				return HistoricalEvent.BUILDING;
			case PASSED:
				return STATUS_FAILED.equals(oldState)
						? HistoricalEvent.FIXED
						: HistoricalEvent.PASSED;
			case FAILED:
				return STATUS_PASSED.equals(oldState)
						? HistoricalEvent.BROKEN
						: HistoricalEvent.FAILED;
			case CANCELLED:
				return HistoricalEvent.CANCELLED;
			default:
				LOGGER.error("Unknown base state " + baseEvent);
				return null;
		}
	}

	private String fetchOldState(StageStateChange stateChange) {
		String oldState = null;

		if (stateChange.getPipelineCounter() > 1) {
			oldState = fetchPipelineHistory(stateChange.getPipelineName(), stateChange.getPipelineCounter())
					.flatMap(h -> h.getPreviousStageResult(stateChange.getStageName(), stateChange.getPipelineCounter()))
					.orElse(null);

			if (oldState == null) {
				LOGGER.warn("Could not get previous state of " + stateChange.getPipelineName() + "/" +
						stateChange.getPipelineCounter() + "/" + stateChange.getStageName());
			}
		}
		return oldState;
	}
}
