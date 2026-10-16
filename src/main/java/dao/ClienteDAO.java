package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Cliente;
import util.DatabaseConnection;

public class ClienteDAO {

    public void insertar(Cliente cliente) {
        String sql = "INSERT INTO Clientes (C_id, C_dni, C_nom, C_apl, C_dir, C_tel, C_correo, C_fAlta) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getId());
            stmt.setInt(2, cliente.getDni());
            stmt.setString(3, cliente.getNom());
            stmt.setString(4, cliente.getApl());
            stmt.setString(5, cliente.getDir());
            stmt.setLong(6, cliente.getTel());
            stmt.setString(7, cliente.getCorreo());
            stmt.setString(8, cliente.getfAlta());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el cliente: " + e.getMessage(), e);
        }
    }

    public Cliente buscarPorId(int id) {
        String sql = "SELECT C_id, C_dni, C_nom, C_apl, C_dir, C_tel, C_correo, C_fAlta "
                   + "FROM Clientes WHERE C_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
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
            throw new RuntimeException("Error al buscar el cliente por C_id: " + e.getMessage(), e);
        }
        return null;
    }
}
