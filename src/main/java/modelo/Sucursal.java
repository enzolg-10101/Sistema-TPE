package modelo;

public class Sucursal {

    private Integer cod;
    private String nom;
    private String dir;
    private Long tel;

    // Atributo transitorio solo para lectura/visualización en la interfaz
    private transient int cantidadVehiculos;

    public Sucursal() {
    }

    /**
     * Constructor sin ID (cod) para altas con AUTO_INCREMENT en la base de datos.
     */
    public Sucursal(String nom, String dir, Long tel) {
        this.nom = nom;
        this.dir = dir;
        this.tel = tel;
    }

    /**
     * Constructor completo para consultas de lectura desde la base de datos.
     */
    public Sucursal(Integer cod, String nom, String dir, Long tel) {
        this.cod = cod;
        this.nom = nom;
        this.dir = dir;
        this.tel = tel;
    }

    public Sucursal(Integer cod, String nom, String dir, Long tel, int cantidadVehiculos) {
        this.cod = cod;
        this.nom = nom;
        this.dir = dir;
        this.tel = tel;
        this.cantidadVehiculos = cantidadVehiculos;
    }

    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }

    public Integer getS_cod() {
        return cod;
    }

    public void setS_cod(Integer s_cod) {
        this.cod = s_cod;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getS_nom() {
        return nom;
    }

    public void setS_nom(String s_nom) {
        this.nom = s_nom;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getS_dir() {
        return dir;
    }

    public void setS_dir(String s_dir) {
        this.dir = s_dir;
    }

    public Long getTel() {
        return tel;
    }

    public void setTel(Long tel) {
        this.tel = tel;
    }

    public Long getS_tel() {
        return tel;
    }

    public void setS_tel(long s_tel) {
        this.tel = s_tel;
    }

    public void setS_tel(Long s_tel) {
        this.tel = s_tel;
    }

    public int getCantidadVehiculos() {
        return cantidadVehiculos;
    }

    public void setCantidadVehiculos(int cantidadVehiculos) {
        this.cantidadVehiculos = cantidadVehiculos;
    }

    @Override
    public String toString() {
        return cod + " - " + nom;
    }
}
