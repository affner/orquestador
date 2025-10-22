package mx.com.actinver.orquestador.util;

import java.util.Arrays;

public enum StatusEnum {

	UNDEFINED(-1), DISABLED(0), ENABLED(1);

	private int value;

	private StatusEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static StatusEnum getByValue(int value) {
		return Arrays.asList(values()).stream()
				.filter(e -> e.getValue() == value)
				.findFirst().orElse(UNDEFINED);
	}

}
