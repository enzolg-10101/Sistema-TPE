package modelo;

public class CheckInOut {

    private Integer id;
    private String feHrSal;
    private Integer kmjSal;
    private Double nivCombSal;
    private String daExist;
    private String feHrEnt; // Opcional
    private Integer kmjEnt; // Opcional
    private Double nivCombEnt; // Opcional
    private String daNuevos; // Opcional
    private Integer nroParticipa;
    private Integer nroCorresponde;

    public CheckInOut() {
    }

    public CheckInOut(Integer id) {
        this.id = id;
    }

    public CheckInOut(Integer id, String feHrSal, Integer kmjSal, Double nivCombSal, String daExist, 
                      String feHrEnt, Integer kmjEnt, Double nivCombEnt, String daNuevos, 
                      Integer nroParticipa, Integer nroCorresponde) {
        this.id = id;
        this.feHrSal = feHrSal;
        this.kmjSal = kmjSal;
        this.nivCombSal = nivCombSal;
        this.daExist = daExist;
        this.feHrEnt = feHrEnt;
        this.kmjEnt = kmjEnt;
        this.nivCombEnt = nivCombEnt;
        this.daNuevos = daNuevos;
        this.nroParticipa = nroParticipa;
        this.nroCorresponde = nroCorresponde;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFeHrSal() {
        return feHrSal;
    }

    public void setFeHrSal(String feHrSal) {
        this.feHrSal = feHrSal;
    }

    public Integer getKmjSal() {
        return kmjSal;
    }

    public void setKmjSal(Integer kmjSal) {
        this.kmjSal = kmjSal;
    }

    public Double getNivCombSal() {
        return nivCombSal;
    }

    public void setNivCombSal(Double nivCombSal) {
        this.nivCombSal = nivCombSal;
    }

    public String getDaExist() {
        return daExist;
    }

    public void setDaExist(String daExist) {
        this.daExist = daExist;
    }

    public String getFeHrEnt() {
        return feHrEnt;
    }

    public void setFeHrEnt(String feHrEnt) {
        this.feHrEnt = feHrEnt;
    }

    public Integer getKmjEnt() {
        return kmjEnt;
    }

    public void setKmjEnt(Integer kmjEnt) {
        this.kmjEnt = kmjEnt;
    }

    public Double getNivCombEnt() {
        return nivCombEnt;
    }

    public void setNivCombEnt(Double nivCombEnt) {
        this.nivCombEnt = nivCombEnt;
    }

    public String getDaNuevos() {
        return daNuevos;
    }

    public void setDaNuevos(String daNuevos) {
        this.daNuevos = daNuevos;
    }

    public Integer getNroParticipa() {
        return nroParticipa;
    }

    public void setNroParticipa(Integer nroParticipa) {
        this.nroParticipa = nroParticipa;
    }

    public Integer getNroCorresponde() {
        return nroCorresponde;
    }

    public void setNroCorresponde(Integer nroCorresponde) {
        this.nroCorresponde = nroCorresponde;
    }
}
