package modelo;

public class Sucursal {

    private Integer cod;
    private String nom;
    private String dir;
    private Long tel;

    public Sucursal() {
    }

    public Sucursal(Integer cod, String nom, String dir, Long tel) {
        this.cod = cod;
        this.nom = nom;
        this.dir = dir;
        this.tel = tel;
    }

    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    @Override
    public String toString() {
        return "Sucursal{" +
                "cod=" + cod +
                ", nom='" + nom + '\'' +
                ", dir='" + dir + '\'' +
                ", tel=" + tel +
                '}';
    }
}
