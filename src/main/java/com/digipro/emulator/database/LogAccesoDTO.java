package com.digipro.emulator.database;

import java.time.OffsetDateTime;

public class LogAccesoDTO {
    public Integer idLog;
    public String  idTicket;
    public Integer idDocumento;
    public String  operacion;       // "ObtenLogin", "ContestaExpedientexLlave", "ContestaFileHSM"
    public String  llaveBusqueda;
    public OffsetDateTime fechaHora;
    public String  ipOrigen;
    public boolean exitoso;
    public String  mensajeError;
}
