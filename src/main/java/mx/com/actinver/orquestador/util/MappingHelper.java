package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase de apoyo para convertir:
 * <ol>
 * <li>Una cadena en formato JSON a Clase</li>
 * <li>Una Clase a cadena en formato JSON</li>
 * </ol>
 */
public final class MappingHelper {

	private static final Logger LOG = LogManager.getLogger(MappingHelper.class);
	private static final ObjectMapper MAPPER = createMapper();

	private MappingHelper() {
		// Utility class
	}

	private static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return mapper;
	}

	/**
	 * Permite convertir una Clase a una cadena en formato JSON.
	 *
	 * @param <T>  Tipo de entrada
	 * @param clss Clase a convertir
	 * @return Cadena en formato JSON
	 */
	public static <T> String toJson(T clss) {
		if (Objects.isNull(clss)) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(clss);
		} catch (JsonProcessingException e) {
			LOG.error("Error al convertir la Clase a JSON", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Permite convertir una cadena en formato JSON a una Clase.
	 *
	 * @param <T>  Tipo de entrada y salida
	 * @param json Cadena en formato JSON
	 * @param clss Clase de salida
	 * @return Clase de salida
	 */
	public static <T> T toClass(String json, Class<T> clss) {
		if (StringUtils.isBlank(json) || Objects.isNull(clss)) {
			return null;
		}
		try {
			return MAPPER.readValue(json, clss);
		} catch (IOException e) {
			LOG.error("Error al convertir el JSON a Clase", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Permite convertir un campo del JSON a una Clase.
	 *
	 * <p>Se puede indicar la ruta a un campo utilizando el "." como delimitador.
	 * Ejemplo:
	 * JSON = {"a": { "b": { "c": { "clave": 1, "valor": "Hola"}}}}
	 * Ruta = a.b.c
	 * Resultado = ClaseC[clave=1, valor="Hola"]</p>
	 *
	 * @param <T>       Tipo de entrada y salida
	 * @param json      Cadena en formato JSON
	 * @param fieldName Campo del JSON
	 * @param clss      Clase de salida
	 * @return Clase de salida o {@code null} si el campo no existe
	 */
	public static <T> T byFieldToClass(String json, String fieldName, Class<T> clss) {
		if (StringUtils.isBlank(json) || StringUtils.isBlank(fieldName) || Objects.isNull(clss)) {
			return null;
		}

		try {
			JsonNode node = MAPPER.readTree(json);
			for (String token : fieldName.split("\\.")) {
				if (node == null || node.isMissingNode()) {
					return null;
				}
				node = node.get(token);
			}

			if (node == null || node.isMissingNode() || node.isNull()) {
				return null;
			}

			return MAPPER.treeToValue(node, clss);
		} catch (IOException e) {
			LOG.error("Error al convertir un campo del JSON a Clase", e);
			return null;
		}
	}
}