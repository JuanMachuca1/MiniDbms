/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import java.util.List;
import minidbms.comun.ErrorBD;

public final class Validador {

    public static final int MAX_CAMPOS = 200;
    public static final int MAX_LONGITUD = 10000;

    private Validador() {
    }

    public static void validarNombre(String nombre, String que) throws ErrorBD {
        if (nombre == null || !nombre.matches("\\p{L}[\\p{L}\\p{N}_]{0,63}")) {
            throw new ErrorBD(que + ": use de 1 a 64 letras, números o guion bajo,"
                    + " sin espacios, y empiece con una letra");
        }
    }

    public static void validarCampo(Campo c, List<Campo> existentes) throws ErrorBD {
        validarNombre(c.nombre(), "Nombre de campo");
        if (c.tipo() == null) {
            throw new ErrorBD("Elija el tipo del campo");
        }
        validarLongitud(c);
        if (c.llavePrimaria() && !c.obligatorio()) {
            throw new ErrorBD("La llave primaria debe ser obligatoria");
        }
        if (c.llavePrimaria() && c.valorPorDefecto() != null) {
            throw new ErrorBD("La llave primaria no puede tener valor por defecto");
        }
        for (Campo otro : existentes) {
            if (otro.id() == c.id()) {
                continue;
            }
            if (otro.nombre().equalsIgnoreCase(c.nombre())) {
                throw new ErrorBD("Ya existe un campo llamado " + otro.nombre());
            }
            if (otro.llavePrimaria() && c.llavePrimaria()) {
                throw new ErrorBD("Ya hay una llave primaria: " + otro.nombre());
            }
        }
    }
    private static void validarLongitud(Campo c) throws ErrorBD {
        if (c.tipo() != TipoDato.TEXTO) {
            if (c.longMax() != 0) {
                throw new ErrorBD("Solo los campos de texto llevan longitud");
            }
            return;
        }
        if (c.longMax() < 1 || c.longMax() > MAX_LONGITUD) {
            throw new ErrorBD("La longitud debe estar entre 1 y " + MAX_LONGITUD);
        }
        if (c.valorPorDefecto() instanceof String s
                && s.codePointCount(0, s.length()) > c.longMax()) {
            throw new ErrorBD("El valor por defecto pasa de " + c.longMax() + " caracteres");
        }
    }

    public static void validarOrden(int orden) throws ErrorBD {
        if (orden < 3 || orden > 1000) {
            throw new ErrorBD("El orden del árbol debe estar entre 3 y 1000");
        }
    }

    public static void validarTodo(Metadata m) throws ErrorBD {
        validarNombre(m.nombre(), "Nombre de archivo");
        int primarias = 0;
        for (Campo c : m.campos()) {
            validarCampo(c, m.campos());
            if (c.llavePrimaria()) {
                primarias++;
            }
        }
        if (!m.campos().isEmpty() && primarias != 1) {
            throw new ErrorBD("Debe haber exactamente una llave primaria (hay "
                    + primarias + ")");
        }
        for (DefIndice i : m.indices()) {
            m.campoPorId(i.idCampo());
            validarOrden(i.orden());
        }
        for (LlaveForanea f : m.foraneas()) {
            m.campoPorId(f.idCampo());
            validarNombre(f.archivoReferido(), "Archivo referido");
        }
    }
}