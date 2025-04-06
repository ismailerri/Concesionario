package modelo.vehiculo;

import java.util.Objects;

public abstract class Vehiculo {
    // Atributos comunes a todos los vehículos según el enunciado
    private final String matricula;           // Matrícula como identificador único
    private String marca;                     // Marca del vehículo
    private int potenciaMotor;                // Potencia del motor (50/100/150/200)
    private int anioFabricacion;              // Año de fabricación
    private double precio;                    // Precio del vehículo
    private int posicionX;                    // Posición X en el parking (0-8)
    private int posicionY;                    // Posición Y en el parking (0-8)

    public Vehiculo(String matricula, String marca, int potenciaMotor,
                    int anioFabricacion, double precio) {
        // Validar potencia del motor (según enunciado debe ser 50/100/150/200)
        if (potenciaMotor != 50 && potenciaMotor != 100 &&
                potenciaMotor != 150 && potenciaMotor != 200) {
            throw new IllegalArgumentException("La potencia del motor debe ser 50, 100, 150 o 200");
        }

        this.matricula = matricula;
        this.marca = marca;
        this.potenciaMotor = potenciaMotor;
        this.anioFabricacion = anioFabricacion;
        this.precio = precio;
        this.posicionX = -1; // No asignado a ninguna posición
        this.posicionY = -1; // No asignado a ninguna posición
    }

    public Vehiculo(String matricula, String marca, int potenciaMotor,
                    int anioFabricacion, double precio,
                    int posicionX, int posicionY) {
        this.matricula = matricula;
        this.marca = marca;
        this.potenciaMotor = potenciaMotor;
        this.anioFabricacion = anioFabricacion;
        this.precio = precio;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    public abstract String getTipoVehiculo();

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getPotenciaMotor() {
        return potenciaMotor;
    }

    public void setPotenciaMotor(int potenciaMotor) {
        // Validar potencia del motor (según enunciado debe ser 50/100/150/200)
        if (potenciaMotor != 50 && potenciaMotor != 100 &&
                potenciaMotor != 150 && potenciaMotor != 200) {
            throw new IllegalArgumentException("La potencia del motor debe ser 50, 100, 150 o 200");
        }
        this.potenciaMotor = potenciaMotor;
    }

    public int getAnioFabricacion() {
        return anioFabricacion;
    }

    public void setAnioFabricacion(int anioFabricacion) {
        this.anioFabricacion = anioFabricacion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(int posicionX) {
        this.posicionX = posicionX;
    }

    public int getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(int posicionY) {
        this.posicionY = posicionY;
    }

    public void asignarPosicionParking(int x, int y) {
        this.posicionX = x;
        this.posicionY = y;
    }

    public boolean estaAparcado() {
        return posicionX >= 0 && posicionY >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return Objects.equals(matricula, vehiculo.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula);
    }

    @Override
    public String toString() {
        return String.format("%s - Matrícula: %s - Marca: %s - Potencia: %d CV - Año: %d - Precio: %.2f€ - Posición: [%d,%d]",
                getTipoVehiculo(), matricula, marca, potenciaMotor, anioFabricacion, precio, posicionX, posicionY);
    }
}