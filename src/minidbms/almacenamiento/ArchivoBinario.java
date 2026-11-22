/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.almacenamiento;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ArchivoBinario implements AutoCloseable {

    private final RandomAccessFile archivo;

    private ArchivoBinario(File ruta) throws IOException {
        archivo = new RandomAccessFile(ruta, "rw");
    }

    public static ArchivoBinario crear(File ruta) throws IOException {
        if (!ruta.createNewFile()) {
            throw new IOException("El archivo ya existe: " + ruta.getName());
        }
        return new ArchivoBinario(ruta);
    }

    public static ArchivoBinario abrir(File ruta) throws IOException {
        if (!ruta.isFile()) {
            throw new IOException("El archivo no existe: " + ruta.getName());
        }
        return new ArchivoBinario(ruta);
    }

    public byte[] leer(long offset, int cantidad) throws IOException {
        if (offset < 0 || cantidad < 0 || offset > archivo.length() - cantidad) {
            throw new EOFException("Lectura fuera del archivo: offset "
                    + offset + ", " + cantidad + " bytes");
        }
        byte[] datos = new byte[cantidad];
        archivo.seek(offset);
        archivo.readFully(datos);
        return datos;
    }

    public void escribir(long offset, byte[] datos) throws IOException {
        if (offset < 0) {
            throw new IOException("Offset negativo: " + offset);
        }
        archivo.seek(offset);
        archivo.write(datos);
    }

    public long longitud() throws IOException {
        return archivo.length();
    }

    @Override
    public void close() throws IOException {
        archivo.close();
    }
}