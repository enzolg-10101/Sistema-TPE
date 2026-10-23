package modelo;

public class Mantenimiento {

    private Integer id;
    private String fec;
    private TipoMantenimiento tipo;
    private Integer kmj;
    private Double costo;
    private String obs;
    private Integer idRealiza;   // FK -> Empleados(E_id)
    private Integer idRealizado; // FK -> Vehículos(V_id)

    public Mantenimiento() {
    }

    public Mantenimiento(Integer id, String fec, TipoMantenimiento tipo, Integer kmj,
                         Double costo, String obs, Integer idRealiza, Integer idRealizado) {
        this.id = id;
        this.fec = fec;
        this.tipo = tipo;
        this.kmj = kmj;
        this.costo = costo;
        this.obs = obs;
        this.idRealiza = idRealiza;
        this.idRealizado = idRealizado;
    }

    public Mantenimiento(Integer id, String fec, String tipo, Integer kmj,
                         Double costo, String obs, Integer idRealiza, Integer idRealizado) {
        this(id, fec, TipoMantenimiento.fromString(tipo), kmj, costo, obs, idRealiza, idRealizado);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getM_id() {
        return id;
    }

    public void setM_id(Integer m_id) {
        this.id = m_id;
    }

    public String getFec() {
        return fec;
    }

    public void setFec(String fec) {
        this.fec = fec;
    }

    public String getM_fec() {
        return fec;
    }

    public void setM_fec(String m_fec) {
        this.fec = m_fec;
    }

    public TipoMantenimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMantenimiento tipo) {
        this.tipo = tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = TipoMantenimiento.fromString(tipo);
    }

    public String getM_tipo() {
        return tipo != null ? tipo.getDescripcion() : null;
    }

    public void setM_tipo(String m_tipo) {
        this.tipo = TipoMantenimiento.fromString(m_tipo);
    }

    public void setM_tipo(TipoMantenimiento m_tipo) {
        this.tipo = m_tipo;
    }

    public Integer getKmj() {
        return kmj;
    }

    public void setKmj(Integer kmj) {
        this.kmj = kmj;
    }

    public Integer getM_kmj() {
        return kmj;
    }

    public void setM_kmj(Integer m_kmj) {
        this.kmj = m_kmj;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public Double getM_costo() {
        return costo;
    }

    public void setM_costo(double m_costo) {
        this.costo = m_costo;
    }

    public void setM_costo(Double m_costo) {
        this.costo = m_costo;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getM_obs() {
        return obs;
    }

    public void setM_obs(String m_obs) {
        this.obs = m_obs;
    }

    public Integer getIdRealiza() {
        return idRealiza;
    }

    public void setIdRealiza(Integer idRealiza) {
        this.idRealiza = idRealiza;
    }

    public Integer getId_Realiza() {
        return idRealiza;
    }

    public void setId_Realiza(Integer id_Realiza) {
        this.idRealiza = id_Realiza;
    }

    public Integer getIdRealizado() {
        return idRealizado;
    }

    public void setIdRealizado(Integer idRealizado) {
        this.idRealizado = idRealizado;
    }

    public Integer getId_Realizado() {
        return idRealizado;
    }

    public void setId_Realizado(Integer id_Realizado) {
        this.idRealizado = id_Realizado;
    }

    @Override
    public String toString() {
        return "Mantenimiento{" +
                "id=" + id +
                ", fec='" + fec + '\'' +
                ", tipo=" + tipo +
                ", kmj=" + kmj +
                ", costo=" + costo +
                ", obs='" + obs + '\'' +
                ", idRealiza=" + idRealiza +
                ", idRealizado=" + idRealizado +
                '}';
    }
}
