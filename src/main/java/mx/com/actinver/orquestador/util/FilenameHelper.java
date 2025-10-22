package mx.com.actinver.orquestador.util;

import org.springframework.util.StringUtils;

/**
 * Clase se apoyo para controlar el nombre de un archivo.
 */
public class FilenameHelper {

	/**
	 * Permite recuperar de una ruta solo el nombre del archivo sin extension. <br>
	 * "mypath/myfile.txt" -> "myfile".
	 * 
	 * @param path
	 * @return
	 */
	public static final String getFilename(String path) {
		return getFilenameWithoutExtension(getFilenameWithExtension(path));
	}

	/**
	 * Permite recuperar de una ruta solo la extension del archivo. <br>
	 * "mypath/myfile.txt" -> "txt".
	 * 
	 * @param path
	 * @return
	 */
	public static final String getExtension(String path) {
		return StringUtils.getFilenameExtension(StringUtils.cleanPath(path));
	}

	/**
	 * Permite recuperar de una ruta el nombre del archivo sin extension. <br>
	 * "mypath/myfile.txt" -> "mypath/myfile".
	 * 
	 * @param path
	 * @return
	 */
	public static final String getFilenameWithoutExtension(String path) {
		return StringUtils.stripFilenameExtension(StringUtils.cleanPath(path));
	}

	/**
	 * Permite recuperar de una ruta solo el nombre del archivo con extension. <br>
	 * "mypath/myfile.txt" -> "myfile.txt".
	 * 
	 * @param path
	 * @return Nombre del archivo con extension.
	 */
	public static final String getFilenameWithExtension(String path) {
		return StringUtils.getFilename(StringUtils.cleanPath(path));
	}

}

