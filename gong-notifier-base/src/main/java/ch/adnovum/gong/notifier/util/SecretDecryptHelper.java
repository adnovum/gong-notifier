package ch.adnovum.gong.notifier.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretDecryptHelper {

	private static final Base64.Decoder B64_DECODER = Base64.getDecoder();
	private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";


	public static String decrypt(String secret, byte[] key) throws SecretDecryptException {
		try {
			// Format: AES:Base64(IV):Base64(CIPHER)
			String[] parts = secret.split(":");

			String encodedIv = parts[1];
			String encodedCipher = parts[2];
			byte[] iv = B64_DECODER.decode(encodedIv);
			byte[] cipher = B64_DECODER.decode(encodedCipher);

			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher decryptCipher = Cipher.getInstance(CIPHER_ALGO);
			decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			byte[] plainBytes = decryptCipher.doFinal(cipher);
			return new String(plainBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new SecretDecryptException("Could not decrypt secret " + secret, e);
		}
	}

}
