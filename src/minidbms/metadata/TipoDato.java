/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import minidbms.comun.ErrorBD;

public enum TipoDato {

    ENTERO(1, "Entero"),
    DECIMAL(2, "Decimal"),
    TEXTO(3, "Texto"),
    FECHA(4, "Fecha"),
    BOOLEANO(5, "Booleano");

    private final int codigo;
    private final String etiqueta;

    TipoDato(int codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public int codigo() {
        return codigo;
    }

    public static TipoDato desdeCodigo(int codigo) throws ErrorBD {
        for (TipoDato tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new ErrorBD("Tipo de dato desconocido: código " + codigo);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}