package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Asigna;
import util.DatabaseConnection;

public class AsignaDAO {

    public void insertar(Asigna asigna) {
        String sql = "INSERT INTO Asigna (contrato_id, vehiculo_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, asigna.getContratoId());
            stmt.setInt(2, asigna.getVehiculoId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar en Asigna: " + e.getMessage(), e);
        }
    }

    public Asigna buscarPorId(int id) {
        String sql = "SELECT contrato_id, vehiculo_id FROM Asigna WHERE contrato_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Asigna(
                        rs.getInt("contrato_id"),
                        rs.getInt("vehiculo_id")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar en Asigna por contrato_id: " + e.getMessage(), e);
        }
        return null;
    }
}
