/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package minidbms;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import minidbms.ui.FrmPrincipal;


public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                
            }
            new FrmPrincipal().setVisible(true);
        });
    }
}