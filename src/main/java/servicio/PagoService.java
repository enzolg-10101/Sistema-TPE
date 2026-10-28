package servicio;

import dao.ContratoDAO;
import dao.PagoDAO;
import modelo.Contrato;
import modelo.Pago;

public class PagoService {

    private PagoDAO pagoDAO;
    private ContratoDAO contratoDAO;

    public PagoService() {
        this.pagoDAO = new PagoDAO();
        this.contratoDAO = new ContratoDAO();
    }

    public PagoService(PagoDAO pagoDAO, ContratoDAO contratoDAO) {
        this.pagoDAO = pagoDAO;
        this.contratoDAO = contratoDAO;
    }

    /**
     * 4.6 Alta en la entidad Pagos
     */
    public Pago altaPago(Pago pago) {
        // Inicio-ts
        try {
            if (pago == null) {
                throw new IllegalArgumentException("El pago no puede ser nulo.");
            }

            // Por cada relación saliente total:
            // 1. Generar la nupla correspondiente en Registra (FK nro_Registra -> Contratos(Co_nro))
            generarNuplaRegistra(pago);

            // Grabar la nupla en Pagos
            pagoDAO.insertar(pago);

            return pago;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Pago: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    // --- 1 Generar la nupla correspondiente en Registra ---
    private void generarNuplaRegistra(Pago pago) {
        // Inicio-ts
        Contrato c = seleccionarContratoRegistra(pago.getNroRegistra());
        pago.setNroRegistra(c.getNro());
        // Fin-ts
    }

    // --- 2 Seleccionar el elemento (c) en la entidad Contratos según Registra saliente ---
    private Contrato seleccionarContratoRegistra(Integer nroContrato) {
        // Inicio-ts
        if (nroContrato == null) {
            throw new RuntimeException("Error: Debe especificar el número de contrato asociado al pago.");
        }
        Contrato c = contratoDAO.buscarPorId(nroContrato);
        if (c == null) {
            throw new RuntimeException("Error: El Contrato especificado (" + nroContrato + ") no existe.");
        }
        // Sino -> ok -> Retornar (c)
        // Fin-ts
        return c;
    }
}
