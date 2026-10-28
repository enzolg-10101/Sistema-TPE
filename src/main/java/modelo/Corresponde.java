package modelo;

public class Corresponde {

    private Integer contratoId;
    private Integer checkInOutId;

    public Corresponde() {
    }

    public Corresponde(Integer contratoId, Integer checkInOutId) {
        this.contratoId = contratoId;
        this.checkInOutId = checkInOutId;
    }

    public Integer getContratoId() {
        return contratoId;
    }

    public void setContratoId(Integer contratoId) {
        this.contratoId = contratoId;
    }

    public Integer getCheckInOutId() {
        return checkInOutId;
    }

    public void setCheckInOutId(Integer checkInOutId) {
        this.checkInOutId = checkInOutId;
    }
}
