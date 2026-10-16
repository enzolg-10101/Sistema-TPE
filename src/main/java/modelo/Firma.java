package modelo;

public class Firma {

    private Integer contratoId;
    private Integer clienteId;

    public Firma() {
    }

    public Firma(Integer contratoId, Integer clienteId) {
        this.contratoId = contratoId;
        this.clienteId = clienteId;
    }

    public Integer getContratoId() {
        return contratoId;
    }

    public void setContratoId(Integer contratoId) {
        this.contratoId = contratoId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }
}
