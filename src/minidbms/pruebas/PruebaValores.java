/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.pruebas;

import minidbms.comun.ErrorBD;
import minidbms.metadata.TipoDato;
import minidbms.metadata.Valores;

public class PruebaValores {

    public static void main(String[] args) {
        probar(TipoDato.ENTERO, "1000");
        probar(TipoDato.ENTERO, "abc");
        probar(TipoDato.DECIMAL, "2500.5");
        probar(TipoDato.DECIMAL, "NaN");
        probar(TipoDato.FECHA, "2026-11-22");
        probar(TipoDato.FECHA, "2026-02-30");
        probar(TipoDato.BOOLEANO, "Sí");
        probar(TipoDato.BOOLEANO, "tal vez");
        probar(TipoDato.TEXTO, "  Ana | Mejia  ");
        probar(TipoDato.ENTERO, "   ");
    }

    private static void probar(TipoDato tipo, String texto) {
        try {
            Object valor = Valores.convertir(tipo, texto);
            System.out.println("OK        " + tipo + ": " + valor);
        } catch (ErrorBD e) {
            System.out.println("Rechazado " + tipo + ": " + e.getMessage());
        }
    }
}
