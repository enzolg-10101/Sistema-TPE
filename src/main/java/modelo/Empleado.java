package modelo;

public class Empleado {

    private Integer id;
    private Integer dni; // Unique
    private String nom;
    private String apl;
    private String dir;
    private Long tel;
    private String correo;
    private String fIng;
    private Integer codTrabaja; // FK a Sucursal(cod)

    public Empleado() {
    }

    /**
     * Constructor sin ID para altas con AUTO_INCREMENT en la base de datos.
     */
    public Empleado(Integer dni, String nom, String apl, String dir, Long tel, String correo, String fIng, Integer codTrabaja) {
        this.dni = dni;
        this.nom = nom;
        this.apl = apl;
        this.dir = dir;
        this.tel = tel;
        this.correo = correo;
        this.fIng = fIng;
        this.codTrabaja = codTrabaja;
    }

    /**
     * Constructor completo para consultas de lectura desde la base de datos.
     */
    public Empleado(Integer id, Integer dni, String nom, String apl, String dir, Long tel, String correo, String fIng, Integer codTrabaja) {
        this.id = id;
        this.dni = dni;
        this.nom = nom;
        this.apl = apl;
        this.dir = dir;
        this.tel = tel;
        this.correo = correo;
        this.fIng = fIng;
        this.codTrabaja = codTrabaja;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getApl() {
        return apl;
    }

    public void setApl(String apl) {
        this.apl = apl;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Long getTel() {
        return tel;
    }

    public void setTel(Long tel) {
        this.tel = tel;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getfIng() {
        return fIng;
    }

    public void setfIng(String fIng) {
        this.fIng = fIng;
    }

    public Integer getCodTrabaja() {
        return codTrabaja;
    }

    public void setCodTrabaja(Integer codTrabaja) {
        this.codTrabaja = codTrabaja;
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "id=" + id +
                ", dni=" + dni +
                ", nom='" + nom + '\'' +
                ", apl='" + apl + '\'' +
                ", dir='" + dir + '\'' +
                ", tel=" + tel +
                ", correo='" + correo + '\'' +
                ", fIng='" + fIng + '\'' +
                ", codTrabaja=" + codTrabaja +
                '}';
    }
}
