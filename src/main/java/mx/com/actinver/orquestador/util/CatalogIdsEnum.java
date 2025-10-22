package mx.com.actinver.orquestador.util;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CatalogIdsEnum {

	SIN_VALIDEZ(47L, "SIN_VALIDEZ", "Sin Validez oficial"),
	CON_VALIDEZ(46, "CON_VALIDEZ", "Con Validez oficial"),
	EVIDENCIA_PRODUCCION(49L, "EVIDENCIA_PRODUCCION", "Archivos cargados durante generación de producción"),
	PORTAL(12L, "PORTAL", "Usuarios de portal"),
	ORIGENES_DE_DATOS(110L, "ORIGENES_DE_DATOS", "Origen de datos de archivo"),
	BANCO(10L, "BANCO", "Banco"),
	CANAL_DIGITAL(13L, "CANALES_DIGITAL", "Canales digitales que hacen solitudes al portal");


	private final long   id;
	private final String code;
	private final String description;

	CatalogIdsEnum(long id, String code, String description) {
		this.id          = id;
		this.code        = code;
		this.description = description;
	}

    /** Busca por id numérico (long). */
	public static CatalogIdsEnum getById(long id) {
		return Arrays.stream(values())
				.filter(e -> e.id == id)
				.findFirst().orElse(null);

	}

	/** Busca por código corto (case-insensitive). */
	public static CatalogIdsEnum getByCode(String code) {
		return Arrays.stream(values())
				.filter(e -> e.code.equalsIgnoreCase(code))
				.findFirst()
				.orElse(null);
	}
}
