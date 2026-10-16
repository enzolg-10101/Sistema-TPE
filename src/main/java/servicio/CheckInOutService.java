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
     * @return El CheckInOut persistido (idealmente con su ID ya generado por la base de datos).
     */
    public CheckInOut altaCheckInOut(CheckInOut checkInOut) {
        // Inicio-ts
        try {
            checkInOutDAO.insertar(checkInOut);
            
            // Se retorna el objeto checkInOut
            return checkInOut;
        } catch (Exception e) {
            // Manejo y rollback de la transacción
            throw new RuntimeException("Error en la transacción de Alta de Check_inout: " + e.getMessage(), e);
        }
        // Fin-ts
    }
}
