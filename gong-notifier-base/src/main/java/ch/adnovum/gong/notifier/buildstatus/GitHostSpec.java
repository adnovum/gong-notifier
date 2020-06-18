package ch.adnovum.gong.notifier.buildstatus;

import java.util.Optional;

import ch.adnovum.gong.notifier.buildstatus.MaterialInfo.RepoCoordinates;
import ch.adnovum.gong.notifier.go.api.StageStateChange;

public interface GitHostSpec {

	Optional<MaterialInfo.MaterialType> matchMaterial(StageStateChange.Material mat);

	Optional<RepoCoordinates> extractRepoCoordinates(String url);
}
