package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClsFileHSM", propOrder = {
        "docID","docPID","tipoDocID","tipoDocIdGrupo","descripcion","consecutivo",
        "separador","ext","fechaDigitalizacion","arrayFile","createdBy"
})
public class ClsFileHSM {

    @XmlElement(name="DocID")
    protected Long docID;

    @XmlElement(name="DocPID")
    protected Long docPID;

    @XmlElement(name="TipoDocID")
    protected Integer tipoDocID;

    @XmlElement(name="TipoDocIdGrupo")
    protected Long tipoDocIdGrupo;

    @XmlElement(name="Descripcion")
    protected String descripcion;

    @XmlElement(name="Consecutivo")
    protected Integer consecutivo;

    @XmlElement(name="Separador")
    protected Boolean separador;

    @XmlElement(name="Ext")
    protected String ext;

    @XmlElement(name="FechaDigitalizacion")
    protected String fechaDigitalizacion;

    @XmlElement(name="ArrayFile")
    protected byte[] arrayFile;

    @XmlElement(name="CreatedBy")
    protected Long createdBy;

    // getters/setters
    public Long getDocID() { return docID; }
    public void setDocID(Long value) { this.docID = value; }

    public Long getDocPID() { return docPID; }
    public void setDocPID(Long value) { this.docPID = value; }

    public Integer getTipoDocID() { return tipoDocID; }
    public void setTipoDocID(Integer value) { this.tipoDocID = value; }

    public Long getTipoDocIdGrupo() { return tipoDocIdGrupo; }
    public void setTipoDocIdGrupo(Long value) { this.tipoDocIdGrupo = value; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }

    public Integer getConsecutivo() { return consecutivo; }
    public void setConsecutivo(Integer value) { this.consecutivo = value; }

    public Boolean isSeparador() { return separador; }
    public void setSeparador(Boolean value) { this.separador = value; }

    public String getExt() { return ext; }
    public void setExt(String value) { this.ext = value; }

    public String getFechaDigitalizacion() { return fechaDigitalizacion; }
    public void setFechaDigitalizacion(String value) { this.fechaDigitalizacion = value; }

    public byte[] getArrayFile() { return arrayFile; }
    public void setArrayFile(byte[] value) { this.arrayFile = value; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long value) { this.createdBy = value; }
}
