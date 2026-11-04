package servicio;

import dao.CheckInOutDAO;
import modelo.CheckInOut;

public class CheckInOutService {

    private CheckInOutDAO checkInOutDAO;

    public CheckInOutService(CheckInOutDAO checkInOutDAO) {
        this.checkInOutDAO = checkInOutDAO;
    }

    /**
     * Método para dar de alta un nuevo Check_inout.
     * @param checkInOut El objeto CheckInOut a persistir.
     * @return El CheckInOut persistido con su ID autogenerado.
     */
    public CheckInOut altaCheckInOut(CheckInOut checkInOut) {
        try {
            checkInOutDAO.insertar(checkInOut);
            return checkInOut;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Check_inout: " + e.getMessage(), e);
        }
    }

    public CheckInOut buscarPorId(int id) {
        return checkInOutDAO.buscarPorId(id);
    }

    public CheckInOut buscarPorContrato(int nroContrato) {
        return checkInOutDAO.buscarPorContrato(nroContrato);
    }

    /**
     * Actualiza los valores de checkInOuts que inicialmente fueron nulos cuando se devuelve el vehículo.
     */
    public void registrarDevolucion(int nroContrato, String feHrEnt, int kmjEnt, double nivCombEnt, String daNuevos, int sucEnt) {
        try {
            checkInOutDAO.actualizarDevolucion(nroContrato, feHrEnt, kmjEnt, nivCombEnt, daNuevos, sucEnt);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar CheckInOut en la devolución: " + e.getMessage(), e);
        }
    }

    public void actualizar(CheckInOut checkInOut) {
        try {
            checkInOutDAO.actualizar(checkInOut);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar CheckInOut: " + e.getMessage(), e);
        }
    }
}
