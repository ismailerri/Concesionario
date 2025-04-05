package modelo;

import dao.VehiculoDAO;
import dao.VehiculoDAOMemoria;
import modelo.vehiculo.Vehiculo;

import java.util.List;
import java.util.Optional;

public class GestorDatos {

    private final VehiculoDAO dao;

    public GestorDatos() {
        // Usar la implementación en memoria para evitar problemas de DB
        this.dao = new VehiculoDAOMemoria();
        System.out.println("Usando implementación en memoria para pruebas");
    }

    public boolean guardarVehiculo(Vehiculo vehiculo) {
        return dao.guardar(vehiculo);
    }

    public boolean guardarConjuntoVehiculos(List<Vehiculo> vehiculos) {
        boolean todosGuardados = true;

        for (Vehiculo vehiculo : vehiculos) {
            if (!guardarVehiculo(vehiculo)) {
                todosGuardados = false;
                // Si falla uno, intentamos eliminar los que ya se guardaron
                for (Vehiculo v : vehiculos) {
                    if (!v.equals(vehiculo)) { // No intentar eliminar el que acaba de fallar
                        eliminarVehiculo(v.getMatricula());
                    }
                }
                break;
            }
        }

        return todosGuardados;
    }

    public boolean actualizarVehiculo(Vehiculo vehiculo) {
        return dao.actualizar(vehiculo);
    }

    public boolean eliminarVehiculo(String matricula) {
        return dao.eliminar(matricula);
    }

    public Optional<Vehiculo> buscarVehiculo(String matricula) {
        return dao.buscarPorId(matricula);
    }

    public List<Vehiculo> obtenerTodosLosVehiculos() {
        return dao.obtenerTodos();
    }

    public List<Vehiculo> obtenerVehiculosPorTipo(String tipo) {
        return dao.obtenerPorTipo(tipo);
    }

    public boolean actualizarPosicionVehiculo(String matricula, int posicionX, int posicionY) {
        return dao.actualizarPosicion(matricula, posicionX, posicionY);
    }

    public boolean verificarSincronizacion() {
        // En modo memoria siempre está sincronizado
        return true;
    }

    public void cerrar() {
        // No hay recursos que cerrar en la implementación en memoria
    }
}