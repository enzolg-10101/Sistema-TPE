package servicio;

import dao.ClienteDAO;
import java.util.List;
import modelo.Cliente;

public class ClienteService {

    private ClienteDAO clienteDAO;

    public ClienteService() {
        this.clienteDAO = new ClienteDAO();
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Método para dar de alta un nuevo Cliente.
     * @param cliente El objeto Cliente a persistir.
     * @return El Cliente persistido (con su ID ya generado por la base de datos).
     */
    public Cliente altaCliente(Cliente cliente) {
        // Inicio-ts
        try {
            clienteDAO.insertar(cliente);
            return cliente;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Cliente: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    public Cliente actualizarCliente(Cliente cliente) {
        try {
            clienteDAO.actualizar(cliente);
            return cliente;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el Cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.listarTodos();
    }
}
