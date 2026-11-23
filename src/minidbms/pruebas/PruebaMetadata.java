/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.pruebas;

import minidbms.comun.ErrorBD;
import minidbms.metadata.Campo;
import minidbms.metadata.Metadata;
import minidbms.metadata.TipoDato;
import minidbms.metadata.Validador;

public class PruebaMetadata {

    private static final Metadata m = new Metadata("Prueba");

    public static void main(String[] args) throws ErrorBD {
        agregar("Codigo", TipoDato.ENTERO, 0, true, true);
        agregar("Nombre", TipoDato.TEXTO, 80, false, false);
        agregar("NOMBRE", TipoDato.TEXTO, 10, false, false);
        agregar("Otra", TipoDato.ENTERO, 0, true, true);
        agregar("Ciudad", TipoDato.TEXTO, 0, false, false);
        agregar("Edad", TipoDato.ENTERO, 5, false, false);
        agregar("mi campo", TipoDato.FECHA, 0, false, false);

        System.out.println("\nCampos aceptados: " + m.campos().size());
        System.out.println("Indices: " + m.indices());
        try {
            m.borrarCampo(1);
        } catch (ErrorBD e) {
            System.out.println("Rechazado borrar Codigo: " + e.getMessage());
        }
        Validador.validarTodo(m);
        System.out.println("La metadata completa es correcta");
    }

    private static void agregar(String nombre, TipoDato tipo, int longMax,
            boolean obligatorio, boolean primaria) {
        try {
            m.agregarCampo(new Campo(0, nombre, tipo, longMax,
                    obligatorio, primaria, null));
            System.out.println("OK        " + nombre);
        } catch (ErrorBD e) {
            System.out.println("Rechazado " + nombre + ": " + e.getMessage());
        }
    }
}