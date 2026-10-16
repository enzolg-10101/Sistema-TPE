package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Sucursal;
import util.DatabaseConnection;

public class SucursalDAO {

    public void insertar(Sucursal sucursal) {
        String sql = "INSERT INTO Sucursales (S_cod, S_nom, S_dir, S_tel) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sucursal.getCod());
            stmt.setString(2, sucursal.getNom());
            stmt.setString(3, sucursal.getDir());
            stmt.setLong(4, sucursal.getTel());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar la sucursal: " + e.getMessage(), e);
        }
    }

    public Sucursal buscarPorId(int id) {
        String sql = "SELECT S_cod, S_nom, S_dir, S_tel FROM Sucursales WHERE S_cod = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Sucursal(
                        rs.getInt("S_cod"),
                        rs.getString("S_nom"),
                        rs.getString("S_dir"),
                        rs.getLong("S_tel")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la sucursal por S_cod: " + e.getMessage(), e);
        }
        return null;
    }
}
