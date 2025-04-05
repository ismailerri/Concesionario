package controlador;

import modelo.FabricaVehiculos;
import modelo.GestorDatos;
import modelo.Parking;
import modelo.vehiculo.Tanque;
import modelo.vehiculo.Vehiculo;

import java.util.List;
import java.util.Optional;

public class ControladorVehiculos {

    private final GestorDatos gestorDatos;
    private final Parking parking;

    public ControladorVehiculos() {
        this.gestorDatos = new GestorDatos();
        this.parking = new Parking();
        cargarVehiculosDesdeBaseDeDatos();
    }

    private void cargarVehiculosDesdeBaseDeDatos() {
        List<Vehiculo> vehiculos = gestorDatos.obtenerTodosLosVehiculos();
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.estaAparcado()) {
                parking.estacionarVehiculo(vehiculo, vehiculo.getPosicionX(), vehiculo.getPosicionY());
            }
        }
    }

    public boolean guardarVehiculo(Vehiculo vehiculo) {
        // Verificar que la matrícula no esté duplicada
        if (gestorDatos.buscarVehiculo(vehiculo.getMatricula()).isPresent()) {
            return false;
        }

        // Guardar en base de datos
        return gestorDatos.guardarVehiculo(vehiculo);
    }

    public boolean estacionarVehiculo(String matricula, int x, int y) {
        Optional<Vehiculo> optVehiculo = gestorDatos.buscarVehiculo(matricula);

        if (optVehiculo.isPresent()) {
            Vehiculo vehiculo = optVehiculo.get();

            if (parking.estacionarVehiculo(vehiculo, x, y)) {
                // Actualizar posición en bases de datos
                boolean actualizado = gestorDatos.actualizarPosicionVehiculo(matricula, x, y);

                if (!actualizado) {
                    // Si falla la actualización en BD, revertimos el estacionamiento
                    parking.retirarVehiculo(matricula);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    public boolean actualizarVehiculo(Vehiculo vehiculo) {
        // Comprobar que el vehículo existe
        if (!gestorDatos.buscarVehiculo(vehiculo.getMatricula()).isPresent()) {
            return false;
        }

        // Actualizar en bases de datos
        return gestorDatos.actualizarVehiculo(vehiculo);
    }

    public boolean eliminarVehiculo(String matricula) {
        // Primero lo retiramos del parking si está estacionado
        parking.retirarVehiculo(matricula);

        // Luego lo eliminamos de las bases de datos
        return gestorDatos.eliminarVehiculo(matricula);
    }

    public Optional<Vehiculo> buscarVehiculo(String matricula) {
        return gestorDatos.buscarVehiculo(matricula);
    }

    public List<Vehiculo> obtenerTodosLosVehiculos() {
        return gestorDatos.obtenerTodosLosVehiculos();
    }

    public List<Vehiculo> obtenerVehiculosPorTipo(String tipo) {
        return gestorDatos.obtenerVehiculosPorTipo(tipo);
    }

    public Vehiculo[][] obtenerMatrizParking() {
        return parking.getMatrizParking();
    }

    public boolean plazaOcupada(int x, int y) {
        return parking.plazaOcupada(x, y);
    }

    public Optional<Vehiculo> obtenerVehiculoEnPosicion(int x, int y) {
        return parking.obtenerVehiculoEnPosicion(x, y);
    }

    public boolean retirarVehiculoDeParking(String matricula) {
        Optional<Vehiculo> optVehiculo = gestorDatos.buscarVehiculo(matricula);

        if (optVehiculo.isPresent() && parking.retirarVehiculo(matricula)) {
            // Actualizar posición en bases de datos
            return gestorDatos.actualizarPosicionVehiculo(matricula, -1, -1);
        }

        return false;
    }

    public boolean dispararTanque(String matriculaTanque) {
        // Verificar que el vehículo existe y es un tanque
        Optional<Vehiculo> optVehiculo = gestorDatos.buscarVehiculo(matriculaTanque);
        if (!optVehiculo.isPresent() || !(optVehiculo.get() instanceof Tanque)) {
            return false;
        }

        // Procesar el disparo
        String matriculaObjetivo = parking.procesarDisparoTanque(matriculaTanque);

        // Si hay un objetivo válido, eliminarlo
        if (matriculaObjetivo != null && !matriculaObjetivo.equals("No hay ningún vehículo delante para disparar")) {
            return eliminarVehiculo(matriculaObjetivo);
        }

        return false;
    }

    public void cerrar() {
        gestorDatos.cerrar();
    }
}