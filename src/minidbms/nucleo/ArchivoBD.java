/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.nucleo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minidbms.almacenamiento.ArchivoBinario;
import minidbms.almacenamiento.Encabezado;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;
import minidbms.metadata.Campo;
import minidbms.metadata.Metadata;
import minidbms.metadata.MetadataCodec;
import minidbms.metadata.Validador;

public class ArchivoBD implements AutoCloseable {

    public static final String EXTENSION = ".mdat";

    private final File ruta;
    private final ArchivoBinario binario;
    private final Encabezado encabezado;
    private final Metadata metadata;
    private byte[] encabezadoGuardado = new byte[0];
    private byte[] metadataGuardada = new byte[0];

    private ArchivoBD(File ruta, ArchivoBinario binario,
            Encabezado encabezado, Metadata metadata) {
        this.ruta = ruta;
        this.binario = binario;
        this.encabezado = encabezado;
        this.metadata = metadata;
    }

    public File ruta() {
        return ruta;
    }

    public Metadata metadata() {
        return metadata;
    }

    public Encabezado encabezado() {
        return encabezado;
    }
     // ---------- CREAR ----------

    public static ArchivoBD crear(File carpeta, String nombre,
            Politica politica, int tamMinHueco) throws IOException, ErrorBD {
        Validador.validarNombre(nombre, "Nombre de archivo");
        Encabezado.validarTamMinHueco(tamMinHueco);
        File ruta = new File(carpeta, nombre + EXTENSION);
        ArchivoBinario binario = ArchivoBinario.crear(ruta);
        try {
            Encabezado e = new Encabezado();
            e.politica = politica;
            e.tamMinHueco = tamMinHueco;
            binario.escribir(0, new byte[(int) e.inicioDatos]);
            ArchivoBD nuevo = new ArchivoBD(ruta, binario, e, new Metadata(nombre));
            nuevo.guardar();
            return nuevo;
        } catch (IOException | ErrorBD error) {
            binario.close();
            ruta.delete();
            throw error;
        }
    }
      // ---------- ABRIR ----------

    public static ArchivoBD abrir(File ruta) throws IOException, ErrorBD {
        ArchivoBinario binario = ArchivoBinario.abrir(ruta);
        try {
            if (binario.longitud() < Encabezado.TAMANO) {
                throw new ErrorBD("No es un archivo de Mini DBMS: es demasiado pequeño");
            }
            byte[] enc = binario.leer(0, Encabezado.TAMANO);    
            Encabezado e = Encabezado.desdeBytes(enc);
            if (binario.longitud() < e.finDatos) {
                throw new ErrorBD("Archivo dañado: está truncado (faltan bytes al final)");
            }
            byte[] meta = binario.leer(Encabezado.TAMANO, e.tamMeta);
            if (Encabezado.crc(meta, meta.length) != e.crcMeta) {
                throw new ErrorBD("Archivo dañado: la metadata fue alterada");
            }
            Metadata m = MetadataCodec.desdeBytes(meta, e.siguienteIdCampo);
            ArchivoBD abierto = new ArchivoBD(ruta, binario, e, m);
            abierto.encabezadoGuardado = enc;
            abierto.metadataGuardada = meta;
            return abierto;
        } catch (IOException | ErrorBD error) {
            binario.close();
            throw error;
        }
    }
    
 // ---------- GUARDAR Y CERRAR ----------

    public void guardar() throws IOException, ErrorBD {
        Validador.validarTodo(metadata);
        byte[] meta = MetadataCodec.aBytes(metadata);
        if (meta.length > Encabezado.CAPACIDAD_META) {
            throw new ErrorBD("La metadata ocupa " + meta.length
                    + " bytes y el máximo es " + Encabezado.CAPACIDAD_META);
        }
        encabezado.tamMeta = meta.length;
        encabezado.crcMeta = Encabezado.crc(meta, meta.length);
        encabezado.siguienteIdCampo = metadata.siguienteId();
        byte[] enc = encabezado.aBytes();
        binario.escribir(Encabezado.TAMANO, meta);
        binario.escribir(0, enc);
        metadataGuardada = meta;
        encabezadoGuardado = enc;
    }

    public boolean hayCambios() throws IOException {
        encabezado.siguienteIdCampo = metadata.siguienteId();
        return !Arrays.equals(encabezado.aBytes(), encabezadoGuardado)
                || !Arrays.equals(MetadataCodec.aBytes(metadata), metadataGuardada);
    }

    @Override
    public void close() throws IOException {
        binario.close();
    }

    // ---------- LLAVES FORANEAS ----------

    public List<String> archivosDeLaCarpeta() {
        List<String> nombres = new ArrayList<>();
        File[] archivos = ruta.getAbsoluteFile().getParentFile().listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                if (f.isFile() && f.getName().endsWith(EXTENSION)) {
                    nombres.add(f.getName().substring(0,
                            f.getName().length() - EXTENSION.length()));
                }
            }
        }
        return nombres;
    }

    public void definirForanea(int idCampo, String archivo) throws IOException, ErrorBD {
        Campo local = metadata.campoPorId(idCampo);
        Campo destino = primariaDe(archivo);
        if (destino == null) {
            throw new ErrorBD(archivo + " todavía no tiene llave primaria guardada");
        }
        if (destino.tipo() != local.tipo()) {
            throw new ErrorBD("Tipos incompatibles: " + local.nombre() + " es "
                    + local.tipo() + " y " + archivo + "." + destino.nombre()
                    + " es " + destino.tipo());
        }
        metadata.definirForanea(idCampo, archivo);
    }

    private Campo primariaDe(String archivo) throws IOException, ErrorBD {
        Validador.validarNombre(archivo, "Archivo referido");
        File otra = new File(ruta.getAbsoluteFile().getParentFile(), archivo + EXTENSION);
        if (otra.equals(ruta.getAbsoluteFile())) {
            return metadata.llavePrimaria();
        }
        try (ArchivoBD otro = abrir(otra)) {
            return otro.metadata().llavePrimaria();
        }
    }
}