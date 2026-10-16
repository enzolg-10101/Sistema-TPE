package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import modelo.Vehiculo;
import util.DatabaseConnection;

public class VehiculoDAO {

    public void insertar(Vehiculo vehiculo) {
        String sql = "INSERT INTO Vehiculos (V_id, V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculo.getId());
            stmt.setString(2, vehiculo.getPat());
            stmt.setString(3, vehiculo.getMarca());
            stmt.setString(4, vehiculo.getMod());
            stmt.setInt(5, vehiculo.getaFabr());
            stmt.setString(6, vehiculo.getfAdq());
            stmt.setInt(7, vehiculo.getKmj());
            stmt.setString(8, vehiculo.getEst());
            stmt.setDouble(9, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null) {
                stmt.setString(10, vehiculo.getfBaja());
            } else {
                stmt.setNull(10, Types.VARCHAR);
            }
            stmt.setString(11, vehiculo.getTiComb());
            stmt.setDouble(12, vehiculo.getCarga());
            stmt.setInt(13, vehiculo.getPax());
            stmt.setDouble(14, vehiculo.getTarBase());
            stmt.setString(15, vehiculo.getCat());
            stmt.setInt(16, vehiculo.getCodPertenece());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el vehículo: " + e.getMessage(), e);
        }
    }

    public Vehiculo buscarPorId(String pat) {
        String sql = "SELECT V_id, V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece "
                   + "FROM Vehiculos WHERE V_pat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pat);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Vehiculo(
                        rs.getInt("V_id"),
                        rs.getString("V_pat"),
                        rs.getString("V_marca"),
                        rs.getString("V_mod"),
                        rs.getInt("V_aFabr"),
                        rs.getString("V_fAdq"),
                        rs.getInt("V_kmj"),
                        rs.getString("V_est"),
                        rs.getDouble("V_preAdq"),
                        rs.getString("V_fBaja"),
                        rs.getString("V_tiComb"),
                        rs.getDouble("T_carga"),
                        rs.getInt("T_pax"),
                        rs.getDouble("T_tarBase"),
                        rs.getString("T_cat"),
                        rs.getInt("cod_Pertenece")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo por V_pat: " + e.getMessage(), e);
        }
        return null;
    }
}
