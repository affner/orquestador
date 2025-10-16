package mx.com.actinver.orquestador.util;

/**
 * Estados y tipos de registro utilizados en la tabla temporal de producciones.
 * Los nombres de los enums coinciden con los valores almacenados en la base de datos.
 */
public enum ProdStatus {
    TA_TIMBRADO,
    TIMBRADO,
    PENDIENTE,
    ERROR,
    REMANENTE
}