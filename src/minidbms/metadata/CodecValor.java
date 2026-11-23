/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import minidbms.comun.ErrorBD;

public final class CodecValor {

    private CodecValor() {
    }

    public static byte[] aBytes(TipoDato tipo, Object valor) {
        return switch (tipo) {
            case ENTERO -> ByteBuffer.allocate(8).putLong((Long) valor).array();
            case DECIMAL -> ByteBuffer.allocate(8).putDouble((Double) valor).array();
            case TEXTO -> ((String) valor).getBytes(StandardCharsets.UTF_8);
            case FECHA -> ByteBuffer.allocate(4)
                    .putInt((int) ((LocalDate) valor).toEpochDay()).array();
            case BOOLEANO -> new byte[]{(byte) ((Boolean) valor ? 1 : 0)};
        };
    }

    public static Object desdeBytes(TipoDato tipo, byte[] datos) throws ErrorBD {
        int esperado = switch (tipo) {
            case ENTERO, DECIMAL -> 8;
            case FECHA -> 4;
            case BOOLEANO -> 1;
            case TEXTO -> datos.length;
        };
        if (datos.length != esperado) {
            throw new ErrorBD("Valor dañado de tipo " + tipo);
        }
        ByteBuffer b = ByteBuffer.wrap(datos);
        try {
            return switch (tipo) {
                case ENTERO -> b.getLong();
                case DECIMAL -> b.getDouble();
                case TEXTO -> new String(datos, StandardCharsets.UTF_8);
                case FECHA -> LocalDate.ofEpochDay(b.getInt());
                case BOOLEANO -> b.get() == 1;
            };
        } catch (DateTimeException e) {
            throw new ErrorBD("Fecha dañada");
        }
    }
}