package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Empleado;
import util.DatabaseConnection;

public class EmpleadoDAO {

    public void insertar(Empleado empleado) {
        String sql = "INSERT INTO Empleados (E_id, E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleado.getId());
            stmt.setInt(2, empleado.getDni());
            stmt.setString(3, empleado.getNom());
            stmt.setString(4, empleado.getApl());
            stmt.setString(5, empleado.getDir());
            stmt.setLong(6, empleado.getTel());
            stmt.setString(7, empleado.getCorreo());
            stmt.setString(8, empleado.getfIng());
            stmt.setInt(9, empleado.getCodTrabaja());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el empleado: " + e.getMessage(), e);
        }
    }

    public Empleado buscarPorId(int id) {
        String sql = "SELECT E_id, E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja "
                   + "FROM Empleados WHERE E_dni = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Empleado(
                        rs.getInt("E_id"),
                        rs.getInt("E_dni"),
                        rs.getString("E_nom"),
                        rs.getString("E_apl"),
                        rs.getString("E_dir"),
                        rs.getLong("E_tel"),
                        rs.getString("E_correo"),
                        rs.getString("E_fIng"),
                        rs.getInt("cod_Trabaja")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el empleado por E_dni: " + e.getMessage(), e);
        }
        return null;
    }
}
