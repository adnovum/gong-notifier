package ch.adnovum.gong.notifier.util;

public class SecretDecryptException extends Exception {

	public SecretDecryptException(String message) {
		super(message);
	}

	public SecretDecryptException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecretDecryptException(Throwable cause) {
		super(cause);
	}
}
