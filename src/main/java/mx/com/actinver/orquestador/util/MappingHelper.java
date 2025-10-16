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
import java.util.StringTokenizer;

/**
 * Clase de apoyo para convetir:
 * <ol>
 * <li>Una cadena en formato JSON a Clase</li>
 * <li>Una Clase a cadena en formato JSON</li>
 * </ol>
 * <em></em>
 */
public class MappingHelper {

	private static final Logger LOG = LogManager.getLogger(MappingHelper.class);

	/**
	 * Permite convertir una Clase a una cada en formato JSON.
	 * 
	 * @param <T>  Tipo de entrada
	 * @param clss Clase a convertir
	 * @return Cadena en formato JSON
	 */
	public static <T> String toJson(T clss) {
		String rs = null;

		try {
			if (Objects.nonNull(clss)) {
				ObjectMapper mapper = new ObjectMapper();

				rs = mapper.writeValueAsString(clss);
			}
		} catch (JsonProcessingException e) {
			LOG.error("Error al convertir la Clase a JSON", e);

			throw new RuntimeException(e);
		}

		return rs;
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
		T rs = null;

		try {
			if (StringUtils.isNotBlank(json) && Objects.nonNull(clss)) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				
				rs = mapper.readValue(json, clss);
			}
		} catch (IOException e) {
			LOG.error("Error al convertir el JSON a Clase", e);

			throw new RuntimeException(e);
		}

		return rs;
	}

	/**
	 * Permite convertir un campo del JSON a una Clase. <br>
	 * <em>Nota 1: El campo debe estar en formato JSON.</em> <br>
	 * <em>Nota 2: Se puede indicar la ruta a un campo utilizando el "." como
	 * delimitador.<br>
	 * Ejemplo: <br>
	 * JSON = {"a": { "b": { "c": { "clave": 1, "valor": "Hola"}}}} <br>
	 * Ruta = a.b.c <br>
	 * Resultado = ClaseC[clave=1, valor="Hola"]</em>
	 * 
	 * @param <T>       Tipo de entrada y salida
	 * @param json      Cadena en formato JSON
	 * @param fieldName Campo del JSON
	 * @param clss      Clase de salida
	 * @return Clase de salida
	 */
	public static <T> T byFieldToClass(String json, String fieldName, Class<T> clss) {
		T rs = null;

		try {
			if (StringUtils.isNotBlank(json) && StringUtils.isNotBlank(fieldName) && Objects.nonNull(clss)) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(json);
				JsonNode node = null;

				if (fieldName.contains(".")) {
					StringTokenizer st = new StringTokenizer(fieldName, ".");

					while (st.hasMoreTokens()) {
						String field = st.nextToken();

						if (Objects.isNull(node)) {
							if (root.has(field)) {
								node = root.get(field);
							} else {
								node = null;

								break;
							}
						} else {
							if (node.has(field)) {
								node = node.get(field);
							} else {
								node = null;

								break;
							}
						}
					}
				} else {
					node = root.get(fieldName);
				}

				if (Objects.nonNull(node)) {
					rs = toClass(node.toString(), clss);
				}
			}
		} catch (IOException e) {
			LOG.error("Error al convertir un campo del JSON a Clase", e);
		}

		return rs;
	}

}
