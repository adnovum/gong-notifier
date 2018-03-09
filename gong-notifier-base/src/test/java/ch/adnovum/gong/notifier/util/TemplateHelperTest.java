package ch.adnovum.gong.notifier.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TemplateHelperTest {


	@Test
	public void shouldReplaceVariables() {
		Map<String, Object> vals = new HashMap<>();
		vals.put("who", "world");
		vals.put("age", 21);
		String str = TemplateHelper.fillTemplate("hello {who}! I'm {age} years old.", vals);
		assertEquals("hello world! I'm 21 years old.", str);
	}

	@Test
	public void shouldIgnoreUnknownVariables() {
		Map<String, Object> vals = new HashMap<>();
		vals.put("who", "world");
		String str = TemplateHelper.fillTemplate("hello {notvalidvariable}", vals);
		assertEquals("hello {notvalidvariable}", str);
	}

	@Test
	public void shouldIgnoreHalfOpenBrackets() {
		Map<String, Object> vals = new HashMap<>();
		vals.put("who", "world");
		String str = TemplateHelper.fillTemplate("hello {who", vals);
		assertEquals("hello {who", str);
	}

	@Test
	public void shouldHandleEmptyInputs() {
		Map<String, Object> vals = new HashMap<>();
		vals.put("who", "world");
		assertEquals(null, TemplateHelper.fillTemplate(null, vals));
	}
}
