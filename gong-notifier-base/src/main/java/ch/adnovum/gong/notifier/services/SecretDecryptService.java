package ch.adnovum.gong.notifier.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ch.adnovum.gong.notifier.util.SecretDecryptException;
import ch.adnovum.gong.notifier.util.SecretDecryptHelper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class SecretDecryptService {

	private final File cipherKeyFile;
	private byte[] cachedCipherKey;

	public SecretDecryptService(File cipherKeyFile) {
		this.cipherKeyFile = cipherKeyFile;
	}

	public String decrypt(String secret) throws SecretDecryptException {
		return SecretDecryptHelper.decrypt(secret, loadCipherKey());
	}

	private byte[] loadCipherKey() throws SecretDecryptException {
		if (cachedCipherKey == null) {
			try {
				String keyHex = new String(Files.readAllBytes(cipherKeyFile.toPath()), StandardCharsets.UTF_8);
				cachedCipherKey = Hex.decodeHex(keyHex);
			}
			catch (DecoderException | IOException e) {
				throw new SecretDecryptException(e);
			}

		}

		return cachedCipherKey;
	}

}
