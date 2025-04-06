package dao;

import modelo.FabricaVehiculos;
import modelo.vehiculo.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.io.FileInputStream;

public class VehiculoDAOSQL implements VehiculoDAO {

    private final String jdbcUrl;
    private final String usuario;
    private final String password;

    public VehiculoDAOSQL() {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
            props.load(fis);

            this.jdbcUrl = props.getProperty("jdbc.url") + "/dealership";
            this.usuario = props.getProperty("jdbc.usuario");
            this.password = props.getProperty("jdbc.password");

            fis.close();

        } catch (Exception e) {
            System.err.println("Error al cargar la configuración: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar SQL DAO", e);
        }
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, usuario, password);
    }

    @Override
    public boolean guardar(Vehiculo vehiculo) {
        try (Connection conn = obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                // Insertar en la tabla principal de vehículos
                String sqlVehiculo = "INSERT INTO vehicles VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlVehiculo)) {
                    pstmt.setString(1, vehiculo.getMatricula());
                    pstmt.setString(2, vehiculo.getTipoVehiculo().toUpperCase());
                    pstmt.setString(3, vehiculo.getMarca());
                    pstmt.setString(4, vehiculo.getMarca()); // Usamos marca en lugar de modelo
                    pstmt.setInt(5, vehiculo.getPotenciaMotor());
                    pstmt.setInt(6, vehiculo.getAnioFabricacion());
                    pstmt.setDouble(7, vehiculo.getPrecio());
                    pstmt.executeUpdate();
                }

                // Insertar en la tabla de posiciones si está aparcado
                if (vehiculo.estaAparcado()) {
                    String sqlParking = "INSERT INTO parking VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlParking)) {
                        pstmt.setString(1, vehiculo.getMatricula());
                        pstmt.setInt(2, vehiculo.getPosicionX());
                        pstmt.setInt(3, vehiculo.getPosicionY());
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al guardar vehículo: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Vehiculo vehiculo) {
        try (Connection conn = obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                // Actualizar la tabla principal de vehículos
                String sqlVehiculo = "UPDATE vehicles SET marca = ?, potencia_motor = ?, " +
                        "anio_fabricacion = ?, precio = ? WHERE matricula = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlVehiculo)) {
                    pstmt.setString(1, vehiculo.getMarca());
                    pstmt.setInt(2, vehiculo.getPotenciaMotor());
                    pstmt.setInt(3, vehiculo.getAnioFabricacion());
                    pstmt.setDouble(4, vehiculo.getPrecio());
                    pstmt.setString(5, vehiculo.getMatricula());
                    pstmt.executeUpdate();
                }

                // Actualizar la posición en el parking si está aparcado
                if (vehiculo.estaAparcado()) {
                    // Verificar si ya existe en la tabla de parking
                    boolean existeEnParking = false;
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM parking WHERE matricula = ?")) {
                        pstmt.setString(1, vehiculo.getMatricula());
                        try (ResultSet rs = pstmt.executeQuery()) {
                            existeEnParking = rs.next();
                        }
                    }

                    if (existeEnParking) {
                        String sqlUpdateParking = "UPDATE parking SET pos_x = ?, pos_y = ? WHERE matricula = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateParking)) {
                            pstmt.setInt(1, vehiculo.getPosicionX());
                            pstmt.setInt(2, vehiculo.getPosicionY());
                            pstmt.setString(3, vehiculo.getMatricula());
                            pstmt.executeUpdate();
                        }
                    } else {
                        String sqlInsertParking = "INSERT INTO parking VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertParking)) {
                            pstmt.setString(1, vehiculo.getMatricula());
                            pstmt.setInt(2, vehiculo.getPosicionX());
                            pstmt.setInt(3, vehiculo.getPosicionY());
                            pstmt.executeUpdate();
                        }
                    }
                } else {
                    // Si no está aparcado, eliminar de la tabla de parking si existía
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM parking WHERE matricula = ?")) {
                        pstmt.setString(1, vehiculo.getMatricula());
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al actualizar vehículo: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(String matricula) {
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM vehicles WHERE matricula = ?")) {

            pstmt.setString(1, matricula);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar vehículo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Vehiculo> buscarPorId(String matricula) {
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT v.*, p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN parking p ON v.matricula = p.matricula " +
                             "WHERE v.matricula = ?")) {

            pstmt.setString(1, matricula);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(construirVehiculoDesdeResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar vehículo: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Vehiculo> obtenerTodos() {
        List<Vehiculo> vehiculos = new ArrayList<>();

        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT v.*, p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN parking p ON v.matricula = p.matricula")) {

            while (rs.next()) {
                Vehiculo vehiculo = construirVehiculoDesdeResultSet(rs);
                vehiculos.add(vehiculo);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los vehículos: " + e.getMessage());
            e.printStackTrace();
        }

        return vehiculos;
    }

    @Override
    public List<Vehiculo> obtenerPorTipo(String tipo) {
        List<Vehiculo> vehiculos = new ArrayList<>();

        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT v.*, p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN parking p ON v.matricula = p.matricula " +
                             "WHERE v.tipo = ?")) {

            pstmt.setString(1, tipo.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vehiculo vehiculo = construirVehiculoDesdeResultSet(rs);
                    vehiculos.add(vehiculo);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener vehículos por tipo: " + e.getMessage());
            e.printStackTrace();
        }

        return vehiculos;
    }

    @Override
    public boolean existe(String matricula) {
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM vehicles WHERE matricula = ?")) {

            pstmt.setString(1, matricula);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarPosicion(String matricula, int posicionX, int posicionY) {
        try (Connection conn = obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                // Verificar si ya existe en la tabla de parking
                boolean existeEnParking = false;
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM parking WHERE matricula = ?")) {
                    pstmt.setString(1, matricula);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        existeEnParking = rs.next();
                    }
                }

                if (posicionX >= 0 && posicionY >= 0) {
                    // Actualizar o insertar la posición
                    if (existeEnParking) {
                        // Actualizar
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "UPDATE parking SET pos_x = ?, pos_y = ? WHERE matricula = ?")) {
                            pstmt.setInt(1, posicionX);
                            pstmt.setInt(2, posicionY);
                            pstmt.setString(3, matricula);
                            pstmt.executeUpdate();
                        }
                    } else {
                        // Insertar
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "INSERT INTO parking VALUES (?, ?, ?)")) {
                            pstmt.setString(1, matricula);
                            pstmt.setInt(2, posicionX);
                            pstmt.setInt(3, posicionY);
                            pstmt.executeUpdate();
                        }
                    }
                } else if (existeEnParking) {
                    // Eliminar la posición si las coordenadas son negativas
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "DELETE FROM parking WHERE matricula = ?")) {
                        pstmt.setString(1, matricula);
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al actualizar posición: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Vehiculo construirVehiculoDesdeResultSet(ResultSet rs) throws SQLException {
        String matricula = rs.getString("matricula");
        String tipo = rs.getString("tipo");
        String marca = rs.getString("marca");
        // Usamos el mismo valor para modelo que para marca
        String modelo = marca;
        int potenciaMotor = rs.getInt("potencia_motor");
        int anioFabricacion = rs.getInt("anio_fabricacion");
        double precio = rs.getDouble("precio");

        // Obtener posición del parking (puede ser NULL)
        int posicionX = -1;
        int posicionY = -1;
        try {
            posicionX = rs.getInt("pos_x");
            posicionY = rs.getInt("pos_y");
            if (rs.wasNull()) {
                posicionX = -1;
                posicionY = -1;
            }
        } catch (SQLException e) {
            // Ignorar error si no existe la columna
        }

        // Simplificamos la creación usando valores predeterminados para atributos específicos
        switch (tipo) {
            case "COCHE":
                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_COCHE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        4, "Gasolina", false // Valores por defecto
                );

            case "TRACTOR":
                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TRACTOR,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        "Arador", "Agrícola", true // Valores por defecto
                );

            case "TANQUE":
                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TANQUE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        "Cañón estándar", 100.0, 50 // Valores por defecto
                );

            default:
                throw new SQLException("Tipo de vehículo desconocido: " + tipo);
        }
    }
}