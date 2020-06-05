package ch.adnovum.gong.notifier.util;

import static ch.adnovum.gong.notifier.TestConstants.KEY_HEX;
import static ch.adnovum.gong.notifier.TestConstants.VALID_SECRET_ENCRYPTED;
import static ch.adnovum.gong.notifier.TestConstants.VALID_SECRET_PLAIN;
import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;

public class SecretDecryptHelperTest {

	private static byte[] key;

	@BeforeClass
	public static void setupClass() throws DecoderException {
		key = Hex.decodeHex(KEY_HEX);
	}

	@Test
	public void shouldDecryptValidSecret() throws SecretDecryptException {
		String plain = SecretDecryptHelper.decrypt(VALID_SECRET_ENCRYPTED, key);
		assertEquals(VALID_SECRET_PLAIN, plain);
	}

	@Test(expected = SecretDecryptException.class)
	public void shouldFailForInvalidSecret() throws SecretDecryptException {
		SecretDecryptHelper.decrypt("bla bla bla", key);
	}
}
