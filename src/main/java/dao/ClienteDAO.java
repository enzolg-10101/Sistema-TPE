package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import util.DatabaseConnection;

public class ClienteDAO {

    public void insertar(Cliente cliente) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertar(cliente, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Versión sobrecargada para participar en una transacción externa.
     * No abre ni cierra la conexión recibida; solo ejecuta el INSERT y asigna el ID autogenerado.
     */
    public void insertar(Cliente cliente, Connection conn) throws SQLException {
        String sql = "INSERT INTO Clientes (C_dni, C_nom, C_apl, C_dir, C_tel, C_correo, C_fAlta) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cliente.getDni());
            stmt.setString(2, cliente.getNom());
            stmt.setString(3, cliente.getApl());
            stmt.setString(4, cliente.getDir());
            stmt.setLong(5, cliente.getTel());
            stmt.setString(6, cliente.getCorreo());
            stmt.setString(7, cliente.getfAlta());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cliente.setId(keys.getInt(1));
                }
            }
        }
    }

    public void actualizar(Cliente cliente) {
        String sql = "UPDATE Clientes SET C_dni = ?, C_nom = ?, C_apl = ?, C_dir = ?, "
                   + "C_tel = ?, C_correo = ?, C_fAlta = ? WHERE C_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getDni());
            stmt.setString(2, cliente.getNom());
            stmt.setString(3, cliente.getApl());
            stmt.setString(4, cliente.getDir());
            stmt.setLong(5, cliente.getTel());
            stmt.setString(6, cliente.getCorreo());
            stmt.setString(7, cliente.getfAlta());
            stmt.setInt(8, cliente.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT C_id, C_dni, C_nom, C_apl, C_dir, C_tel, C_correo, C_fAlta "
                   + "FROM Clientes ORDER BY C_apl ASC, C_nom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("C_id"),
                    rs.getInt("C_dni"),
                    rs.getString("C_nom"),
                    rs.getString("C_apl"),
                    rs.getString("C_dir"),
                    rs.getLong("C_tel"),
                    rs.getString("C_correo"),
                    rs.getString("C_fAlta")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    public Cliente buscarPorId(int id) {
        String sql = "SELECT C_id, C_dni, C_nom, C_apl, C_dir, C_tel, C_correo, C_fAlta "
                   + "FROM Clientes WHERE C_id = ? OR C_dni = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                        rs.getInt("C_id"),
                        rs.getInt("C_dni"),
                        rs.getString("C_nom"),
                        rs.getString("C_apl"),
                        rs.getString("C_dir"),
                        rs.getLong("C_tel"),
                        rs.getString("C_correo"),
                        rs.getString("C_fAlta")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el cliente: " + e.getMessage(), e);
        }
        return null;
    }
}
