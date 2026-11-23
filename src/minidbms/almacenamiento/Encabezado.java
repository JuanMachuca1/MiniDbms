/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.almacenamiento;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import minidbms.comun.ErrorBD;
import minidbms.comun.Politica;

public class Encabezado {

    public static final int TAMANO = 128;
    public static final int MAGIA = 0x4D444255;      // "MDBU"
    public static final int VERSION = 1;
    public static final int CAPACIDAD_META = 16384;
    public static final int HUECO_MINIMO_FISICO = 25;
    public static final int HUECO_MAXIMO = 100000;

    public int banderas = 0;
    public int tamMeta = 0;
    public int crcMeta = 0;
    public long inicioDatos = TAMANO + CAPACIDAD_META;
    public long cabezaAvail = -1;
    public Politica politica = Politica.FIRST_FIT;
    public int tamMinHueco = 32;
    public long registrosActivos = 0;
    public long registrosEliminados = 0;
    public long bytesLibres = 0;
    public int cantidadHuecos = 0;
    public int siguienteIdCampo = 1;
    public long finDatos = TAMANO + CAPACIDAD_META;
    public long fechaCreacion = System.currentTimeMillis();
    public long sello = 0;
    public long bytesRelleno = 0;
    
public byte[] aBytes() {
        ByteBuffer b = ByteBuffer.allocate(TAMANO);
        b.putInt(MAGIA);                        // 0
        b.putShort((short) VERSION);            // 4
        b.putShort((short) banderas);           // 6
        b.putInt(TAMANO);                       // 8
        b.putInt(CAPACIDAD_META);               // 12
        b.putInt(tamMeta);                      // 16
        b.putInt(crcMeta);                      // 20
        b.putLong(inicioDatos);                 // 24
        b.putLong(cabezaAvail);                 // 32
        b.put((byte) politica.codigo());        // 40
        b.put(new byte[3]);                     // 41 reservado
        b.putInt(tamMinHueco);                  // 44
        b.putLong(registrosActivos);            // 48
        b.putLong(registrosEliminados);         // 56
        b.putLong(bytesLibres);                 // 64
        b.putInt(cantidadHuecos);               // 72
        b.putInt(siguienteIdCampo);             // 76
        b.putLong(finDatos);                    // 80
        b.putLong(fechaCreacion);               // 88
        b.putLong(sello);                       // 96
        b.putLong(bytesRelleno);                // 104
        b.put(new byte[12]);                    // 112 reservado
        b.putInt(crc(b.array(), 124));          // 124
        return b.array();
    }

 public static Encabezado desdeBytes(byte[] datos) throws ErrorBD {
        ByteBuffer b = ByteBuffer.wrap(datos);
        if (b.getInt() != MAGIA) {
            throw new ErrorBD("No es un archivo de Mini DBMS");
        }
        int version = Short.toUnsignedInt(b.getShort());
        if (version != VERSION) {
            throw new ErrorBD("Versión de formato no soportada: " + version);
        }
        if (b.getInt(124) != crc(datos, 124)) {
            throw new ErrorBD("Archivo dañado: el encabezado fue alterado");
        }
        Encabezado e = new Encabezado();
        e.banderas = Short.toUnsignedInt(b.getShort());
        int tamEncabezado = b.getInt();
        int capacidad = b.getInt();
        e.tamMeta = b.getInt();
        e.crcMeta = b.getInt();
        e.inicioDatos = b.getLong();
        e.cabezaAvail = b.getLong();
        e.politica = Politica.desdeCodigo(b.get());
        b.position(44);
        e.tamMinHueco = b.getInt();
        e.registrosActivos = b.getLong();
        e.registrosEliminados = b.getLong();
        e.bytesLibres = b.getLong();
        e.cantidadHuecos = b.getInt();
        e.siguienteIdCampo = b.getInt();
        e.finDatos = b.getLong();
        e.fechaCreacion = b.getLong();
        e.sello = b.getLong();
        e.bytesRelleno = b.getLong();
        if (tamEncabezado != TAMANO || capacidad != CAPACIDAD_META
                || e.tamMeta < 0 || e.tamMeta > CAPACIDAD_META
                || e.inicioDatos != TAMANO + CAPACIDAD_META
                || e.finDatos < e.inicioDatos) {
            throw new ErrorBD("Archivo dañado: el encabezado tiene valores imposibles");
        }
        validarTamMinHueco(e.tamMinHueco);
        return e;
    }
 
    public static void validarTamMinHueco(int tam) throws ErrorBD {
        if (tam < HUECO_MINIMO_FISICO || tam > HUECO_MAXIMO) {
            throw new ErrorBD("El tamaño mínimo de hueco debe estar entre "
                    + HUECO_MINIMO_FISICO + " y " + HUECO_MAXIMO);
        }
    }

    public static int crc(byte[] datos, int cantidad) {
        CRC32 crc = new CRC32();
        crc.update(datos, 0, cantidad);
        return (int) crc.getValue();
    }
}