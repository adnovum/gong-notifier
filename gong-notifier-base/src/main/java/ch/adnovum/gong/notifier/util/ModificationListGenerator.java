package ch.adnovum.gong.notifier.util;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import ch.adnovum.gong.notifier.go.api.PipelineHistory;

public class ModificationListGenerator {

	private static final String NL = "\n";

	private static final String HTML_NL = "\n<br/>";

	private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private String timezone;
	private boolean html;

	public ModificationListGenerator(String timezone, boolean html) {
		this.timezone = timezone;
		this.html = html;
	}

	public String generateModificationList(List<PipelineHistory.MaterialRevision> revisions) {
		final SimpleDateFormat tmFmt = new SimpleDateFormat(TIME_FORMAT);
		if (timezone != null) {
			tmFmt.setTimeZone(TimeZone.getTimeZone(timezone));
		}

		final String nl = html ? HTML_NL : NL;

		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (PipelineHistory.MaterialRevision matRev: revisions) {
			for (PipelineHistory.Modification mod: matRev.modifications) {
				if (!first) {
					sb.append(nl);
				}
				first = false;
				sb
						.append(matRev.material.type + ": "  + escapeHtml(matRev.material.description)).append(nl)
						.append("revision: " + escapeHtml(mod.revision)).append(", modified by " + escapeHtml(mod.userName))
						.append(" on " + tmFmt.format(mod.getModifiedTime())).append(nl)
						.append(escapeHtml(mod.comment).replaceAll("\n", nl))
						.append(nl);
			}
		}
		return sb.toString();
	}

	private String escapeHtml(String str) {
		return html ? GongUtil.escapeHtml(str) : str;
	}
}
