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
import java.util.ArrayList;
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

    // Definir colores y fuentes para la nueva interfaz
    private static final Color COLOR_BOTON = new Color(59, 153, 252);  // Color azul para botones
    private static final Color COLOR_FONDO = new Color(245, 245, 245);  // Color gris claro para el fondo
    private static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 28);
    private static final Font FUENTE_TEXTO = new Font("Arial", Font.PLAIN, 16);
    private static final Font FUENTE_BOTON = new Font("Arial", Font.BOLD, 18);

    public VistaGUI() {
        this.controlador = new ControladorVehiculos();

        // Configuración básica de la ventana
        setTitle("Gestor de Concesionario");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

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
        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.setBackground(COLOR_FONDO);

        // Panel de menú principal
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBackground(COLOR_FONDO);
        panelMenu.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Título
        JLabel lblTitulo = new JLabel("Bienvenido al concesionario de vehículos");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Texto descriptivo
        JTextArea txtDescripcion = new JTextArea(
                "Sistema de gestión integral para concesionarios de vehículos. " +
                        "Esta aplicación permite administrar un inventario de diferentes tipos de vehículos " +
                        "(coches, tractores y tanques militares), organizar su disposición en el parking " +
                        "y realizar operaciones básicas como añadir, mostrar, actualizar y eliminar vehículos. " +
                        "El sistema utiliza un patrón MVC y ofrece la posibilidad de persistencia de datos " +
                        "tanto en memoria como en bases de datos SQL y MongoDB."
        );
        txtDescripcion.setFont(FUENTE_TEXTO);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setEditable(false);
        txtDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtDescripcion.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        // Botones principales
        JButton btnAnadirVehiculos = crearBoton("Añadir vehículos");
        JButton btnMostrarVehiculos = crearBoton("Mostrar vehículos");
        JButton btnActualizarVehiculos = crearBoton("Actualizar vehículos");
        JButton btnEliminarVehiculos = crearBoton("Eliminar vehículos");
        JButton btnVerParking = crearBoton("Ver parking");
        JButton btnSalir = crearBoton("Salir del programa");

        // Acciones de los botones
        btnAnadirVehiculos.addActionListener(e -> mostrarDialogoCrearVehiculos());
        btnMostrarVehiculos.addActionListener(e -> mostrarDialogoListaVehiculos());
        btnActualizarVehiculos.addActionListener(e -> mostrarDialogoActualizarVehiculo());
        btnEliminarVehiculos.addActionListener(e -> mostrarDialogoEliminarVehiculo());
        btnVerParking.addActionListener(e -> mostrarDialogoVerParking());
        btnSalir.addActionListener(e -> {
            controlador.cerrar();
            System.exit(0);
        });

        // Agregar componentes al panel de menú
        panelMenu.add(lblTitulo);
        panelMenu.add(txtDescripcion);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 20)));
        panelMenu.add(btnAnadirVehiculos);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnMostrarVehiculos);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnActualizarVehiculos);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnEliminarVehiculos);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnVerParking);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnSalir);

        // Añadir el panel de menú al panel principal
        panelPrincipal.add(new JScrollPane(panelMenu), BorderLayout.CENTER);

        // Añadir el panel principal a la ventana
        getContentPane().add(panelPrincipal);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(500, 60));
        boton.setPreferredSize(new Dimension(500, 60));

        // Bordes redondeados
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        return boton;
    }

    // Método para mostrar diálogo de creación de vehículos
    private void mostrarDialogoCrearVehiculos() {
        List<Vehiculo> vehiculos = FabricaVehiculos.crearConjuntoVehiculos();

        if (vehiculos != null && !vehiculos.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Se han creado los siguientes vehículos:\n\n");

            for (Vehiculo v : vehiculos) {
                mensaje.append("- ").append(v.getTipoVehiculo()).append(": ")
                        .append(v.getMarca())
                        .append(" (").append(v.getMatricula()).append(")\n");

                // Guardar cada vehículo
                controlador.guardarVehiculo(v);
            }

            JOptionPane.showMessageDialog(this,
                    mensaje.toString(),
                    "Vehículos Creados",
                    JOptionPane.INFORMATION_MESSAGE);

            actualizarTablaVehiculos();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al crear los vehículos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar lista de vehículos
    private void mostrarDialogoListaVehiculos() {
        // Crear diálogo modal
        JDialog dialogo = new JDialog(this, "Lista de Vehículos", true);
        dialogo.setSize(800, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal del diálogo
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(COLOR_FONDO);

        // Tabla de vehículos
        String[] columnas = {"Matrícula", "Tipo", "Marca", "Modelo", "Potencia", "Año", "Precio (€)", "Posición"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Llenar la tabla con los vehículos
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();
        for (Vehiculo v : vehiculos) {
            String posicion = v.estaAparcado() ?
                    "[" + v.getPosicionX() + "," + v.getPosicionY() + "]" : "No estacionado";

            modeloTabla.addRow(new Object[]{
                    v.getMatricula(),
                    v.getTipoVehiculo(),
                    v.getMarca(),
                    "", // No modelo disponible
                    v.getPotenciaMotor() + " CV",
                    v.getAnioFabricacion(),
                    String.format("%.2f", v.getPrecio()),
                    posicion
            });
        }

        // Crear la tabla
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Arial", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Botón para cerrar el diálogo
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Panel para el botón
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setBackground(COLOR_FONDO);
        panelBoton.add(btnCerrar);

        // Añadir componentes
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);

        // Añadir panel al diálogo
        dialogo.add(panel);
        dialogo.setVisible(true);
    }

    // Método para mostrar diálogo de actualización de vehículo
    private void mostrarDialogoActualizarVehiculo() {
        // Obtener todos los vehículos
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();

        if (vehiculos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vehículos en el sistema",
                    "Actualizar Vehículo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Array para el combobox
        String[] opciones = new String[vehiculos.size()];
        for (int i = 0; i < vehiculos.size(); i++) {
            Vehiculo v = vehiculos.get(i);
            opciones[i] = v.getTipoVehiculo() + ": " + v.getMarca() +
                    " (Matrícula: " + v.getMatricula() + ")";
        }

        // Pedir al usuario que elija un vehículo
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione el vehículo a actualizar:",
                "Actualizar Vehículo",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion == null) return;

        // Obtener la matrícula del vehículo seleccionado
        String matricula = seleccion.substring(seleccion.lastIndexOf("Matrícula: ") + 11, seleccion.length() - 1);
        Optional<Vehiculo> optVehiculo = controlador.buscarVehiculo(matricula);

        if (!optVehiculo.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    "Vehículo no encontrado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehiculo vehiculo = optVehiculo.get();

        // Crear panel con campos para actualizar
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Campos comunes a todos los vehículos
        JTextField txtMarca = new JTextField(vehiculo.getMarca());
        // No hay modelo disponible
        JTextField txtPotencia = new JTextField(String.valueOf(vehiculo.getPotenciaMotor()));
        JTextField txtAnio = new JTextField(String.valueOf(vehiculo.getAnioFabricacion()));
        JTextField txtPrecio = new JTextField(String.valueOf(vehiculo.getPrecio()));

        panel.add(new JLabel("Matrícula:"));
        panel.add(new JLabel(vehiculo.getMatricula()));
        panel.add(new JLabel("Tipo:"));
        panel.add(new JLabel(vehiculo.getTipoVehiculo()));
        panel.add(new JLabel("Marca:"));
        panel.add(txtMarca);
        panel.add(new JLabel("Potencia (CV):"));
        panel.add(txtPotencia);
        panel.add(new JLabel("Año Fabricación:"));
        panel.add(txtAnio);
        panel.add(new JLabel("Precio (€):"));
        panel.add(txtPrecio);

        // No mostramos campos específicos según el tipo de vehículo
        // Solo permitimos editar los campos comunes a todos los vehículos

        // Mostrar diálogo
        int resultado = JOptionPane.showConfirmDialog(this,
                panel,
                "Actualizar Vehículo",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) return;

        try {
            // Actualizar campos comunes
            vehiculo.setMarca(txtMarca.getText());
            // No actualizamos modelo porque no existe
            vehiculo.setPotenciaMotor(Integer.parseInt(txtPotencia.getText()));
            vehiculo.setAnioFabricacion(Integer.parseInt(txtAnio.getText()));
            vehiculo.setPrecio(Double.parseDouble(txtPrecio.getText()));

            // No se actualizan los campos específicos de cada tipo de vehículo
            // Solo se actualizan los campos comunes a todos los vehículos

            // Actualizar en el controlador
            boolean actualizado = controlador.actualizarVehiculo(vehiculo);

            if (actualizado) {
                JOptionPane.showMessageDialog(this,
                        "Vehículo actualizado correctamente",
                        "Actualización Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaVehiculos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar el vehículo",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Error en el formato de los datos numéricos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar diálogo de eliminación de vehículo
    private void mostrarDialogoEliminarVehiculo() {
        // Obtener todos los vehículos
        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();

        if (vehiculos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vehículos en el sistema",
                    "Eliminar Vehículo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Array para el combobox
        String[] opciones = new String[vehiculos.size()];
        for (int i = 0; i < vehiculos.size(); i++) {
            Vehiculo v = vehiculos.get(i);
            opciones[i] = v.getTipoVehiculo() + ": " + v.getMarca() +
                    " (Matrícula: " + v.getMatricula() + ")";
        }

        // Pedir al usuario que elija un vehículo
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione el vehículo a eliminar:",
                "Eliminar Vehículo",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion == null) return;

        // Obtener la matrícula del vehículo seleccionado
        String matricula = seleccion.substring(seleccion.lastIndexOf("Matrícula: ") + 11, seleccion.length() - 1);

        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el vehículo con matrícula " + matricula + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) return;

        // Eliminar vehículo
        boolean eliminado = controlador.eliminarVehiculo(matricula);

        if (eliminado) {
            JOptionPane.showMessageDialog(this,
                    "Vehículo eliminado correctamente",
                    "Eliminación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            actualizarTablaVehiculos();
            actualizarVistaParking();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar el vehículo",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar el panel del parking
    private void mostrarDialogoVerParking() {
        // Crear diálogo modal
        JDialog dialogo = new JDialog(this, "Gestión de Parking", true);
        dialogo.setSize(800, 700);
        dialogo.setLocationRelativeTo(this);

        // Panel principal del diálogo
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(COLOR_FONDO);

        // Panel de la matriz del parking (9x9)
        JPanel panelParking = new JPanel(new GridLayout(9, 9, 2, 2));

        // Crear botones
        JButton[][] botonesParking = new JButton[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int fila = i;
                final int columna = j;

                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(70, 70));

                // Añadir acción al hacer clic
                boton.addActionListener(e -> {
                    if (controlador.plazaOcupada(columna, fila)) {
                        // Si hay un vehículo, mostrar opciones
                        Optional<Vehiculo> optVehiculo = controlador.obtenerVehiculoEnPosicion(columna, fila);
                        if (optVehiculo.isPresent()) {
                            Vehiculo v = optVehiculo.get();
                            JOptionPane.showMessageDialog(dialogo,
                                    "Plaza ocupada por: " + v.getTipoVehiculo() + " - " + v.getMarca() +
                                            " (" + v.getMatricula() + ")",
                                    "Información", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        // Si está vacía, intentar aparcar
                        List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();
                        List<Vehiculo> disponibles = new ArrayList<>();

                        // Filtrar vehículos no aparcados
                        for (Vehiculo v : vehiculos) {
                            if (!v.estaAparcado()) {
                                disponibles.add(v);
                            }
                        }

                        if (disponibles.isEmpty()) {
                            JOptionPane.showMessageDialog(dialogo,
                                    "No hay vehículos disponibles para aparcar",
                                    "Información", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        // Crear selector
                        String[] opciones = new String[disponibles.size()];
                        for (int k = 0; k < disponibles.size(); k++) {
                            opciones[k] = disponibles.get(k).getTipoVehiculo() + ": " +
                                    disponibles.get(k).getMarca() + " (" +
                                    disponibles.get(k).getMatricula() + ")";
                        }

                        String seleccion = (String) JOptionPane.showInputDialog(dialogo,
                                "Seleccione vehículo para aparcar en [" + columna + "," + fila + "]:",
                                "Aparcar vehículo", JOptionPane.QUESTION_MESSAGE,
                                null, opciones, opciones[0]);

                        if (seleccion != null) {
                            // Extraer matrícula
                            String matricula = seleccion.substring(
                                    seleccion.lastIndexOf("(") + 1,
                                    seleccion.lastIndexOf(")")
                            );

                            // Aparcar
                            if (controlador.estacionarVehiculo(matricula, columna, fila)) {
                                // Actualizar botones
                                actualizarVistaBotonesParking(botonesParking);
                            } else {
                                JOptionPane.showMessageDialog(dialogo,
                                        "Error al aparcar el vehículo",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });

                botonesParking[i][j] = boton;
                panelParking.add(boton);
            }
        }

        // Actualizar botones inicialmente
        actualizarVistaBotonesParking(botonesParking);

        // Leyenda para los colores
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLeyenda.setBackground(COLOR_FONDO);

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

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(COLOR_FONDO);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialogo.dispose());
        panelBotones.add(btnCerrar);

        // Añadir componentes al panel principal
        panel.add(panelLeyenda, BorderLayout.NORTH);
        panel.add(panelParking, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Añadir panel al diálogo
        dialogo.add(panel);
        dialogo.setVisible(true);
    }

    // Métodos existentes que se mantienen para compatibilidad pero ya no se usarán directamente
    private void actualizarTablaVehiculos() {
        if (modeloTabla != null) {
            // Limpiar tabla
            modeloTabla.setRowCount(0);

            // Obtener todos los vehículos y agregarlos a la tabla
            List<Vehiculo> vehiculos = controlador.obtenerTodosLosVehiculos();

            for (Vehiculo vehiculo : vehiculos) {
                String posicion = vehiculo.estaAparcado() ?
                        "[" + vehiculo.getPosicionX() + "," + vehiculo.getPosicionY() + "]" : "No estacionado";

                // Ya no mostramos los atributos específicos
                String atributosEspecificos = "";

                modeloTabla.addRow(new Object[]{
                        vehiculo.getMatricula(),
                        vehiculo.getTipoVehiculo(),
                        vehiculo.getMarca(),
                        "", // No hay modelo disponible
                        vehiculo.getPotenciaMotor() + " CV",
                        vehiculo.getAnioFabricacion(),
                        String.format("%.2f", vehiculo.getPrecio()),
                        posicion,
                        atributosEspecificos
                });
            }
        }
    }

    private void actualizarVistaParking() {
        if (botonesParking != null) {
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
    }

    // Método auxiliar para actualizar los botones del parking
    private void actualizarVistaBotonesParking(JButton[][] botones) {
        Vehiculo[][] matriz = controlador.obtenerMatrizParking();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Vehiculo vehiculo = matriz[i][j];
                JButton boton = botones[i][j];

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
                } else {
                    // Plaza vacía
                    boton.setBackground(Color.LIGHT_GRAY);
                    boton.setText("[" + j + "," + i + "]");
                }
            }
        }
    }

    private void verificarBotonCrearVehiculos() {
        if (btnCrearVehiculos != null) {
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
    }
}