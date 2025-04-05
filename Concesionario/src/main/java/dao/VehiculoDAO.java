package dao;

import modelo.vehiculo.Vehiculo;
import java.util.List;
import java.util.Optional;

public interface VehiculoDAO {

    boolean guardar(Vehiculo vehiculo);

    boolean actualizar(Vehiculo vehiculo);

    boolean eliminar(String matricula);

    Optional<Vehiculo> buscarPorId(String matricula);

    List<Vehiculo> obtenerTodos();

    List<Vehiculo> obtenerPorTipo(String tipo);

    boolean existe(String matricula);

    boolean actualizarPosicion(String matricula, int posicionX, int posicionY);
}
