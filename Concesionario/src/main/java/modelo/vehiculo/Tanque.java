package modelo.vehiculo;

public class Tanque extends Vehiculo {
    private String armamento;

    public Tanque(String matricula, String marca, int potenciaMotor,
                  int anioFabricacion, double precio, String armamento) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio);
        this.armamento = armamento;
    }

    public Tanque(String matricula, String marca, int potenciaMotor,
                  int anioFabricacion, double precio, int posicionX, int posicionY,
                  String armamento) {
        super(matricula, marca, potenciaMotor, anioFabricacion, precio, posicionX, posicionY);
        this.armamento = armamento;
    }

    @Override
    public String getTipoVehiculo() {
        return "Tanque";
    }

    public String getArmamento() {
        return armamento;
    }

    public void setArmamento(String armamento) {
        this.armamento = armamento;
    }

    // Método para disparar al vehículo que está delante
    public String disparar(Vehiculo[][] matriz) {
        // Si el tanque no está aparcado, no puede disparar
        if (!estaAparcado()) {
            return "El tanque no está en el parking, no puede disparar";
        }

        // Determinar la posición del vehículo que está delante
        int posX = getPosicionX();
        int posY = getPosicionY() - 1; // Asumimos que "delante" es hacia arriba en la matriz

        // Verificar que la posición sea válida
        if (posY < 0 || posY >= matriz.length) {
            return "No hay ningún vehículo delante para disparar";
        }

        // Verificar si hay un vehículo en esa posición
        Vehiculo objetivo = matriz[posY][posX];
        if (objetivo == null) {
            return "No hay ningún vehículo delante para disparar";
        }

        // Devolver la matrícula del vehículo a eliminar
        return objetivo.getMatricula();
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" - Armamento: %s", armamento);
    }
}