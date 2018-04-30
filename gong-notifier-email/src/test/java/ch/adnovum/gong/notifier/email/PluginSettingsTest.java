package ch.adnovum.gong.notifier.email;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class PluginSettingsTest {

	/**
	 * It's important that changes in the settings can be detected so the plugin can reinitialize itself.
	 */
	@Test
	public void shouldCheckAllFieldsInEqualsAndHashCode() throws Exception {
		PluginSettings base = new PluginSettings();

		setters(PluginSettings.class).forEach(m -> {
			PluginSettings settings = new PluginSettings();
			Class<?> paramType = m.getParameterTypes()[0];
			try {
				m.invoke(settings, createValue(paramType));
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Could not change plugin settings");
			}

			if (base.equals(settings)) {
				Assert.fail("The equals method does not appear to use the field for setter " + m.getName());
			}
			if (base.hashCode() == settings.hashCode()) {
				Assert.fail("The hashCode method does not appear to use the field for setter " + m.getName());
			}
		});
	}

	private static Stream<Method> setters(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		return Arrays.stream(methods)
				.filter(m -> m.getName().startsWith("set"));
	}

	@SuppressWarnings("unchecked")
	private static <T> T createValue(Class<T> type) {
		if (type.equals(String.class)) {
			return (T) "str";
		}
		else if (type.equals(Integer.class)) {
			return (T) Integer.valueOf(77);
		}
		else {
			throw new UnsupportedOperationException("Unsupported param type " + type);
		}
	}
}
