package ch.adnovum.gong.notifier.email;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateHelper {

	public static String fillTemplate(String template, Map<String, Object> values) {

		Pattern p = Pattern.compile("\\{([^}]*)\\}");
		Matcher m = p.matcher(template);
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

	public static void main(String[] args) {
		Map<String, Object> m = new HashMap<>();
		m.put("pipeline", "gagag");
		m.put("stage", "oyoyoy");
		System.out.println(fillTemplate("hello {pipeline}, stage {stage}!", m));
	}

}
