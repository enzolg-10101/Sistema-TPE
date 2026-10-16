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

    public Cliente() {
    }

    public Cliente(Integer id) {
        this.id = id;
    }

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
}
