package dao;

import modelo.vehiculo.Vehiculo;
import java.util.*;

public class VehiculoDAOMemoria implements VehiculoDAO {
    private Map<String, Vehiculo> vehiculos = new HashMap<>();

    @Override
    public boolean guardar(Vehiculo vehiculo) {
        vehiculos.put(vehiculo.getMatricula(), vehiculo);
        return true;
    }

    @Override
    public boolean actualizar(Vehiculo vehiculo) {
        if (vehiculos.containsKey(vehiculo.getMatricula())) {
            vehiculos.put(vehiculo.getMatricula(), vehiculo);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(String matricula) {
        return vehiculos.remove(matricula) != null;
    }

    @Override
    public Optional<Vehiculo> buscarPorId(String matricula) {
        return Optional.ofNullable(vehiculos.get(matricula));
    }

    @Override
    public List<Vehiculo> obtenerTodos() {
        return new ArrayList<>(vehiculos.values());
    }

    @Override
    public List<Vehiculo> obtenerPorTipo(String tipo) {
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : vehiculos.values()) {
            if (v.getTipoVehiculo().equalsIgnoreCase(tipo)) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    @Override
    public boolean existe(String matricula) {
        return vehiculos.containsKey(matricula);
    }

    @Override
    public boolean actualizarPosicion(String matricula, int posicionX, int posicionY) {
        Vehiculo v = vehiculos.get(matricula);
        if (v != null) {
            v.asignarPosicionParking(posicionX, posicionY);
            return true;
        }
        return false;
    }
}