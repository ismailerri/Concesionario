package main;

import vista.gui.VistaGUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando sistema de gestión de concesionario...");

        // Iniciar con interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            try {
                // Intentar usar el look and feel del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            VistaGUI gui = new VistaGUI();
            gui.setVisible(true);
        });
    }
}