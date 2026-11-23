/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import minidbms.comun.ErrorBD;

public final class Valores {

    private Valores() {
    }

    public static Object convertir(TipoDato tipo, String texto) throws ErrorBD {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String t = texto.strip();
        try {
            return switch (tipo) {
                case ENTERO -> Long.valueOf(t);
                case DECIMAL -> convertirDecimal(t);
                case TEXTO -> t;
                case FECHA -> convertirFecha(t);
                case BOOLEANO -> convertirBooleano(t);
            };
        } catch (NumberFormatException | DateTimeException e) {
            throw new ErrorBD("\"" + t + "\" no es un valor válido de tipo " + tipo
                    + (tipo == TipoDato.FECHA ? " (use AAAA-MM-DD)" : ""));
        }
    }

    private static Double convertirDecimal(String t) throws ErrorBD {
        double d = new BigDecimal(t).doubleValue();
        if (!Double.isFinite(d)) {
            throw new ErrorBD("El número es demasiado grande: " + t);
        }
        return d;
    }

    private static LocalDate convertirFecha(String t) throws ErrorBD {
        LocalDate fecha = LocalDate.parse(t);
        if (fecha.getYear() < 1 || fecha.getYear() > 9999) {
            throw new ErrorBD("El año debe estar entre 0001 y 9999");
        }
        return fecha;
    }
    public static String aTexto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Boolean b) {
            return b ? "Sí" : "No";
        }
        if (valor instanceof Double d) {
            return BigDecimal.valueOf(d).toPlainString();
        }
        return valor.toString();
    }

    private static Boolean convertirBooleano(String t) throws ErrorBD {
        return switch (t.toLowerCase()) {
            case "true", "si", "sí", "verdadero", "1" -> true;
            case "false", "no", "falso", "0" -> false;
            default -> throw new ErrorBD("\"" + t + "\" no es Sí ni No");
        };
    }
    
}