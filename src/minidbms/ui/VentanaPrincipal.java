/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;
import minidbms.metadata.Campo;
import minidbms.metadata.DefIndice;
import minidbms.metadata.LlaveForanea;
import minidbms.nucleo.ArchivoBD;

public class VentanaPrincipal extends JFrame {

    @FunctionalInterface
    public interface Accion {
        void ejecutar() throws Exception;
    }

    private ArchivoBD archivo;
    private File carpeta = new File("datos");
    private final JTextArea resumen = new JTextArea();
    private final JLabel estado = new JLabel(" ");
    
     public VentanaPrincipal() {
        super("Mini DBMS");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ejecutar(VentanaPrincipal.this::salir);
            }
        });
        setJMenuBar(crearMenu());
        resumen.setEditable(false);
        resumen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(new JScrollPane(resumen), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        setSize(900, 600);
        setLocationRelativeTo(null);
        refrescar();
    }

    private JMenuBar crearMenu() {
        JMenu menu = new JMenu("Archivo");
        agregar(menu, "Nuevo...", KeyEvent.VK_N, this::nuevo);
        agregar(menu, "Abrir...", KeyEvent.VK_O, this::abrir);
        agregar(menu, "Guardar", KeyEvent.VK_S, this::guardar);
        agregar(menu, "Cerrar", KeyEvent.VK_W, this::cerrar);
        menu.addSeparator();
        agregar(menu, "Salir", KeyEvent.VK_Q, this::salir);
        JMenuBar barra = new JMenuBar();
        barra.add(menu);
        return barra;
    }

    private void agregar(JMenu menu, String texto, int tecla, Accion accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> ejecutar(accion));
        menu.add(item);
    }

    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (ErrorBD e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error de disco: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        refrescar();
    }
    // ---------- MENU ARCHIVO ----------

    private void nuevo() throws Exception {
        if (!soltarArchivo()) {
            return;
        }
        JTextField nombre = new JTextField();
        JComboBox<Politica> politica = new JComboBox<>(Politica.values());
        JSpinner minimo = new JSpinner(new SpinnerNumberModel(32, 25, 100000, 1));
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Nombre del archivo:"));
        panel.add(nombre);
        panel.add(new JLabel("Política de la avail list:"));
        panel.add(politica);
        panel.add(new JLabel("Tamaño mínimo de hueco (bytes):"));
        panel.add(minimo);
        int r = JOptionPane.showConfirmDialog(this, panel, "Nuevo archivo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        carpeta.mkdirs();
        JFileChooser elegir = new JFileChooser(carpeta);
        elegir.setDialogTitle("¿En qué carpeta se guarda?");
        elegir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (elegir.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        carpeta = elegir.getSelectedFile();
        archivo = ArchivoBD.crear(carpeta, nombre.getText().strip(),
                (Politica) politica.getSelectedItem(), (Integer) minimo.getValue());
        estado.setText("Creado " + archivo.ruta());
    }

    private void abrir() throws Exception {
        if (!soltarArchivo()) {
            return;
        }
        carpeta.mkdirs();
        JFileChooser elegir = new JFileChooser(carpeta);
        elegir.setFileFilter(new FileNameExtensionFilter("Archivos Mini DBMS (*.mdat)", "mdat"));
        if (elegir.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File elegido = elegir.getSelectedFile();
        carpeta = elegido.getParentFile();
        archivo = ArchivoBD.abrir(elegido);
        estado.setText("Abierto " + elegido);
    }
    private void guardar() throws Exception {
        if (archivo == null) {
            throw new ErrorBD("No hay ningún archivo abierto");
        }
        archivo.guardar();
        estado.setText("Guardado " + archivo.ruta());
    }

    private void cerrar() throws Exception {
        soltarArchivo();
    }

    private void salir() throws Exception {
        if (soltarArchivo()) {
            dispose();
            System.exit(0);
        }
    }

    private boolean soltarArchivo() throws Exception {
        if (archivo == null) {
            return true;
        }
        if (archivo.hayCambios()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Guardar los cambios de " + archivo.metadata().nombre() + "?",
                    "Cambios sin guardar", JOptionPane.YES_NO_CANCEL_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                archivo.guardar();
            } else if (r != JOptionPane.NO_OPTION) {
                return false;
            }
        }
        archivo.close();
        archivo = null;
        estado.setText(" ");
        return true;
    }
    // ---------- PANTALLA ----------

    private void refrescar() {
        if (archivo == null) {
            setTitle("Mini DBMS");
            resumen.setText("\n  Use Archivo > Nuevo o Archivo > Abrir.");
            return;
        }
        boolean cambios;
        try {
            cambios = archivo.hayCambios();
        } catch (IOException e) {
            cambios = true;
        }
        setTitle("Mini DBMS - " + archivo.metadata().nombre() + (cambios ? " *" : ""));
        StringBuilder t = new StringBuilder();
        t.append("Archivo: ").append(archivo.ruta()).append('\n');
        t.append("Política: ").append(archivo.encabezado().politica)
                .append("   Hueco mínimo: ").append(archivo.encabezado().tamMinHueco)
                .append(" bytes\n\nCAMPOS\n");
        for (Campo c : archivo.metadata().campos()) {
            t.append(String.format("  %-3d %-20s %-9s %5s  %s%s%n", c.id(), c.nombre(),
                    c.tipo(), c.longMax() > 0 ? c.longMax() : "",
                    c.obligatorio() ? "obligatorio " : "",
                    c.llavePrimaria() ? "LLAVE PRIMARIA" : ""));
        }
        t.append("\nINDICES\n");
        for (DefIndice i : archivo.metadata().indices()) {
            t.append("  campo ").append(i.idCampo()).append("  orden ").append(i.orden())
                    .append(i.unico() ? "  único  " : "  no único  ").append(i.archivo())
                    .append('\n');
        }
        t.append("\nLLAVES FORANEAS\n");
        for (LlaveForanea f : archivo.metadata().foraneas()) {
            t.append("  campo ").append(f.idCampo()).append(" -> ")
                    .append(f.archivoReferido()).append('\n');
        }
        resumen.setText(t.toString());
    }
}
