package ch.adnovum.gong.notifier.util;

import static ch.adnovum.gong.notifier.util.GongUtil.readResourceString;

import java.util.List;

import ch.adnovum.gong.notifier.go.api.PipelineHistory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

public class ModificationListGeneratorTest {

	private final static List<PipelineHistory.MaterialRevision> REVISIONS = new Gson().fromJson(
			readResourceString("/test-material-revisions.json"),
			new TypeToken<List<PipelineHistory.MaterialRevision>>(){}.getType());

	@Test
	public void shouldGeneratePlain() {
		ModificationListGenerator gen = new ModificationListGenerator("UTC", false);

		String modList = gen.generateModificationList(REVISIONS);
		Assert.assertEquals(readResourceString("/test-plain-modification-list.txt"), modList);
	}

	@Test
	public void shouldGenerateWithOtherTimezone() {
		ModificationListGenerator gen = new ModificationListGenerator("CET", false);

		String modList = gen.generateModificationList(REVISIONS);
		Assert.assertEquals(readResourceString("/test-plain-modification-list-cet.txt"), modList);
	}

	@Test
	public void shouldGenerateHtml() {
		ModificationListGenerator gen = new ModificationListGenerator("UTC", true);

		String modList = gen.generateModificationList(REVISIONS);
		Assert.assertEquals(readResourceString("/test-plain-modification-list.html"), modList);
	}
}
