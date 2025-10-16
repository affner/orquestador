package mx.com.actinver.common.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;

public class ULID {

	public static final String ENCODING_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final int ENCODING_LENGTH = ENCODING_CHARS.length();

	public static final int TIMESTAMP_LENGTH = 10;

	public static final int RANDOMNESS_LENGTH = 16;

	public static final int ULID_LENGTH = TIMESTAMP_LENGTH + RANDOMNESS_LENGTH;

	private static final Random RANDOM = new SecureRandom();

	public static String randomULID() {
		StringBuilder sb = new StringBuilder(ULID_LENGTH);

		// Generar timestamp
		long timestamp = Instant.now().toEpochMilli();
		for (int i = TIMESTAMP_LENGTH - 1; i >= 0; i--) {
			sb.append(ENCODING_CHARS.charAt((int) (timestamp % ENCODING_LENGTH)));
			timestamp /= ENCODING_LENGTH;
		}

		// Generar parte aleatoria
		byte[] randomness = new byte[RANDOMNESS_LENGTH / 2];
		RANDOM.nextBytes(randomness);
		for (byte b : randomness) {
			sb.append(ENCODING_CHARS.charAt((b & 0xF0) >> 4));
			sb.append(ENCODING_CHARS.charAt(b & 0x0F));
		}

		return sb.toString();
	}
}
