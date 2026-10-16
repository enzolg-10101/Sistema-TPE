package modelo;

public class Contrato {

    private Integer nro;
    private String feHrEnt;
    private String feHrDev;
    private String condUso;
    private String est;
    private Double impFianza; // Opcional
    private Double impAlq;
    private Double cargos; // Opcional
    private Integer idAsigna;
    private Integer idFirma;

    public Contrato() {
    }

    public Contrato(Integer nro) {
        this.nro = nro;
    }

    public Contrato(Integer nro, String feHrEnt, String feHrDev, String condUso, String est, 
                    Double impFianza, Double impAlq, Double cargos, Integer idAsigna, Integer idFirma) {
        this.nro = nro;
        this.feHrEnt = feHrEnt;
        this.feHrDev = feHrDev;
        this.condUso = condUso;
        this.est = est;
        this.impFianza = impFianza;
        this.impAlq = impAlq;
        this.cargos = cargos;
        this.idAsigna = idAsigna;
        this.idFirma = idFirma;
    }

    // Alias para compatibilidad con getId()
    public Integer getId() {
        return nro;
    }

    public void setId(Integer id) {
        this.nro = id;
    }

    public Integer getNro() {
        return nro;
    }

    public void setNro(Integer nro) {
        this.nro = nro;
    }

    public String getFeHrEnt() {
        return feHrEnt;
    }

    public void setFeHrEnt(String feHrEnt) {
        this.feHrEnt = feHrEnt;
    }

    public String getFeHrDev() {
        return feHrDev;
    }

    public void setFeHrDev(String feHrDev) {
        this.feHrDev = feHrDev;
    }

    public String getCondUso() {
        return condUso;
    }

    public void setCondUso(String condUso) {
        this.condUso = condUso;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }

    public Double getImpFianza() {
        return impFianza;
    }

    public void setImpFianza(Double impFianza) {
        this.impFianza = impFianza;
    }

    public Double getImpAlq() {
        return impAlq;
    }

    public void setImpAlq(Double impAlq) {
        this.impAlq = impAlq;
    }

    public Double getCargos() {
        return cargos;
    }

    public void setCargos(Double cargos) {
        this.cargos = cargos;
    }

    public Integer getIdAsigna() {
        return idAsigna;
    }

    public void setIdAsigna(Integer idAsigna) {
        this.idAsigna = idAsigna;
    }

    public Integer getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(Integer idFirma) {
        this.idFirma = idFirma;
    }
}
