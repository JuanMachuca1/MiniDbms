/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import minidbms.comun.ErrorBD;

public final class MetadataCodec {

    private MetadataCodec() {
    }

    // ---------- EMPACAR ----------

    public static byte[] aBytes(Metadata m) throws IOException {
        ByteArrayOutputStream memoria = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(memoria);
        escribirTexto(s, m.nombre());
        s.writeShort(m.campos().size());
        for (Campo c : m.campos()) {
            escribirCampo(s, c);
        }
        s.writeShort(m.indices().size());
        for (DefIndice i : m.indices()) {
            s.writeShort(i.idCampo());
            s.writeShort(i.orden());
            s.writeByte((i.unico() ? 1 : 0) | (i.dePrimaria() ? 2 : 0)
                    | (i.construido() ? 4 : 0));
            escribirTexto(s, i.archivo());
        }
        s.writeShort(m.foraneas().size());
        for (LlaveForanea f : m.foraneas()) {
            s.writeShort(f.idCampo());
            escribirTexto(s, f.archivoReferido());
        }
        return memoria.toByteArray();
    }
    private static void escribirCampo(DataOutputStream s, Campo c) throws IOException {
        s.writeShort(c.id());
        escribirTexto(s, c.nombre());
        s.writeByte(c.tipo().codigo());
        s.writeShort(c.longMax());
        s.writeByte((c.obligatorio() ? 1 : 0) | (c.llavePrimaria() ? 2 : 0)
                | (c.valorPorDefecto() != null ? 4 : 0));
        if (c.valorPorDefecto() != null) {
            escribirBloque(s, CodecValor.aBytes(c.tipo(), c.valorPorDefecto()));
        }
    }

    private static void escribirTexto(DataOutputStream s, String t) throws IOException {
        escribirBloque(s, t.getBytes(StandardCharsets.UTF_8));
    }

    private static void escribirBloque(DataOutputStream s, byte[] b) throws IOException {
        s.writeShort(b.length);
        s.write(b);
    }
     // ---------- DESEMPACAR ----------

    public static Metadata desdeBytes(byte[] datos, int siguienteId) throws ErrorBD {
        try {
            ByteBuffer b = ByteBuffer.wrap(datos);
            String nombre = leerTexto(b);
            List<Campo> campos = new ArrayList<>();
            int n = leerCorto(b);
            for (int k = 0; k < n; k++) {
                campos.add(leerCampo(b));
            }
            List<DefIndice> indices = new ArrayList<>();
            n = leerCorto(b);
            for (int k = 0; k < n; k++) {
                int id = leerCorto(b);
                int orden = leerCorto(b);
                int banderas = b.get();
                indices.add(new DefIndice(id, orden, (banderas & 1) != 0,
                        (banderas & 2) != 0, (banderas & 4) != 0, leerTexto(b)));
            }
            List<LlaveForanea> foraneas = new ArrayList<>();
            n = leerCorto(b);
            for (int k = 0; k < n; k++) {
                foraneas.add(new LlaveForanea(leerCorto(b), leerTexto(b)));
            }
            Metadata m = new Metadata(nombre, siguienteId, campos, indices, foraneas);
            Validador.validarTodo(m);
            return m;
        } catch (BufferUnderflowException e) {
            throw new ErrorBD("Archivo dañado: la metadata está incompleta");
        }
    }

    private static Campo leerCampo(ByteBuffer b) throws ErrorBD {
        int id = leerCorto(b);
        String nombre = leerTexto(b);
        TipoDato tipo = TipoDato.desdeCodigo(b.get());
        int longMax = leerCorto(b);
        int banderas = b.get();
        Object defecto = null;
        if ((banderas & 4) != 0) {
            defecto = CodecValor.desdeBytes(tipo, leerBloque(b));
        }
        return new Campo(id, nombre, tipo, longMax,
                (banderas & 1) != 0, (banderas & 2) != 0, defecto);
    }

    private static int leerCorto(ByteBuffer b) {
        return Short.toUnsignedInt(b.getShort());
    }

    private static byte[] leerBloque(ByteBuffer b) {
        byte[] bloque = new byte[leerCorto(b)];
        b.get(bloque);
        return bloque;
    }

    private static String leerTexto(ByteBuffer b) {
        return new String(leerBloque(b), StandardCharsets.UTF_8);
    }
}
