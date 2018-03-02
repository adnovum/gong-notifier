package ch.adnovum.gong.notifier.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateHelper {

	private static final Pattern VAR_PATTERN = Pattern.compile("\\{([^}]*)\\}");

	public static String fillTemplate(String template, Map<String, Object> values) {
		if (template == null || template.isEmpty()) {
			return template;
		}

		Matcher m = VAR_PATTERN.matcher(template);
		int k = 0;
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			if (m.start() > k) {
				sb.append(template.substring(k, m.start()));
			}

			String key = m.group(1);
			Object val = values.get(key);
			if (val == null) {
				sb.append("{").append(key).append("}");
			}
			else {
				sb.append(val);
			}
			k = m.end();
		}
		if (k < template.length()) {
			sb.append(template.substring(k));
		}

		return sb.toString();
	}

}
