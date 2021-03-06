package ch.adnovum.gong.notifier.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.thoughtworks.go.plugin.api.logging.Logger;

public class GongUtil {

	private static final Logger LOGGER = Logger.getLoggerFor(GongUtil.class);

	private GongUtil() {
		// Static utility class should not be instantiated.
	}

	public static String readResourceString(String resourceUrl, Class<?> clazz) {
		try (InputStream is = clazz.getResourceAsStream(resourceUrl)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int r;
			byte[] buf = new byte[4096];
			while ((r = is.read(buf)) > 0) {
				bos.write(buf, 0, r);
			}
			return new String(bos.toByteArray(), StandardCharsets.UTF_8).replaceAll("\r", "");
		}
		catch (IOException e) {
			LOGGER.error("Exception while loading resource " + resourceUrl + ": ", e);
			return "";
		}
	}

	public static String readResourceString(String resourceUrl) {
		return readResourceString(resourceUrl, GongUtil.class);
	}

	public static String escapeHtml(String str) {
		return str == null ? "" : str
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}
}
