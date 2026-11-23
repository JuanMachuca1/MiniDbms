/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.ui;

import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import minidbms.comun.ErrorBD;
import minidbms.metadata.Campo;
import minidbms.metadata.TipoDato;
import minidbms.metadata.Valores;

public class DialogoCampo {

    @FunctionalInterface
    public interface Destino {
        void aplicar(Campo campo) throws ErrorBD;
    }

    private final JTextField nombre = new JTextField(20);
    private final JComboBox<TipoDato> tipo = new JComboBox<>(TipoDato.values());
    private final JTextField longitud = new JTextField();
    private final JCheckBox obligatorio = new JCheckBox("Sí");
    private final JCheckBox primaria = new JCheckBox("Sí");
    private final JTextField defecto = new JTextField();
    private final int id;

    public DialogoCampo(Campo inicial) {
        id = inicial == null ? 0 : inicial.id();
        if (inicial != null) {
            nombre.setText(inicial.nombre());
            tipo.setSelectedItem(inicial.tipo());
            longitud.setText(inicial.longMax() > 0 ? "" + inicial.longMax() : "");
            obligatorio.setSelected(inicial.obligatorio());
            primaria.setSelected(inicial.llavePrimaria());
            defecto.setText(Valores.aTexto(inicial.valorPorDefecto()));
        }
        tipo.addActionListener(e -> actualizar());
        primaria.addActionListener(e -> actualizar());
        actualizar();
    }

    private void actualizar() {
        longitud.setEnabled(tipo.getSelectedItem() == TipoDato.TEXTO);
        if (primaria.isSelected()) {
            obligatorio.setSelected(true);
            defecto.setText("");
        }
        obligatorio.setEnabled(!primaria.isSelected());
        defecto.setEnabled(!primaria.isSelected());
    }
    public boolean mostrar(Component padre, String titulo, Destino destino) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);
        panel.add(new JLabel("Tipo:"));
        panel.add(tipo);
        panel.add(new JLabel("Longitud máxima (solo texto):"));
        panel.add(longitud);
        panel.add(new JLabel("Obligatorio:"));
        panel.add(obligatorio);
        panel.add(new JLabel("Llave primaria:"));
        panel.add(primaria);
        panel.add(new JLabel("Valor por defecto (opcional):"));
        panel.add(defecto);
        while (true) {
            int r = JOptionPane.showConfirmDialog(padre, panel, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                destino.aplicar(leer());
                return true;
            } catch (ErrorBD e) {
                JOptionPane.showMessageDialog(padre, e.getMessage(), "Revise el campo",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private Campo leer() throws ErrorBD {
        TipoDato t = (TipoDato) tipo.getSelectedItem();
        int largo = 0;
        if (t == TipoDato.TEXTO) {
            try {
                largo = Integer.parseInt(longitud.getText().strip());
            } catch (NumberFormatException e) {
                throw new ErrorBD("La longitud debe ser un número entero, por ejemplo 40");
            }
        }
        Object valor = Valores.convertir(t, defecto.getText());
        return new Campo(id, nombre.getText().strip(), t, largo,
                obligatorio.isSelected(), primaria.isSelected(), valor);
    }
}
