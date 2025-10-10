package com.digipro.emulator.database;

import java.time.LocalDate;
import java.util.Date;

public class DocumentoDTO {
    public Integer idDocumento;
    public String  negocio;
    public Integer proyectoId;
    public Integer tipoDocId;
    public String  llaveBusqueda;
    public String  periodo;
    public String  contrato;
    public String  descripcion;
    public LocalDate fechaDocumento;
    public Date   fechaCreacion;
    public Integer idArchivo;
    public String  rutaRelativa;
    public String  nombreArchivo;
    public String  extension;
    public Long    tamanoBytes;
    public Integer consecutivo = 1;

    // === Getters auxiliares ===
    public long getIdDocumento() {
        return idDocumento != null ? idDocumento.longValue() : 0L;
    }

    public int getTipoDocId() {
        return tipoDocId != null ? tipoDocId.intValue() : 0;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getExtension() {
        return extension;
    }

    public String getRutaArchivo() {
        return rutaRelativa;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public int getConsecutivo() {
        return consecutivo != null ? consecutivo.intValue() : 1;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public String getContrato() {
        return contrato;
    }
}