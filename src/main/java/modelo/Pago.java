package modelo;

public class Pago {

    private Integer id;
    private String fec;
    private Double imp;
    private MedioPago medio;
    private Integer nroRegistra; // FK -> Contratos(Co_nro)

    public Pago() {
    }

    public Pago(Integer id, String fec, Double imp, MedioPago medio, Integer nroRegistra) {
        this.id = id;
        this.fec = fec;
        this.imp = imp;
        this.medio = medio;
        this.nroRegistra = nroRegistra;
    }

    public Pago(Integer id, String fec, Double imp, String medio, Integer nroRegistra) {
        this(id, fec, imp, MedioPago.fromString(medio), nroRegistra);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getP_id() {
        return id;
    }

    public void setP_id(Integer p_id) {
        this.id = p_id;
    }

    public String getFec() {
        return fec;
    }

    public void setFec(String fec) {
        this.fec = fec;
    }

    public String getP_fec() {
        return fec;
    }

    public void setP_fec(String p_fec) {
        this.fec = p_fec;
    }

    public Double getImp() {
        return imp;
    }

    public void setImp(Double imp) {
        this.imp = imp;
    }

    public Double getP_imp() {
        return imp;
    }

    public void setP_imp(double p_imp) {
        this.imp = p_imp;
    }

    public void setP_imp(Double p_imp) {
        this.imp = p_imp;
    }

    public MedioPago getMedio() {
        return medio;
    }

    public void setMedio(MedioPago medio) {
        this.medio = medio;
    }

    public void setMedio(String medio) {
        this.medio = MedioPago.fromString(medio);
    }

    public String getP_medio() {
        return medio != null ? medio.getDescripcion() : null;
    }

    public void setP_medio(String p_medio) {
        this.medio = MedioPago.fromString(p_medio);
    }

    public void setP_medio(MedioPago p_medio) {
        this.medio = p_medio;
    }

    public Integer getNroRegistra() {
        return nroRegistra;
    }

    public void setNroRegistra(Integer nroRegistra) {
        this.nroRegistra = nroRegistra;
    }

    public Integer getNro_Registra() {
        return nroRegistra;
    }

    public void setNro_Registra(Integer nro_Registra) {
        this.nroRegistra = nro_Registra;
    }

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", fec='" + fec + '\'' +
                ", imp=" + imp +
                ", medio=" + medio +
                ", nroRegistra=" + nroRegistra +
                '}';
    }
}
