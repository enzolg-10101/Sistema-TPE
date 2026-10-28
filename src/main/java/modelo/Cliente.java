package modelo;

public class Cliente {

    private Integer id;
    private Integer dni;
    private String nom;
    private String apl;
    private String dir;
    private Long tel;
    private String correo;
    private String fAlta;

    // Atributo transitorio de lectura/interfaz para mostrar el estado en TableView
    private String estado = "Activo";

    public Cliente() {
    }

    public Cliente(Integer id) {
        this.id = id;
    }

    /**
     * Constructor sin ID para altas con AUTO_INCREMENT en la base de datos.
     */
    public Cliente(Integer dni, String nom, String apl, String dir, Long tel, String correo, String fAlta) {
        this.dni = dni;
        this.nom = nom;
        this.apl = apl;
        this.dir = dir;
        this.tel = tel;
        this.correo = correo;
        this.fAlta = fAlta;
    }

    /**
     * Constructor completo para consultas de lectura desde la base de datos.
     */
    public Cliente(Integer id, Integer dni, String nom, String apl, String dir, Long tel, String correo, String fAlta) {
        this.id = id;
        this.dni = dni;
        this.nom = nom;
        this.apl = apl;
        this.dir = dir;
        this.tel = tel;
        this.correo = correo;
        this.fAlta = fAlta;
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

    public String getfAlta() {
        return fAlta;
    }

    public void setfAlta(String fAlta) {
        this.fAlta = fAlta;
    }

    public String getFAlta() {
        return fAlta;
    }

    public void setFAlta(String fAlta) {
        this.fAlta = fAlta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
