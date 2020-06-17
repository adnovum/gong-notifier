package ch.adnovum.gong.notifier.services;

import static ch.adnovum.gong.notifier.TestConstants.KEY_HEX;
import static ch.adnovum.gong.notifier.TestConstants.VALID_SECRET_ENCRYPTED;
import static ch.adnovum.gong.notifier.TestConstants.VALID_SECRET_PLAIN;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ch.adnovum.gong.notifier.util.SecretDecryptException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SecretDecryptServiceTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private SecretDecryptService decryptService;

	@Before
	public void setup() throws IOException {
		File cipherKeyFile = tmp.newFile();
		Files.write(cipherKeyFile.toPath(), KEY_HEX.getBytes(StandardCharsets.UTF_8));
		decryptService = new SecretDecryptService(cipherKeyFile);
	}

	@Test
	public void shouldDecryptWithValidKeyFile() throws SecretDecryptException {
		String plain = decryptService.decrypt(VALID_SECRET_ENCRYPTED);
		assertEquals(VALID_SECRET_PLAIN, plain);
	}

	@Test(expected = SecretDecryptException.class)
	public void shouldFailWithNonExistentKeyFile() throws SecretDecryptException {
		decryptService = new SecretDecryptService(new File("/does/not/exist"));

		decryptService.decrypt(VALID_SECRET_ENCRYPTED);
	}
}
