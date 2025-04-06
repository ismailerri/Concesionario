package modelo;

import modelo.vehiculo.Vehiculo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Parking {
    public static final int TAMANIO = 9;

    private Vehiculo[][] plazas;
    private Map<String, Vehiculo> vehiculosMap;

    public Parking() {
        plazas = new Vehiculo[TAMANIO][TAMANIO];
        vehiculosMap = new HashMap<>();
    }

    public boolean estacionarVehiculo(Vehiculo vehiculo, int x, int y) {
        // Validar coordenadas
        if (!sonCoordenadasValidas(x, y)) {
            return false;
        }

        // Verificar si la plaza está ocupada
        if (plazas[y][x] != null) {
            return false;
        }

        // Si el vehículo ya estaba estacionado, liberar su plaza anterior
        if (vehiculo.estaAparcado()) {
            plazas[vehiculo.getPosicionY()][vehiculo.getPosicionX()] = null;
        }

        // Estacionar el vehículo en la nueva posición
        plazas[y][x] = vehiculo;
        vehiculo.asignarPosicionParking(x, y);

        // Agregar al mapa si no existe
        vehiculosMap.put(vehiculo.getMatricula(), vehiculo);

        return true;
    }

    public boolean retirarVehiculo(String matricula) {
        Vehiculo vehiculo = vehiculosMap.get(matricula);
        if (vehiculo != null && vehiculo.estaAparcado()) {
            plazas[vehiculo.getPosicionY()][vehiculo.getPosicionX()] = null;
            vehiculosMap.remove(matricula);
            return true;
        }
        return false;
    }

    public Optional<Vehiculo> buscarVehiculo(String matricula) {
        return Optional.ofNullable(vehiculosMap.get(matricula));
    }

    public Optional<Vehiculo> obtenerVehiculoEnPosicion(int x, int y) {
        if (sonCoordenadasValidas(x, y)) {
            return Optional.ofNullable(plazas[y][x]);
        }
        return Optional.empty();
    }

    private boolean sonCoordenadasValidas(int x, int y) {
        return x >= 0 && x < TAMANIO && y >= 0 && y < TAMANIO;
    }

    public Map<String, Vehiculo> getVehiculos() {
        return new HashMap<>(vehiculosMap);
    }

    public Vehiculo[][] getMatrizParking() {
        // Creamos una copia para evitar modificaciones externas
        Vehiculo[][] copia = new Vehiculo[TAMANIO][TAMANIO];
        for (int i = 0; i < TAMANIO; i++) {
            System.arraycopy(plazas[i], 0, copia[i], 0, TAMANIO);
        }
        return copia;
    }

    public boolean plazaOcupada(int x, int y) {
        if (sonCoordenadasValidas(x, y)) {
            return plazas[y][x] != null;
        }
        return false; // Plazas fuera de rango se consideran no disponibles
    }

    public int contarPlazasOcupadas() {
        return vehiculosMap.size();
    }

    public int getTotalPlazas() {
        return TAMANIO * TAMANIO;
    }

    // Procesa los disparos de los tanques
    public String procesarDisparoTanque(String matriculaTanque) {
        Vehiculo tanque = vehiculosMap.get(matriculaTanque);
        if (tanque != null && tanque instanceof modelo.vehiculo.Tanque) {
            modelo.vehiculo.Tanque t = (modelo.vehiculo.Tanque) tanque;
            String matriculaObjetivo = t.disparar(plazas);

            // Si se identificó un objetivo, devolverlo para que el controlador lo elimine
            if (matriculaObjetivo != null && !matriculaObjetivo.equals("No hay ningún vehículo delante para disparar")) {
                return matriculaObjetivo;
            }
        }
        return null;
    }

    // Elimina todos los vehículos del parking
    public void limpiarParking() {
        plazas = new Vehiculo[TAMANIO][TAMANIO];
        vehiculosMap.clear();
    }
}