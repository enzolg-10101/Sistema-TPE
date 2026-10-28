package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Empleado;
import util.DatabaseConnection;

public class EmpleadoDAO {

    public void insertar(Empleado empleado) {
        String sql = "INSERT INTO Empleados (E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, empleado.getDni());
            stmt.setString(2, empleado.getNom());
            stmt.setString(3, empleado.getApl());
            stmt.setString(4, empleado.getDir());
            stmt.setLong(5, empleado.getTel());
            stmt.setString(6, empleado.getCorreo());
            stmt.setString(7, empleado.getfIng());
            stmt.setInt(8, empleado.getCodTrabaja());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    empleado.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el empleado: " + e.getMessage(), e);
        }
    }

    public Empleado buscarPorId(int id) {
        String sql = "SELECT E_id, E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja "
                   + "FROM Empleados WHERE E_dni = ? OR E_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el empleado: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT E_id, E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja "
                   + "FROM Empleados ORDER BY E_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar empleados: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Empleado> buscarPorTexto(String criterio) {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT E_id, E_dni, E_nom, E_apl, E_dir, E_tel, E_correo, E_fIng, cod_Trabaja "
                   + "FROM Empleados "
                   + "WHERE CAST(E_id AS CHAR) LIKE ? "
                   + "OR CAST(E_dni AS CHAR) LIKE ? "
                   + "OR LOWER(E_nom) LIKE ? "
                   + "OR LOWER(E_apl) LIKE ? "
                   + "OR LOWER(E_correo) LIKE ? "
                   + "OR CAST(cod_Trabaja AS CHAR) LIKE ? "
                   + "ORDER BY E_id ASC";
        String patron = "%" + (criterio != null ? criterio.trim().toLowerCase() : "") + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                stmt.setString(i, patron);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empleados: " + e.getMessage(), e);
        }
        return lista;
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
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
