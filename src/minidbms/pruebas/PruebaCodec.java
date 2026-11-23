/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.pruebas;

import minidbms.almacenamiento.Encabezado;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;
import minidbms.metadata.Campo;
import minidbms.metadata.Metadata;
import minidbms.metadata.MetadataCodec;
import minidbms.metadata.TipoDato;

public class PruebaCodec {

    public static void main(String[] args) throws Exception {
        Metadata m = new Metadata("Prueba");
        m.agregarCampo(new Campo(0, "Codigo", TipoDato.TEXTO, 3, true, true, null));
        m.agregarCampo(new Campo(0, "Cantidad", TipoDato.ENTERO, 0, false, false, 7L));
        m.definirIndice(2, 8, false);
        m.definirForanea(2, "Otro");

        byte[] bytes = MetadataCodec.aBytes(m);
        for (byte b : bytes) {
            System.out.printf("%02X ", b);
        }
        System.out.println("\nMetadata: " + bytes.length + " bytes");

        Metadata leida = MetadataCodec.desdeBytes(bytes, m.siguienteId());
        System.out.println("Campos iguales:   " + leida.campos().equals(m.campos()));
        System.out.println("Indices iguales:  " + leida.indices().equals(m.indices()));
        System.out.println("Foraneas iguales: " + leida.foraneas().equals(m.foraneas()));

        Encabezado e = new Encabezado();
        e.politica = Politica.BEST_FIT;
        byte[] enc = e.aBytes();
        System.out.println("Encabezado: " + enc.length + " bytes, politica "
                + Encabezado.desdeBytes(enc).politica);

        enc[50] = 99;
        rechazar("byte alterado", enc);
        enc[5] = 99;
        rechazar("version 99", enc);
        enc[0] = 'X';
        rechazar("otra magia", enc);
    }

    private static void rechazar(String caso, byte[] enc) {
        try {
            Encabezado.desdeBytes(enc);
            System.out.println("ERROR: se acepto " + caso);
        } catch (ErrorBD e) {
            System.out.println("Rechazado (" + caso + "): " + e.getMessage());
        }
    }
}