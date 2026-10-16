package modelo;

public class Asigna {

    private Integer contratoId;
    private Integer vehiculoId;

    public Asigna() {
    }

    public Asigna(Integer contratoId, Integer vehiculoId) {
        this.contratoId = contratoId;
        this.vehiculoId = vehiculoId;
    }

    public Integer getContratoId() {
        return contratoId;
    }

    public void setContratoId(Integer contratoId) {
        this.contratoId = contratoId;
    }

    public Integer getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Integer vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
}
