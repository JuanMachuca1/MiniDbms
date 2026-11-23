/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;
import minidbms.metadata.Campo;
import minidbms.metadata.DefIndice;
import minidbms.metadata.Metadata;
import minidbms.nucleo.ArchivoBD;

public class VentanaPrincipal extends JFrame {

    @FunctionalInterface
    public interface Accion {
        void ejecutar() throws Exception;
    }

    private ArchivoBD archivo;
    private File carpeta = new File("datos");
    private final ModeloTablaCampos modelo = new ModeloTablaCampos();
    private final JTable tabla = new JTable(modelo);
    private final JLabel info = new JLabel();
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
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ejecutar(VentanaPrincipal.this::modificarCampo);
                }
            }
        });
        info.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        estado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(info, BorderLayout.NORTH);
        arriba.add(crearBotones(), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        refrescar();
    }

    private JMenuBar crearMenu() {
        JMenu archivoMenu = new JMenu("Archivo");
        agregar(archivoMenu, "Nuevo...", KeyEvent.VK_N, this::nuevo);
        agregar(archivoMenu, "Abrir...", KeyEvent.VK_O, this::abrir);
        agregar(archivoMenu, "Guardar", KeyEvent.VK_S, this::guardar);
        agregar(archivoMenu, "Cerrar", KeyEvent.VK_W, this::cerrar);
        archivoMenu.addSeparator();
        agregar(archivoMenu, "Propiedades...", 0, this::propiedades);
        archivoMenu.addSeparator();
        agregar(archivoMenu, "Salir", KeyEvent.VK_Q, this::salir);

        JMenu camposMenu = new JMenu("Campos");
        agregar(camposMenu, "Agregar campo...", KeyEvent.VK_A, this::agregarCampo);
        agregar(camposMenu, "Modificar campo...", KeyEvent.VK_E, this::modificarCampo);
        agregar(camposMenu, "Borrar campo", 0, this::borrarCampo);
        camposMenu.addSeparator();
        agregar(camposMenu, "Definir índice...", 0, this::definirIndice);
        agregar(camposMenu, "Quitar índice", 0, this::quitarIndice);
        camposMenu.addSeparator();
        agregar(camposMenu, "Definir llave foránea...", 0, this::definirForanea);
        agregar(camposMenu, "Quitar llave foránea", 0, this::quitarForanea);

        JMenuBar barra = new JMenuBar();
        barra.add(archivoMenu);
        barra.add(camposMenu);
        return barra;
    }

    private JPanel crearBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boton(botones, "Agregar campo", this::agregarCampo);
        boton(botones, "Modificar", this::modificarCampo);
        boton(botones, "Borrar", this::borrarCampo);
        boton(botones, "Índice...", this::definirIndice);
        boton(botones, "Llave foránea...", this::definirForanea);
        boton(botones, "Guardar", this::guardar);
        return botones;
    }

    private void boton(JPanel panel, String texto, Accion accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> ejecutar(accion));
        panel.add(b);
    }

    private void agregar(JMenu menu, String texto, int tecla, Accion accion) {
        JMenuItem item = new JMenuItem(texto);
        if (tecla != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK));
        }
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
        exigirArchivo();
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

    private void propiedades() throws Exception {
        exigirArchivo();
        JComboBox<Politica> politica = new JComboBox<>(Politica.values());
        politica.setSelectedItem(archivo.encabezado().politica);
        JSpinner minimo = new JSpinner(new SpinnerNumberModel(
                archivo.encabezado().tamMinHueco, 25, 100000, 1));
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Política de la avail list:"));
        panel.add(politica);
        panel.add(new JLabel("Tamaño mínimo de hueco (bytes):"));
        panel.add(minimo);
        int r = JOptionPane.showConfirmDialog(this, panel, "Propiedades",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            archivo.encabezado().politica = (Politica) politica.getSelectedItem();
            archivo.encabezado().tamMinHueco = (Integer) minimo.getValue();
        }
    }
        // ---------- MENU CAMPOS ----------

    private void agregarCampo() throws Exception {
        exigirArchivo();
        new DialogoCampo(null).mostrar(this, "Agregar campo",
                archivo.metadata()::agregarCampo);
    }

    private void modificarCampo() throws Exception {
        Campo c = campoSeleccionado();
        new DialogoCampo(c).mostrar(this, "Modificar " + c.nombre(),
                archivo.metadata()::modificarCampo);
    }

    private void borrarCampo() throws Exception {
        Campo c = campoSeleccionado();
        int r = JOptionPane.showConfirmDialog(this, "¿Borrar el campo " + c.nombre() + "?",
                "Borrar campo", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            archivo.metadata().borrarCampo(c.id());
        }
    }

    private void definirIndice() throws Exception {
        Campo c = campoSeleccionado();
        DefIndice actual = archivo.metadata().indiceDe(c.id());
        JComboBox<String> clase = new JComboBox<>(new String[]{"Único", "No único"});
        clase.setSelectedIndex(actual != null && !actual.unico() ? 1 : 0);
        clase.setEnabled(!c.llavePrimaria());
        JSpinner orden = new JSpinner(new SpinnerNumberModel(
                actual == null ? Metadata.ORDEN_POR_DEFECTO : actual.orden(), 3, 1000, 1));
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Índice:"));
        panel.add(clase);
        panel.add(new JLabel("Orden del árbol B:"));
        panel.add(orden);
        int r = JOptionPane.showConfirmDialog(this, panel, "Índice de " + c.nombre(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            archivo.metadata().definirIndice(c.id(), (Integer) orden.getValue(),
                    clase.getSelectedIndex() == 0);
        }
    }

    private void quitarIndice() throws Exception {
        archivo.metadata().quitarIndice(campoSeleccionado().id());
    }

    private void definirForanea() throws Exception {
        Campo c = campoSeleccionado();
        List<String> archivos = archivo.archivosDeLaCarpeta();
        if (archivos.isEmpty()) {
            throw new ErrorBD("No hay archivos .mdat en " + carpeta);
        }
        Object elegido = JOptionPane.showInputDialog(this,
                c.nombre() + " apunta a la llave primaria de:", "Llave foránea",
                JOptionPane.PLAIN_MESSAGE, null, archivos.toArray(), archivos.get(0));
        if (elegido != null) {
            archivo.definirForanea(c.id(), elegido.toString());
        }
    }

    private void quitarForanea() throws Exception {
        archivo.metadata().quitarForanea(campoSeleccionado().id());
    }

    private void exigirArchivo() throws ErrorBD {
        if (archivo == null) {
            throw new ErrorBD("Primero cree o abra un archivo");
        }
    }

    private Campo campoSeleccionado() throws ErrorBD {
        exigirArchivo();
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            throw new ErrorBD("Seleccione un campo en la tabla");
        }
        return modelo.campoEn(fila);
    }
        // ---------- PANTALLA ----------

    private void refrescar() {
        if (archivo == null) {
            setTitle("Mini DBMS");
            info.setText("Ningún archivo abierto. Use Archivo > Nuevo o Archivo > Abrir.");
            modelo.mostrar(null);
            return;
        }
        boolean cambios;
        try {
            cambios = archivo.hayCambios();
        } catch (IOException e) {
            cambios = true;
        }
        setTitle("Mini DBMS - " + archivo.metadata().nombre() + (cambios ? " *" : ""));
        info.setText("<html><b>" + archivo.metadata().nombre() + "</b>"
                + "  ·  " + archivo.metadata().campos().size() + " campos"
                + "  ·  Política: " + archivo.encabezado().politica
                + "  ·  Hueco mínimo: " + archivo.encabezado().tamMinHueco + " bytes"
                + (cambios ? "  ·  <font color='#c0392b'>cambios sin guardar</font>" : "")
                + "</html>");
        int fila = tabla.getSelectedRow();
        modelo.mostrar(archivo.metadata());
        if (fila >= 0 && fila < modelo.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
    }
} 