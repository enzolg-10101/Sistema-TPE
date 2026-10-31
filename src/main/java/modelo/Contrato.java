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

    /**
     * Constructor sin ID (nro) para altas con AUTO_INCREMENT en la base de datos.
     */
    public Contrato(String feHrEnt, String feHrDev, String condUso, String est,
            Double impFianza, Double impAlq, Double cargos, Integer idAsigna, Integer idFirma) {
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

    /**
     * Constructor completo para consultas de lectura desde la base de datos.
     */
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

    // Alias semántico para el ID del cliente firmante (id_Firma)
    public Integer getIdCliente() {
        return idFirma;
    }

    public void setIdCliente(Integer idCliente) {
        this.idFirma = idCliente;
    }

    // Atributos de presentación para la interfaz gráfica (TableView y panel de
    // detalles)
    private String clienteNombre;
    private Integer clienteDni;
    private String vehiculoModelo;
    private String vehiculoPatente;

    public String getClienteNombre() {
        return clienteNombre != null ? clienteNombre : (idFirma != null ? "Cliente #" + idFirma : "Sin asignar");
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public Integer getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(Integer clienteDni) {
        this.clienteDni = clienteDni;
    }

    public String getVehiculoModelo() {
        return vehiculoModelo != null ? vehiculoModelo : (idAsigna != null ? "Vehículo #" + idAsigna : "Sin asignar");
    }

    public void setVehiculoModelo(String vehiculoModelo) {
        this.vehiculoModelo = vehiculoModelo;
    }

    public String getVehiculoPatente() {
        return vehiculoPatente != null ? vehiculoPatente : "-";
    }

    public void setVehiculoPatente(String vehiculoPatente) {
        this.vehiculoPatente = vehiculoPatente;
    }
}
