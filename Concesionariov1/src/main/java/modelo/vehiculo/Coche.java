package modelo.vehiculo;

public class Coche extends Vehiculo {

    public Coche(String matricula, String marca, int potenciaMotor,
                 int anioFabricacion, double precio) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio);
    }

    public Coche(String matricula, String marca, int potenciaMotor,
                 int anioFabricacion, double precio, int posicionX, int posicionY) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio, posicionX, posicionY);
    }

    @Override
    public String getTipoVehiculo() {
        return "Coche";
    }
}