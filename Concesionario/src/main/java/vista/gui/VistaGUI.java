package vista.gui;

import controlador.ControladorVehiculos;
import modelo.FabricaVehiculos;
import modelo.vehiculo.Coche;
import modelo.vehiculo.Tanque;
import modelo.vehiculo.Tractor;
import modelo.vehiculo.Vehiculo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;

public class VistaGUI extends JFrame {

    private final ControladorVehiculos controlador;
    private JTabbedPane tabbedPane;
    private JTable tablaVehiculos;
    private DefaultTableModel modeloTabla;
    private JPanel panelParking;
    private JButton[][] botonesParking;
    private JButton btnCrearVehiculos;

    public VistaGUI() {
        this.controlador = new ControladorVehiculos();

        // Configuración básica de la ventana
        setTitle("Gestor de Concesionario");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Agregar evento de cierre para liberar recursos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controlador.cerrar();
            }
        });

        // Inicializar componentes de la interfaz
        inicializarComponentes();

        // Cargar datos iniciales
        actualizarTablaVehiculos();
        actualizarVistaParking();

        // Verificar si hay vehículos para habilitar/deshabilitar el botón de crear
        verificarBotonCrearVehiculos();
    }

    private void inicializarComponentes() {
        // Panel principal con pestañas
        tabbedPane = new JTabbedPane();

        // Pestaña de gestión de vehículos
        JPanel panelGestionVehiculos = crearPanelGestionVehiculos();
        tabbedPane.addTab("Gestión de Vehículos", panelGestionVehiculos);

        // Pestaña de gestión de parking
        JPanel panelGestionParking = crearPanelGestionParking();
        tabbedPane.addTab("Gestión de Parking", panelGestionParking);

        // Agregar panel de pestañas al frame
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelGestionVehiculos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tabla de vehículos
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa en la tabla
            }
        };

        modeloTabla.addColumn("Matrícula");
        modeloTabla.addColumn("Tipo");
        modeloTabla.addColumn("Marca");
        modeloTabla.addColumn("Modelo");
        modeloTabla.addColumn("Potencia");
        modeloTabla.addColumn("Año");
        modeloTabla.addColumn("Precio (€)");
        modeloTabla.addColumn("Posición");
        modeloTabla.addColumn("Atributos Específicos");

        tablaVehiculos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaVehiculos);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();

        btnCrearVehiculos = new JButton("Crear Conjunto de Vehículos");
        btnCrearVehiculos.addActionListener(e -> crearConjuntoVehiculos());

        JButton btnEliminarVehiculo = new JButton("Eliminar Vehículo");
        btnEliminarVehiculo.addActionListener(e -> {
            int filaSeleccionada = tablaVehiculos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                String matricula = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
                eliminarVehiculo(matricula);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un vehículo para eliminar",
                        "Eliminar Vehículo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnActualizar = new JButton("Actualizar Lista");
        btnActualizar.addActionListener(e -> {
            actualizarTablaVehiculos();
            verificarBotonCrearVehiculos();
        });

        panelBotones.add(btnCrearVehiculos);
        panelBotones.add(btnEliminarVehiculo);
        panelBotones.add(btnActualizar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelGestionParking() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel del parking (matriz 9x9)
        panelParking = new JPanel(new GridLayout(9, 9, 2, 2));
        botonesParking = new JButton[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int fila = i;
                final int columna = j;

                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(60, 60));
                boton.addActionListener(e -> gestionarPlazaParking(columna, fila));

                botonesParking[i][j] = boton;
                panelParking.add(boton);
            }
        }

        panel.add(panelParking, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();

        JButton btnActualizarParking = new JButton("Actualizar Parking");
        btnActualizarParking.addActionListener(e -> actualizarVistaParking());

        JButton btnEstacionarVehiculo = new JButton("Estacionar Vehículo");
        btnEstacionarVehiculo.addActionListener(e -> mostrarDialogoEstacionarVehiculo());

        JButton btnRetirarVehiculo = new JButton("Retirar Vehículo");
        btnRetirarVehiculo.addActionListener(e -> mostrarDialogoRetirarVehiculo());

        panelBotones.add(btnActualizarParking);
        panelBotones.add(btnEstacionarVehiculo);
        panelBotones.add(btnRetirarVehiculo);

        panel.add(panelBotones, BorderLayout.SOUTH);

        // Leyenda
        JPanel panelLeyenda = new JPanel();
        panelLeyenda.add(new JLabel("Leyenda: "));

        JLabel labelCoche = new JLabel("Coche");
        labelCoche.setOpaque(true);
        labelCoche.setBackground(Color.GREEN);
        labelCoche.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        labelCoche.setPreferredSize(new Dimension(60, 20));
        panelLeyenda.add(labelCoche);

        JLabel labelTractor = new JLabel("Tractor");
        labelTractor.setOpaque(true);
        labelTractor.setBackground(Color.YELLOW);
        labelTractor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        labelTractor.setPreferredSize(new Dimension(60, 20));
        panelLeyenda.add(labelTractor);

        JLabel labelTanque = new JLabel("Tanque");
        labelTanque.setOpaque(true);
        labelTanque.setBackground(Color.RED);
        labelTanque.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        labelTanque.setPreferredSize(new Dimension(60, 20));
        panelLeyenda.add(labelTanque);

        JLabel labelVacio = new JLabel("Vacío");
        labelVacio.setOpaque(true);
        labelVacio.setBackground(Color.LIGHT_GRAY);
        labelVacio.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        labelVacio.setPreferredSize(new Dimension(60, 20));
        panelLeyenda.add(labelVacio);

        panel.add(panelLeyenda, BorderLayout.NORTH);

        return panel;
    }

    private void actualizarTablaVehiculos() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener todos los vehículos y agregarlos a la tabla
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();

        for (Vehiculo vehiculo : vehiculos) {
            String posicion = vehiculo.estaAparcado() ?
                    "[" + vehiculo.getPosicionX() + "," + vehiculo.getPosicionY() + "]" : "No estacionado";

            String atributosEspecificos = "";
            if (vehiculo instanceof Coche) {
                Coche coche = (Coche) vehiculo;
                atributosEspecificos = "Puertas: " + coche.getNumeroPuertas() +
                        ", Combustible: " + coche.getTipoCombustible() +
                        ", " + (coche.isAutomatico() ? "Automático" : "Manual");
            } else if (vehiculo instanceof Tractor) {
                Tractor tractor = (Tractor) vehiculo;
                atributosEspecificos = "Artilugio: " + tractor.getArtilugio() +
                        ", Tipo: " + tractor.getTipoTractor() +
                        ", " + (tractor.hasCabina() ? "Con cabina" : "Sin cabina");
            } else if (vehiculo instanceof Tanque) {
                Tanque tanque = (Tanque) vehiculo;
                atributosEspecificos = "Armamento: " + tanque.getArmamento() +
                        ", Blindaje: " + tanque.getBlindajeMM() + " mm" +
                        ", Velocidad: " + tanque.getVelocidadMaxima() + " km/h";
            }

            modeloTabla.addRow(new Object[]{
                    vehiculo.getMatricula(),
                    vehiculo.getTipoVehiculo(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getPotenciaMotor() + " CV",
                    vehiculo.getAnioFabricacion(),
                    String.format("%.2f", vehiculo.getPrecio()),
                    posicion,
                    atributosEspecificos
            });
        }
    }

    private void actualizarVistaParking() {
        Vehiculo[][] matriz = controlador.obtenerMatrizParking();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Vehiculo vehiculo = matriz[i][j];
                JButton boton = botonesParking[i][j];

                if (vehiculo != null) {
                    // Establecer color según tipo de vehículo
                    switch (vehiculo.getTipoVehiculo().toUpperCase()) {
                        case "COCHE":
                            boton.setBackground(Color.GREEN);
                            break;
                        case "TRACTOR":
                            boton.setBackground(Color.YELLOW);
                            break;
                        case "TANQUE":
                            boton.setBackground(Color.RED);
                            break;
                    }

                    // Mostrar inicial del tipo + matrícula abreviada
                    boton.setText(vehiculo.getTipoVehiculo().charAt(0) + ": " +
                            vehiculo.getMatricula().substring(0, 4));
                    boton.setToolTipText(vehiculo.toString());
                } else {
                    // Plaza vacía
                    boton.setBackground(Color.LIGHT_GRAY);
                    boton.setText("[" + j + "," + i + "]");
                    boton.setToolTipText("Plaza vacía en posición [" + j + "," + i + "]");
                }
            }
        }
    }

    private void verificarBotonCrearVehiculos() {
        // Verificar si ya hay vehículos en el sistema
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();

        // Si hay vehículos, el botón solo debe estar activo si el número es múltiplo de 3
        btnCrearVehiculos.setEnabled(vehiculos.size() % 3 == 0);

        if (vehiculos.size() % 3 != 0) {
            btnCrearVehiculos.setToolTipText("Para crear nuevos vehículos, el número total debe ser múltiplo de 3");
        } else {
            btnCrearVehiculos.setToolTipText("Crear un nuevo conjunto de 3 vehículos");
        }
    }

    private void crearConjuntoVehiculos() {
        List<Vehiculo> vehiculos = controlador.crearConjuntoVehiculos();

        if (vehiculos != null) {
            JOptionPane.showMessageDialog(this,
                    "Se han creado 3 nuevos vehículos correctamente:\n" +
                            "- Coche: " + vehiculos.get(0).getMatricula() + "\n" +
                            "- Tractor: " + vehiculos.get(1).getMatricula() + "\n" +
                            "- Tanque: " + vehiculos.get(2).getMatricula(),
                    "Vehículos Creados", JOptionPane.INFORMATION_MESSAGE);

            actualizarTablaVehiculos();
            verificarBotonCrearVehiculos();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al crear los vehículos. Inténtelo de nuevo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarVehiculo(String matricula) {
        Optional<Vehiculo> optVehiculo = controlador.buscarVehiculo(matricula);

        if (!optVehiculo.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró el vehículo con matrícula: " + matricula,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehiculo vehiculo = optVehiculo.get();

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el vehículo?\n" + vehiculo,
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controlador.eliminarVehiculo(matricula);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Vehículo eliminado correctamente.",
                        "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);

                actualizarTablaVehiculos();
                actualizarVistaParking();
                verificarBotonCrearVehiculos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar el vehículo.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void gestionarPlazaParking(int x, int y) {
        Optional<Vehiculo> optVehiculo = controlador.obtenerVehiculoEnPosicion(x, y);

        if (optVehiculo.isPresent()) {
            // Si hay un vehículo, mostrar información y opciones
            Vehiculo vehiculo = optVehiculo.get();

            // Crear opciones según el tipo de vehículo
            String[] opciones;
            if (vehiculo instanceof Tanque) {
                opciones = new String[]{"Ver Detalles", "Retirar del Parking", "Disparar", "Cancelar"};
            } else {
                opciones = new String[]{"Ver Detalles", "Retirar del Parking", "Cancelar"};
            }

            int seleccion = JOptionPane.showOptionDialog(this,
                    "Plaza ocupada por:\n" + vehiculo.getTipoVehiculo() + " - " + vehiculo.getMarca() + " " + vehiculo.getModelo(),
                    "Plaza [" + x + "," + y + "]",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]);

            switch (seleccion) {
                case 0: // Ver Detalles
                    JOptionPane.showMessageDialog(this,
                            vehiculo.toString(),
                            "Detalles del Vehículo", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 1: // Retirar del Parking
                    boolean retirado = controlador.retirarVehiculoDeParking(vehiculo.getMatricula());
                    if (retirado) {
                        JOptionPane.showMessageDialog(this,
                                "Vehículo retirado correctamente del parking.",
                                "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        actualizarVistaParking();
                        actualizarTablaVehiculos();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al retirar el vehículo del parking.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;

                case 2: // Disparar (solo para tanques) o Cancelar
                    if (vehiculo instanceof Tanque) {
                        boolean disparado = controlador.dispararTanque(vehiculo.getMatricula());
                        if (disparado) {
                            JOptionPane.showMessageDialog(this,
                                    "¡El tanque ha disparado y ha eliminado un vehículo!",
                                    "Disparo Exitoso", JOptionPane.INFORMATION_MESSAGE);
                            actualizarVistaParking();
                            actualizarTablaVehiculos();
                            verificarBotonCrearVehiculos();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No hay ningún vehículo delante para disparar.",
                                    "Disparo Fallido", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    break;
            }
        } else {
            // Si no hay vehículo, dar opción de estacionar uno
            String[] opciones = {"Estacionar Vehículo Aquí", "Cancelar"};
            int seleccion = JOptionPane.showOptionDialog(this,
                    "Plaza vacía en posición [" + x + "," + y + "]",
                    "Plaza Vacía",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]);

            if (seleccion == 0) {
                mostrarDialogoEstacionarVehiculoEnPosicion(x, y);
            }
        }
    }

    private void mostrarDialogoEstacionarVehiculo() {
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();
        List<Vehiculo> vehiculosNoEstacionados = new java.util.ArrayList<>();

        // Filtrar solo vehículos no estacionados
        for (Vehiculo v : vehiculos) {
            if (!v.estaAparcado()) {
                vehiculosNoEstacionados.add(v);
            }
        }

        if (vehiculosNoEstacionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vehículos disponibles para estacionar.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear lista de opciones con marca, modelo y matrícula
        String[] opciones = new String[vehiculosNoEstacionados.size()];
        for (int i = 0; i < vehiculosNoEstacionados.size(); i++) {
            Vehiculo v = vehiculosNoEstacionados.get(i);
            opciones[i] = v.getTipoVehiculo() + ": " + v.getMarca() + " " + v.getModelo() +
                    " (Matrícula: " + v.getMatricula() + ")";
        }

        // Mostrar diálogo de selección
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione un vehículo para estacionar:",
                "Estacionar Vehículo", JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion != null) {
            // Extraer matrícula del vehículo seleccionado
            String matricula = seleccion.substring(seleccion.lastIndexOf("Matrícula: ") + 11, seleccion.length() - 1);

            // Solicitar posición
            JPanel panelPosicion = new JPanel(new GridLayout(2, 2));
            JTextField txtX = new JTextField(5);
            JTextField txtY = new JTextField(5);
            panelPosicion.add(new JLabel("Posición X (0-8):"));
            panelPosicion.add(txtX);
            panelPosicion.add(new JLabel("Posición Y (0-8):"));
            panelPosicion.add(txtY);

            int resultado = JOptionPane.showConfirmDialog(this, panelPosicion,
                    "Introducir Posición", JOptionPane.OK_CANCEL_OPTION);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    int x = Integer.parseInt(txtX.getText().trim());
                    int y = Integer.parseInt(txtY.getText().trim());

                    if (x < 0 || x > 8 || y < 0 || y > 8) {
                        JOptionPane.showMessageDialog(this,
                                "Las coordenadas deben estar entre 0 y 8.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (controlador.plazaOcupada(x, y)) {
                        JOptionPane.showMessageDialog(this,
                                "La plaza [" + x + "," + y + "] ya está ocupada.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean estacionado = controlador.estacionarVehiculo(matricula, x, y);

                    if (estacionado) {
                        JOptionPane.showMessageDialog(this,
                                "Vehículo estacionado correctamente en la posición [" + x + "," + y + "].",
                                "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        actualizarVistaParking();
                        actualizarTablaVehiculos();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al estacionar el vehículo.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Las coordenadas deben ser números enteros.",
                            "Error de Formato", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void mostrarDialogoEstacionarVehiculoEnPosicion(int x, int y) {
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();
        List<Vehiculo> vehiculosNoEstacionados = new java.util.ArrayList<>();

        // Filtrar solo vehículos no estacionados
        for (Vehiculo v : vehiculos) {
            if (!v.estaAparcado()) {
                vehiculosNoEstacionados.add(v);
            }
        }

        if (vehiculosNoEstacionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vehículos disponibles para estacionar.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear lista de opciones con marca, modelo y matrícula
        String[] opciones = new String[vehiculosNoEstacionados.size()];
        for (int i = 0; i < vehiculosNoEstacionados.size(); i++) {
            Vehiculo v = vehiculosNoEstacionados.get(i);
            opciones[i] = v.getTipoVehiculo() + ": " + v.getMarca() + " " + v.getModelo() +
                    " (Matrícula: " + v.getMatricula() + ")";
        }

        // Mostrar diálogo de selección
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione un vehículo para estacionar en la posición [" + x + "," + y + "]:",
                "Estacionar Vehículo", JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion != null) {
            // Extraer matrícula del vehículo seleccionado
            String matricula = seleccion.substring(seleccion.lastIndexOf("Matrícula: ") + 11, seleccion.length() - 1);

            boolean estacionado = controlador.estacionarVehiculo(matricula, x, y);

            if (estacionado) {
                JOptionPane.showMessageDialog(this,
                        "Vehículo estacionado correctamente en la posición [" + x + "," + y + "].",
                        "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                actualizarVistaParking();
                actualizarTablaVehiculos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al estacionar el vehículo.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoRetirarVehiculo() {
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();
        List<Vehiculo> vehiculosEstacionados = new java.util.ArrayList<>();

        // Filtrar solo vehículos estacionados
        for (Vehiculo v : vehiculos) {
            if (v.estaAparcado()) {
                vehiculosEstacionados.add(v);
            }
        }

        if (vehiculosEstacionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vehículos estacionados en el parking.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear lista de opciones con marca, modelo, posición y matrícula
        String[] opciones = new String[vehiculosEstacionados.size()];
        for (int i = 0; i < vehiculosEstacionados.size(); i++) {
            Vehiculo v = vehiculosEstacionados.get(i);
            opciones[i] = v.getTipoVehiculo() + ": " + v.getMarca() + " " + v.getModelo() +
                    " [" + v.getPosicionX() + "," + v.getPosicionY() + "] (Matrícula: " + v.getMatricula() + ")";
        }

        // Mostrar diálogo de selección
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione un vehículo para retirar del parking:",
                "Retirar Vehículo", JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion != null) {
            // Extraer matrícula del vehículo seleccionado
            String matricula = seleccion.substring(seleccion.lastIndexOf("Matrícula: ") + 11, seleccion.length() - 1);

            boolean retirado = controlador.retirarVehiculoDeParking(matricula);

            if (retirado) {
                JOptionPane.showMessageDialog(this,
                        "Vehículo retirado correctamente del parking.",
                        "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                actualizarVistaParking();
                actualizarTablaVehiculos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al retirar el vehículo del parking.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

}