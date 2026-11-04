package modelo;

public class CheckInOut {

    private Integer id;
    private String feHrSal;
    private Integer kmjSal;
    private Double nivCombSal;
    private String daExist;
    private String feHrEnt; // Opcional (null en el alta)
    private Integer kmjEnt; // Opcional (null en el alta)
    private Double nivCombEnt; // Opcional (null en el alta)
    private String daNuevos; // Opcional (null en el alta)
    private Integer nroParticipaSal; // Sucursal donde se entrega/sale el vehículo
    private Integer nroParticipaEnt; // Sucursal donde se devuelve el vehículo (null en el alta)
    private Integer nroCorresponde;  // Contrato asociado

    public CheckInOut() {
    }

    public CheckInOut(Integer id) {
        this.id = id;
    }

    /**
     * Constructor para salida/entrega inicial con sucursal de salida.
     */
    public CheckInOut(String feHrSal, Integer kmjSal, Double nivCombSal, String daExist,
                      Integer nroParticipaSal) {
        this.feHrSal = feHrSal;
        this.kmjSal = kmjSal;
        this.nivCombSal = nivCombSal;
        this.daExist = daExist;
        this.nroParticipaSal = nroParticipaSal;
    }

    /**
     * Constructor sin ID para altas con AUTO_INCREMENT en la base de datos (nueva estructura checkInOuts).
     */
    public CheckInOut(String feHrSal, Integer kmjSal, Double nivCombSal, String daExist,
                      String feHrEnt, Integer kmjEnt, Double nivCombEnt, String daNuevos,
                      Integer nroParticipaSal, Integer nroParticipaEnt, Integer nroCorresponde) {
        this.feHrSal = feHrSal;
        this.kmjSal = kmjSal;
        this.nivCombSal = nivCombSal;
        this.daExist = daExist;
        this.feHrEnt = feHrEnt;
        this.kmjEnt = kmjEnt;
        this.nivCombEnt = nivCombEnt;
        this.daNuevos = daNuevos;
        this.nroParticipaSal = nroParticipaSal;
        this.nroParticipaEnt = nroParticipaEnt;
        this.nroCorresponde = nroCorresponde;
    }

    /**
     * Constructor completo para consultas de lectura desde la base de datos (nueva estructura checkInOuts).
     */
    public CheckInOut(Integer id, String feHrSal, Integer kmjSal, Double nivCombSal, String daExist, 
                      String feHrEnt, Integer kmjEnt, Double nivCombEnt, String daNuevos, 
                      Integer nroParticipaSal, Integer nroParticipaEnt, Integer nroCorresponde) {
        this.id = id;
        this.feHrSal = feHrSal;
        this.kmjSal = kmjSal;
        this.nivCombSal = nivCombSal;
        this.daExist = daExist;
        this.feHrEnt = feHrEnt;
        this.kmjEnt = kmjEnt;
        this.nivCombEnt = nivCombEnt;
        this.daNuevos = daNuevos;
        this.nroParticipaSal = nroParticipaSal;
        this.nroParticipaEnt = nroParticipaEnt;
        this.nroCorresponde = nroCorresponde;
    }

    /**
     * Constructores de compatibilidad hacia atrás.
     */
    public CheckInOut(String feHrSal, Integer kmjSal, Double nivCombSal, String daExist,
                      String feHrEnt, Integer kmjEnt, Double nivCombEnt, String daNuevos,
                      Integer nroParticipa, Integer nroCorresponde) {
        this(feHrSal, kmjSal, nivCombSal, daExist, feHrEnt, kmjEnt, nivCombEnt, daNuevos, nroParticipa, null, nroCorresponde);
    }

    public CheckInOut(Integer id, String feHrSal, Integer kmjSal, Double nivCombSal, String daExist, 
                      String feHrEnt, Integer kmjEnt, Double nivCombEnt, String daNuevos, 
                      Integer nroParticipa, Integer nroCorresponde) {
        this(id, feHrSal, kmjSal, nivCombSal, daExist, feHrEnt, kmjEnt, nivCombEnt, daNuevos, nroParticipa, null, nroCorresponde);
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

    public Integer getNroParticipaSal() {
        return nroParticipaSal;
    }

    public void setNroParticipaSal(Integer nroParticipaSal) {
        this.nroParticipaSal = nroParticipaSal;
    }

    public Integer getNroParticipaEnt() {
        return nroParticipaEnt;
    }

    public void setNroParticipaEnt(Integer nroParticipaEnt) {
        this.nroParticipaEnt = nroParticipaEnt;
    }

    public Integer getNroCorresponde() {
        return nroCorresponde;
    }

    public void setNroCorresponde(Integer nroCorresponde) {
        this.nroCorresponde = nroCorresponde;
    }

    /**
     * Métodos puente para compatibilidad con código anterior.
     */
    public Integer getNroParticipa() {
        return nroParticipaSal;
    }

    public void setNroParticipa(Integer nroParticipa) {
        this.nroParticipaSal = nroParticipa;
    }
}
