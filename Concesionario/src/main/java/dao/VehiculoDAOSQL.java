package dao;

import modelo.FabricaVehiculos;
import modelo.vehiculo.Coche;
import modelo.vehiculo.Tanque;
import modelo.vehiculo.Tractor;
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
                    pstmt.setString(4, vehiculo.getModelo());
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

                // Insertar atributos específicos según el tipo
                if (vehiculo instanceof Coche) {
                    Coche coche = (Coche) vehiculo;
                    String sqlCoche = "INSERT INTO coches VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlCoche)) {
                        pstmt.setString(1, coche.getMatricula());
                        pstmt.setInt(2, coche.getNumeroPuertas());
                        pstmt.setString(3, coche.getTipoCombustible());
                        pstmt.setBoolean(4, coche.isAutomatico());
                        pstmt.executeUpdate();
                    }
                } else if (vehiculo instanceof Tractor) {
                    Tractor tractor = (Tractor) vehiculo;
                    String sqlTractor = "INSERT INTO tractores VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlTractor)) {
                        pstmt.setString(1, tractor.getMatricula());
                        pstmt.setString(2, tractor.getArtilugio());
                        pstmt.setString(3, tractor.getTipoTractor());
                        pstmt.setBoolean(4, tractor.hasCabina());
                        pstmt.executeUpdate();
                    }
                } else if (vehiculo instanceof Tanque) {
                    Tanque tanque = (Tanque) vehiculo;
                    String sqlTanque = "INSERT INTO tanques VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlTanque)) {
                        pstmt.setString(1, tanque.getMatricula());
                        pstmt.setString(2, tanque.getArmamento());
                        pstmt.setDouble(3, tanque.getBlindajeMM());
                        pstmt.setInt(4, tanque.getVelocidadMaxima());
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
                String sqlVehiculo = "UPDATE vehicles SET marca = ?, modelo = ?, potencia_motor = ?, " +
                        "anio_fabricacion = ?, precio = ? WHERE matricula = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlVehiculo)) {
                    pstmt.setString(1, vehiculo.getMarca());
                    pstmt.setString(2, vehiculo.getModelo());
                    pstmt.setInt(3, vehiculo.getPotenciaMotor());
                    pstmt.setInt(4, vehiculo.getAnioFabricacion());
                    pstmt.setDouble(5, vehiculo.getPrecio());
                    pstmt.setString(6, vehiculo.getMatricula());
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

                // Actualizar atributos específicos según el tipo
                if (vehiculo instanceof Coche) {
                    Coche coche = (Coche) vehiculo;
                    String sqlCoche = "UPDATE coches SET numero_puertas = ?, tipo_combustible = ?, automatico = ? WHERE matricula = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlCoche)) {
                        pstmt.setInt(1, coche.getNumeroPuertas());
                        pstmt.setString(2, coche.getTipoCombustible());
                        pstmt.setBoolean(3, coche.isAutomatico());
                        pstmt.setString(4, coche.getMatricula());
                        pstmt.executeUpdate();
                    }
                } else if (vehiculo instanceof Tractor) {
                    Tractor tractor = (Tractor) vehiculo;
                    String sqlTractor = "UPDATE tractores SET artilugio = ?, tipo_tractor = ?, cabina = ? WHERE matricula = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlTractor)) {
                        pstmt.setString(1, tractor.getArtilugio());
                        pstmt.setString(2, tractor.getTipoTractor());
                        pstmt.setBoolean(3, tractor.hasCabina());
                        pstmt.setString(4, tractor.getMatricula());
                        pstmt.executeUpdate();
                    }
                } else if (vehiculo instanceof Tanque) {
                    Tanque tanque = (Tanque) vehiculo;
                    String sqlTanque = "UPDATE tanques SET armamento = ?, blindaje_mm = ?, velocidad_maxima = ? WHERE matricula = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlTanque)) {
                        pstmt.setString(1, tanque.getArmamento());
                        pstmt.setDouble(2, tanque.getBlindajeMM());
                        pstmt.setInt(3, tanque.getVelocidadMaxima());
                        pstmt.setString(4, tanque.getMatricula());
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
                     "SELECT v.*, " +
                             "c.numero_puertas, c.tipo_combustible, c.automatico, " +
                             "t.artilugio, t.tipo_tractor, t.cabina, " +
                             "tq.armamento, tq.blindaje_mm, tq.velocidad_maxima, " +
                             "p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN coches c ON v.matricula = c.matricula " +
                             "LEFT JOIN tractores t ON v.matricula = t.matricula " +
                             "LEFT JOIN tanques tq ON v.matricula = tq.matricula " +
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
                     "SELECT v.*, " +
                             "c.numero_puertas, c.tipo_combustible, c.automatico, " +
                             "t.artilugio, t.tipo_tractor, t.cabina, " +
                             "tq.armamento, tq.blindaje_mm, tq.velocidad_maxima, " +
                             "p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN coches c ON v.matricula = c.matricula " +
                             "LEFT JOIN tractores t ON v.matricula = t.matricula " +
                             "LEFT JOIN tanques tq ON v.matricula = tq.matricula " +
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
                     "SELECT v.*, " +
                             "c.numero_puertas, c.tipo_combustible, c.automatico, " +
                             "t.artilugio, t.tipo_tractor, t.cabina, " +
                             "tq.armamento, tq.blindaje_mm, tq.velocidad_maxima, " +
                             "p.pos_x, p.pos_y " +
                             "FROM vehicles v " +
                             "LEFT JOIN coches c ON v.matricula = c.matricula " +
                             "LEFT JOIN tractores t ON v.matricula = t.matricula " +
                             "LEFT JOIN tanques tq ON v.matricula = tq.matricula " +
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
        String modelo = rs.getString("modelo");
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

        switch (tipo) {
            case "COCHE":
                int numeroPuertas = rs.getInt("numero_puertas");
                String tipoCombustible = rs.getString("tipo_combustible");
                boolean automatico = rs.getBoolean("automatico");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_COCHE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        numeroPuertas, tipoCombustible, automatico
                );

            case "TRACTOR":
                String artilugio = rs.getString("artilugio");
                String tipoTractor = rs.getString("tipo_tractor");
                boolean cabina = rs.getBoolean("cabina");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TRACTOR,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        artilugio, tipoTractor, cabina
                );

            case "TANQUE":
                String armamento = rs.getString("armamento");
                double blindajeMM = rs.getDouble("blindaje_mm");
                int velocidadMaxima = rs.getInt("velocidad_maxima");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TANQUE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        armamento, blindajeMM, velocidadMaxima
                );

            default:
                throw new SQLException("Tipo de vehículo desconocido: " + tipo);
        }
    }
}