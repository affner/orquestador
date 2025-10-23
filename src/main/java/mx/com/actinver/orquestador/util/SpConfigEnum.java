package mx.com.actinver.orquestador.util;

import lombok.Getter;

/**
 * Configuraciones para los procedimientos almacenados.
 * <ul>
 * <li>Sentencia para el llamado del Procedimiento.</li>
 * <li>Bandera para integrar la paginacion del Procedimiento.</li>
 * </ul>
 */
@Getter
public enum SpConfigEnum {

    /**
     * Procedimiento para el mantenimiento de los catalogos del portal.
     */
    CATALOGS_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDCATALOG01.SP_CRUDCATALOG01(?, ?, ?, ?, ?, ?) }"),
    /** Procedimiento para el mantenimiento de usuarios del portal. */
    USERS_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDUSERS01.SP_CRUDUSERS01(?, ?, ?, ?, ?, ?) }"),
    /** Procedimiento para el mantenimiento de asignacion de roles a un usuario. */
    USER_ROLES_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDUSERS_R01.SP_CRUDUSERS_R01(?, ?, ?, ?, ?, ?) }"),
    /** Procedimiento para consultar los modulos permitidos a un usuario. */
    USER_PERMISSIONS("{ CALL USR_EXS_PORTAL.PKG_PORTAL.SP_USER_BOARD(?, ?, ?, ?) }"),
    /**
     * Procedimiento para el mantenimiento de las credenciales de los
     * usuarios/canales.
     */
    USER_CREDENTIALS_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDUSERS_AUTH01.SP_CRUDUSERS_AUTH01(?, ?, ?, ?, ?, ?) }"),
    /** Procedimiento para el mantenimiento de las producciones. */
    PRODUCTIONS_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDPRODUCTION01.SP_CRUDPRODUCTION01(?, ?, ?, ?, ?, ?) }"),
    /**
     * Procedimiento para consultar los contadores de las producciones.
     */
    PRODUCTION_COUNTERS("{ CALL USR_EXS_PORTAL.PKG_PORTAL.SP_PROD_LIST_HEAD(?, ?, ?, ?, ?) }"),
    /**
     * Procedimiento para consultar los registros de las producciones.
     */
    PRODUCTION_RECORDS("{ CALL USR_EXS_PORTAL.PKG_PORTAL.SP_PROD_LIST(?, ?, ?, ?, ?, ?) }", true),
    /** Procedimiento para el mantenimiento de las estapas de una produccion. */
    PRODUCTION_STAGES_CRUD("{ CALL USR_EXS_PORTAL.PKG_PRODSTAGES01.SP_PRODSTAGES01(?, ?, ?, ?, ?, ?) }"),
    /**
     * Procedimiento para consultar las estapas aplicables a una produccion.
     */
    PRODUCTION_STAGES_APPLICABLE("{ CALL USR_EXS_PORTAL.PKG_PORTAL.SP_STAGESLIST(?, ?, ?, ?, ?) }"),
    /** Procedimiento para el mantenimiento de los archivos. */
    FILES_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDFILES01.SP_CRUDFILES01(?, ?, ?, ?, ?, ?) }"),

    /** Procedimiento para proceso posterior a la carga masiva en la tabla PROD_TMP. para el paso 3 */
    SP_DRIVER("{ CALL USR_EXS_TEMP.PKG_TMP_LOAD.SP_DRIVER(?, ?, ?, ?, ?) }"),
    /** Segundo Procedimiento para proceso posterior a la carga masiva en la tabla PROD_TMP. para el paso 3 */
    SP_PROD_LOAD("{ CALL USR_EXS_TEMP.PKG_PROD_TEMP.SP_PROD_LOAD(?, ?, ?, ?, ?) }"),

    /** Procedimiento para el mantenimiento de las producciones de la tabla temporal. */
    PRODUCTIONS_CRUD02("{ CALL USR_EXS_TEMP.PKG_CRUDPRODUCTION02.SP_CRUDPRODUCTION02(?, ?, ?, ?, ?, ?) }"),

    /** Procedimiento para proceso posterior a la carga masiva en la tabla PROD_TMP para el paso 2. */
    SP_PARTIAL_PROD("{ CALL USR_EXS_TEMP.PKG_TMP_LOAD.SP_PARTIAL_PROD(?, ?, ?, ?, ?) }"),

    /** Procedimiento  para el paso 7. */
    PROD_RELEASE_MAIN_TRANSFER("{ CALL USR_EXS_PORTAL.PKG_PROD_RELEASE.SP_PROD_TRANSFER(?, ?, ?, ?, ?) }"),

    /** Procedimiento para finalizar el proceso de orquestador y borrar la tabla temporal para el paso 8. */
    SP_STAMP("{ CALL USR_EXS_TEMP.PKG_PROD_TEMP.SP_XSA_LOAD (?, ?, ?, ?, ?) }"),

    /** Procedimiento para el mantenimiento de progreso de processos */
    PROCESS_PROGRESSES_CRUD("{ CALL USR_EXS_PORTAL.PKG_CRUDPROG01.SP_CRUDPROG01(?, ?, ?, ?, ?, ?) }"),

    /** Procedimiento para finalizar y aplicar excepciones de la prooduccion para el paso 11. */
    SP_ACTIVATE_PROD("{ CALL USR_EXS_PORTAL.PKG_PROD_RELEASE.SP_ACTIVATE_PROD (?, ?, ?, ?, ?) }"),

    /**
     * Procedimiento para encontrar la informacion del estado de cuenta por contrato/credito.
     */
    ACC_STMT_FINDER("{ CALL USR_EXS_PORTAL.PKG_REPORTS.SP_RPT_EDC_H(?, ?, ?, ?, ?)}"),

    SP_AUDIT_LOG("{ CALL USR_EXS_PORTAL.PKG_CRUDREQLOG01.SP_CRUDREQLOG01(?, ?, ?, ?, ?, ?)}");

    /**
     * Sentencia para el llamado del Procedimiento.
     */
    private String stmt;

    /**
     * Bandera para integrar la paginacion del Procedimiento. <br>
     * <em>Nota: Debe activarse la bandera cuando la paginacion fue integrada al
     * Procedimiento.</em>
     */
    private boolean pageable;

    /**
     *
     * @param stmt Sentencia para el llamado del Procedimiento.
     */
    private SpConfigEnum(String stmt) {
        this.stmt = stmt;
    }

    /**
     * <em>Nota: Debe activarse la bandera de paginacion, cuando la paginacion fue
     * integrada al Procedimiento.</em>
     *
     * @param stmt     Sentencia para el llamado del Procedimiento.
     * @param pageable Bandera para integrar la paginacion del Procedimiento.
     */
    private SpConfigEnum(String stmt, boolean pageable) {
        this.stmt = stmt;
        this.pageable = pageable;
    }

}
