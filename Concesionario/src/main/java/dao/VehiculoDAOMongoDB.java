package dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import modelo.FabricaVehiculos;
import modelo.vehiculo.Vehiculo;
import org.bson.Document;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class VehiculoDAOMongoDB implements VehiculoDAO {

    private final String mongoUri;
    private final String dbName;
    private final String collectionName;

    public VehiculoDAOMongoDB() {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
            props.load(fis);

            this.mongoUri = props.getProperty("mongodb.uri");
            this.dbName = props.getProperty("mongodb.database");
            this.collectionName = props.getProperty("mongodb.collection");

            fis.close();

        } catch (Exception e) {
            System.err.println("Error al cargar la configuración de MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar MongoDB DAO", e);
        }
    }

    private MongoCollection<Document> getCollection() {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoUri));
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection(collectionName);
    }

    private Document vehiculoADocument(Vehiculo vehiculo) {
        Document doc = new Document("_id", vehiculo.getMatricula())
                .append("tipo", vehiculo.getTipoVehiculo().toUpperCase())
                .append("marca", vehiculo.getMarca())
                .append("modelo", vehiculo.getModelo())
                .append("potenciaMotor", vehiculo.getPotenciaMotor())
                .append("anioFabricacion", vehiculo.getAnioFabricacion())
                .append("precio", vehiculo.getPrecio());

        // Agregar posición si está aparcado
        if (vehiculo.estaAparcado()) {
            doc.append("posicion", new Document("x", vehiculo.getPosicionX()).append("y", vehiculo.getPosicionY()));
        } else {
            doc.append("posicion", null);
        }

        // Agregar atributos específicos según el tipo
        switch (vehiculo.getTipoVehiculo().toUpperCase()) {
            case "COCHE":
                doc.append("atributos", new Document()
                        .append("numeroPuertas", ((modelo.vehiculo.Coche) vehiculo).getNumeroPuertas())
                        .append("tipoCombustible", ((modelo.vehiculo.Coche) vehiculo).getTipoCombustible())
                        .append("automatico", ((modelo.vehiculo.Coche) vehiculo).isAutomatico()));
                break;

            case "TRACTOR":
                doc.append("atributos", new Document()
                        .append("artilugio", ((modelo.vehiculo.Tractor) vehiculo).getArtilugio())
                        .append("tipoTractor", ((modelo.vehiculo.Tractor) vehiculo).getTipoTractor())
                        .append("cabina", ((modelo.vehiculo.Tractor) vehiculo).hasCabina()));
                break;

            case "TANQUE":
                doc.append("atributos", new Document()
                        .append("armamento", ((modelo.vehiculo.Tanque) vehiculo).getArmamento())
                        .append("blindajeMM", ((modelo.vehiculo.Tanque) vehiculo).getBlindajeMM())
                        .append("velocidadMaxima", ((modelo.vehiculo.Tanque) vehiculo).getVelocidadMaxima()));
                break;
        }

        return doc;
    }

    private Vehiculo documentAVehiculo(Document doc) {
        String matricula = doc.getString("_id");
        String tipo = doc.getString("tipo");
        String marca = doc.getString("marca");
        String modelo = doc.getString("modelo");
        int potenciaMotor = doc.getInteger("potenciaMotor");
        int anioFabricacion = doc.getInteger("anioFabricacion");
        double precio = doc.getDouble("precio");

        int posicionX = -1;
        int posicionY = -1;

        // Obtener posición si existe
        Document posicion = (Document) doc.get("posicion");
        if (posicion != null) {
            posicionX = posicion.getInteger("x");
            posicionY = posicion.getInteger("y");
        }

        Document atributos = (Document) doc.get("atributos");

        switch (tipo) {
            case "COCHE":
                int numeroPuertas = atributos.getInteger("numeroPuertas");
                String tipoCombustible = atributos.getString("tipoCombustible");
                boolean automatico = atributos.getBoolean("automatico");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_COCHE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        numeroPuertas, tipoCombustible, automatico
                );

            case "TRACTOR":
                String artilugio = atributos.getString("artilugio");
                String tipoTractor = atributos.getString("tipoTractor");
                boolean cabina = atributos.getBoolean("cabina");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TRACTOR,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        artilugio, tipoTractor, cabina
                );

            case "TANQUE":
                String armamento = atributos.getString("armamento");
                double blindajeMM = atributos.getDouble("blindajeMM");
                int velocidadMaxima = atributos.getInteger("velocidadMaxima");

                return FabricaVehiculos.recrearVehiculo(
                        FabricaVehiculos.TIPO_TANQUE,
                        matricula, marca, modelo, potenciaMotor, anioFabricacion, precio, posicionX, posicionY,
                        armamento, blindajeMM, velocidadMaxima
                );

            default:
                throw new IllegalArgumentException("Tipo de vehículo desconocido: " + tipo);
        }
    }

    @Override
    public boolean guardar(Vehiculo vehiculo) {
        try {
            Document doc = vehiculoADocument(vehiculo);
            getCollection().insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar vehículo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Vehiculo vehiculo) {
        try {
            Document doc = vehiculoADocument(vehiculo);
            getCollection().replaceOne(Filters.eq("_id", vehiculo.getMatricula()), doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar vehículo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(String matricula) {
        try {
            getCollection().deleteOne(Filters.eq("_id", matricula));
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar vehículo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Vehiculo> buscarPorId(String matricula) {
        try {
            Document doc = getCollection().find(Filters.eq("_id", matricula)).first();
            if (doc != null) {
                return Optional.of(documentAVehiculo(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar vehículo en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Vehiculo> obtenerTodos() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try {
            List<Document> documentos = new ArrayList<>();
            getCollection().find().into(documentos);

            for (Document doc : documentos) {
                try {
                    vehiculos.add(documentAVehiculo(doc));
                } catch (Exception e) {
                    System.err.println("Error al convertir documento: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener vehículos de MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return vehiculos;
    }

    @Override
    public List<Vehiculo> obtenerPorTipo(String tipo) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try {
            List<Document> documentos = new ArrayList<>();
            getCollection().find(Filters.eq("tipo", tipo.toUpperCase())).into(documentos);

            for (Document doc : documentos) {
                try {
                    vehiculos.add(documentAVehiculo(doc));
                } catch (Exception e) {
                    System.err.println("Error al convertir documento: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener vehículos por tipo de MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return vehiculos;
    }

    @Override
    public boolean existe(String matricula) {
        try {
            return getCollection().countDocuments(Filters.eq("_id", matricula)) > 0;
        } catch (Exception e) {
            System.err.println("Error al verificar existencia en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarPosicion(String matricula, int posicionX, int posicionY) {
        try {
            if (posicionX >= 0 && posicionY >= 0) {
                Document posicion = new Document("x", posicionX).append("y", posicionY);
                getCollection().updateOne(
                        Filters.eq("_id", matricula),
                        Updates.set("posicion", posicion)
                );
            } else {
                getCollection().updateOne(
                        Filters.eq("_id", matricula),
                        Updates.set("posicion", null)
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar posición en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}