/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.pruebas;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import minidbms.almacenamiento.ArchivoBinario;

public class PruebaBinario {

    public static void main(String[] args) throws Exception {
        File ruta = new File("prueba.tmp");
        ruta.delete();

        // 1. EMPACAR: de variables a bytes
        byte[] texto = "Peña".getBytes(StandardCharsets.UTF_8);
        ByteBuffer salida = ByteBuffer.allocate(8 + 8 + 4 + 1 + 2 + texto.length);
        salida.putLong(1000);
        salida.putDouble(2500.5);
        salida.putInt((int) LocalDate.of(2026, 11, 22).toEpochDay());
        salida.put((byte) 1);
        salida.putShort((short) texto.length);
        salida.put(texto);

        try (ArchivoBinario a = ArchivoBinario.crear(ruta)) {
            a.escribir(0, salida.array());
        }

        // 2. DESEMPACAR: el archivo se cerró y se abre de nuevo
        try (ArchivoBinario a = ArchivoBinario.abrir(ruta)) {
            byte[] bytes = a.leer(0, (int) a.longitud());
            for (byte b : bytes) {
                System.out.printf("%02X ", b);
            }
            System.out.println("\nTotal: " + bytes.length + " bytes");

            ByteBuffer entrada = ByteBuffer.wrap(bytes);
            System.out.println("Entero:   " + entrada.getLong());
            System.out.println("Decimal:  " + entrada.getDouble());
            System.out.println("Fecha:    " + LocalDate.ofEpochDay(entrada.getInt()));
            System.out.println("Booleano: " + (entrada.get() == 1));
            byte[] t = new byte[Short.toUnsignedInt(entrada.getShort())];
            entrada.get(t);
            System.out.println("Texto:    " + new String(t, StandardCharsets.UTF_8));

            a.leer(1000, 4);   // a propósito: no existe ese offset
        } catch (java.io.IOException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
    }
}