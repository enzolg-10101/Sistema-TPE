package servicio;

import dao.ClienteDAO;
import modelo.Cliente;

public class ClienteService {

    private ClienteDAO clienteDAO;

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Método para dar de alta un nuevo Cliente.
     * @param cliente El objeto Cliente a persistir.
     * @return El Cliente persistido (idealmente con su ID ya generado por la base de datos).
     */
    public Cliente altaCliente(Cliente cliente) {
        // Inicio-ts
        try {
            clienteDAO.insertar(cliente);
            
            // Se retorna el objeto cliente
            return cliente;
        } catch (Exception e) {
            // Manejo y rollback de la transacción
            throw new RuntimeException("Error en la transacción de Alta de Cliente: " + e.getMessage(), e);
        }
        // Fin-ts
    }
}
