package dao;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class DBInitializer {
    private final String jdbcUrl;
    private final String usuario;
    private final String password;
    private final String mongoUri;
    private final String dbName;

    public DBInitializer() {
        try {
            // Cargar explícitamente el driver JDBC para MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver JDBC de MySQL cargado correctamente");

            // Cargar configuración
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
            props.load(fis);

            // Configuración SQL
            this.jdbcUrl = props.getProperty("jdbc.url") + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            this.usuario = props.getProperty("jdbc.usuario");
            this.password = props.getProperty("jdbc.password");

            // Configuración MongoDB
            this.mongoUri = props.getProperty("mongodb.uri");
            this.dbName = props.getProperty("mongodb.database");

            fis.close();

            // Inicializar bases de datos
            inicializarSQL();
            inicializarMongoDB();

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se pudo cargar el driver JDBC de MySQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se encontró el driver JDBC de MySQL", e);
        } catch (Exception e) {
            System.err.println("Error al inicializar las bases de datos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudieron inicializar las bases de datos", e);
        }
    }

    private void inicializarSQL() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, usuario, password)) {
            System.out.println("Conexión a MySQL establecida correctamente");

            // Crear base de datos si no existe
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS dealership");
                System.out.println("Base de datos 'dealership' creada o verificada correctamente");
            }

            // Usar la base de datos
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("USE dealership");
                System.out.println("Usando base de datos 'dealership'");
            }

            // Crear tabla de vehículos
            String sqlVehiculos = "CREATE TABLE IF NOT EXISTS vehicles (" +
                    "matricula VARCHAR(10) PRIMARY KEY, " +
                    "tipo VARCHAR(20) NOT NULL, " +          // coche / tractor / militar
                    "marca VARCHAR(50) NOT NULL, " +
                    "modelo VARCHAR(50) NOT NULL, " +
                    "potencia_motor INT NOT NULL, " +        // 50/100/150/200
                    "anio_fabricacion INT NOT NULL, " +
                    "precio DOUBLE NOT NULL)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlVehiculos);
                System.out.println("Tabla 'vehicles' creada o verificada correctamente");
            }

            // Crear tabla para coches
            String sqlCoches = "CREATE TABLE IF NOT EXISTS coches (" +
                    "matricula VARCHAR(10) PRIMARY KEY, " +
                    "numero_puertas INT NOT NULL, " +
                    "tipo_combustible VARCHAR(20) NOT NULL, " +
                    "automatico BOOLEAN NOT NULL, " +
                    "FOREIGN KEY (matricula) REFERENCES vehicles(matricula) ON DELETE CASCADE)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlCoches);
                System.out.println("Tabla 'coches' creada o verificada correctamente");
            }

            // Crear tabla para tractores
            String sqlTractores = "CREATE TABLE IF NOT EXISTS tractores (" +
                    "matricula VARCHAR(10) PRIMARY KEY, " +
                    "artilugio VARCHAR(20) NOT NULL, " +     // Aplanador/Arador/Regador
                    "tipo_tractor VARCHAR(30) NOT NULL, " +
                    "cabina BOOLEAN NOT NULL, " +
                    "FOREIGN KEY (matricula) REFERENCES vehicles(matricula) ON DELETE CASCADE)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlTractores);
                System.out.println("Tabla 'tractores' creada o verificada correctamente");
            }

            // Crear tabla para tanques (vehículos militares)
            String sqlTanques = "CREATE TABLE IF NOT EXISTS tanques (" +
                    "matricula VARCHAR(10) PRIMARY KEY, " +
                    "armamento VARCHAR(50) NOT NULL, " +
                    "blindaje_mm DOUBLE NOT NULL, " +
                    "velocidad_maxima INT NOT NULL, " +
                    "FOREIGN KEY (matricula) REFERENCES vehicles(matricula) ON DELETE CASCADE)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlTanques);
                System.out.println("Tabla 'tanques' creada o verificada correctamente");
            }

            // Crear tabla para posiciones del parking
            String sqlParking = "CREATE TABLE IF NOT EXISTS parking (" +
                    "matricula VARCHAR(10) PRIMARY KEY, " +
                    "pos_x INT NOT NULL, " +
                    "pos_y INT NOT NULL, " +
                    "FOREIGN KEY (matricula) REFERENCES vehicles(matricula) ON DELETE CASCADE)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlParking);
                System.out.println("Tabla 'parking' creada o verificada correctamente");
            }

            System.out.println("Base de datos SQL inicializada correctamente");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos SQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar la base de datos SQL", e);
        }
    }

    private void inicializarMongoDB() {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoUri))) {
            // Obtener o crear base de datos
            MongoDatabase database = mongoClient.getDatabase(dbName);

            // Verificar si la colección existe, si no, se creará automáticamente al insertar documentos
            if (!database.listCollectionNames().into(new java.util.ArrayList<>()).contains("vehicles")) {
                database.createCollection("vehicles");
                System.out.println("Colección 'vehicles' creada en MongoDB");
            } else {
                System.out.println("Colección 'vehicles' ya existe en MongoDB");
            }

            System.out.println("Base de datos MongoDB inicializada correctamente");

        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar la base de datos MongoDB", e);
        }
    }
}