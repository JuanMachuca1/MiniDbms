/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.pruebas;

import java.io.File;
import java.io.IOException;
import minidbms.almacenamiento.ArchivoBinario;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;
import minidbms.metadata.Campo;
import minidbms.metadata.TipoDato;
import minidbms.nucleo.ArchivoBD;

public class PruebaArchivo {

    private static final File CARPETA = new File("datos");
    private static final File RUTA = new File(CARPETA, "Prueba.mdat");

    public static void main(String[] args) throws Exception {
        CARPETA.mkdirs();
        crearDeNuevo();

        try (ArchivoBD a = ArchivoBD.abrir(RUTA)) {
            System.out.println("Reabierto: " + a.metadata().campos().size()
                    + " campos, politica " + a.encabezado().politica);
            System.out.println("Cambios pendientes: " + a.hayCambios());
            a.metadata().agregarCampo(new Campo(0, "Extra", TipoDato.FECHA,
                    0, false, false, null));
            System.out.println("Cambios pendientes: " + a.hayCambios());
        }

        try {
            ArchivoBD.crear(CARPETA, "Prueba", Politica.FIRST_FIT, 32);
        } catch (IOException e) {
            System.out.println("Rechazado (ya existe): " + e.getMessage());
        }

        danar("otra magia", 0, 'X');
        danar("version 99", 5, 99);
        danar("encabezado alterado", 50, 99);
        danar("metadata alterada", 140, 99);
        danar("truncado", -1, 0);
        crearDeNuevo();
    }

    private static void crearDeNuevo() throws IOException, ErrorBD {
        RUTA.delete();
        try (ArchivoBD a = ArchivoBD.crear(CARPETA, "Prueba", Politica.BEST_FIT, 40)) {
            a.metadata().agregarCampo(new Campo(0, "Codigo", TipoDato.TEXTO,
                    3, true, true, null));
            a.metadata().agregarCampo(new Campo(0, "Cantidad", TipoDato.ENTERO,
                    0, false, false, 7L));
            a.guardar();
        }
    }

    private static void danar(String caso, int offset, int valor) throws Exception {
        crearDeNuevo();
        if (offset >= 0) {
            try (ArchivoBinario b = ArchivoBinario.abrir(RUTA)) {
                b.escribir(offset, new byte[]{(byte) valor});
            }
        } else {
            byte[] mitad;
            try (ArchivoBinario b = ArchivoBinario.abrir(RUTA)) {
                mitad = b.leer(0, 1000);
            }
            RUTA.delete();
            try (ArchivoBinario b = ArchivoBinario.crear(RUTA)) {
                b.escribir(0, mitad);
            }
        }
        try (ArchivoBD a = ArchivoBD.abrir(RUTA)) {
            System.out.println("ERROR: se acepto " + caso);
        } catch (ErrorBD e) {
            System.out.println("Rechazado (" + caso + "): " + e.getMessage());
        }
    }
}
