package modelo;

public class Vehiculo {

    private Integer id;
    private String pat;
    private String marca;
    private String mod;
    private Integer aFabr;
    private String fAdq;
    private Integer kmj;
    private String est;
    private Double preAdq;
    private String fBaja; // Opcional
    private String tiComb;
    private Double carga;
    private Integer pax;
    private Double tarBase;
    private String cat;
    private Integer codPertenece;

    public Vehiculo() {
    }

    public Vehiculo(Integer id) {
        this.id = id;
    }

    /**
     * Constructor sin ID para altas con AUTO_INCREMENT en la base de datos.
     */
    public Vehiculo(String pat, String marca, String mod, Integer aFabr, String fAdq,
                    Integer kmj, String est, Double preAdq, String fBaja, String tiComb,
                    Double carga, Integer pax, Double tarBase, String cat, Integer codPertenece) {
        this.pat = pat;
        this.marca = marca;
        this.mod = mod;
        this.aFabr = aFabr;
        this.fAdq = fAdq;
        this.kmj = kmj;
        this.est = est;
        this.preAdq = preAdq;
        this.fBaja = fBaja;
        this.tiComb = tiComb;
        this.carga = carga;
        this.pax = pax;
        this.tarBase = tarBase;
        this.cat = cat;
        this.codPertenece = codPertenece;
    }

    /**
     * Constructor completo para consultas de lectura desde la base de datos.
     */
    public Vehiculo(Integer id, String pat, String marca, String mod, Integer aFabr, String fAdq, 
                    Integer kmj, String est, Double preAdq, String fBaja, String tiComb, 
                    Double carga, Integer pax, Double tarBase, String cat, Integer codPertenece) {
        this.id = id;
        this.pat = pat;
        this.marca = marca;
        this.mod = mod;
        this.aFabr = aFabr;
        this.fAdq = fAdq;
        this.kmj = kmj;
        this.est = est;
        this.preAdq = preAdq;
        this.fBaja = fBaja;
        this.tiComb = tiComb;
        this.carga = carga;
        this.pax = pax;
        this.tarBase = tarBase;
        this.cat = cat;
        this.codPertenece = codPertenece;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPat() {
        return pat;
    }

    public void setPat(String pat) {
        this.pat = pat;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public Integer getaFabr() {
        return aFabr;
    }

    public void setaFabr(Integer aFabr) {
        this.aFabr = aFabr;
    }

    public String getfAdq() {
        return fAdq;
    }

    public void setfAdq(String fAdq) {
        this.fAdq = fAdq;
    }

    public Integer getKmj() {
        return kmj;
    }

    public void setKmj(Integer kmj) {
        this.kmj = kmj;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }

    public Double getPreAdq() {
        return preAdq;
    }

    public void setPreAdq(Double preAdq) {
        this.preAdq = preAdq;
    }

    public String getfBaja() {
        return fBaja;
    }

    public void setfBaja(String fBaja) {
        this.fBaja = fBaja;
    }

    public String getTiComb() {
        return tiComb;
    }

    public void setTiComb(String tiComb) {
        this.tiComb = tiComb;
    }

    public Double getCarga() {
        return carga;
    }

    public void setCarga(Double carga) {
        this.carga = carga;
    }

    public Integer getPax() {
        return pax;
    }

    public void setPax(Integer pax) {
        this.pax = pax;
    }

    public Double getTarBase() {
        return tarBase;
    }

    public void setTarBase(Double tarBase) {
        this.tarBase = tarBase;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public Integer getCodPertenece() {
        return codPertenece;
    }

    public void setCodPertenece(Integer codPertenece) {
        this.codPertenece = codPertenece;
    }
}
