package modelo.vehiculo;

public class Tractor extends Vehiculo {
    private String artilugio; // Aplanador/Arador/Regador

    public Tractor(String matricula, String marca, int potenciaMotor,
                   int anioFabricacion, double precio, String artilugio) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio);
        validarArtilugio(artilugio);
        this.artilugio = artilugio;
    }

    public Tractor(String matricula, String marca, int potenciaMotor,
                   int anioFabricacion, double precio, int posicionX, int posicionY,
                   String artilugio) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio, posicionX, posicionY);
        validarArtilugio(artilugio);
        this.artilugio = artilugio;
    }

    // Método para validar que el artilugio sea uno de los permitidos
    private void validarArtilugio(String artilugio) {
        if (!artilugio.equals("Aplanador") && !artilugio.equals("Arador") && !artilugio.equals("Regador")) {
            throw new IllegalArgumentException("El artilugio debe ser Aplanador, Arador o Regador");
        }
    }

    @Override
    public String getTipoVehiculo() {
        return "Tractor";
    }

    public String getArtilugio() {
        return artilugio;
    }

    public void setArtilugio(String artilugio) {
        validarArtilugio(artilugio);
        this.artilugio = artilugio;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" - Artilugio: %s", artilugio);
    }
}