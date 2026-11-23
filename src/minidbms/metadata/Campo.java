/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

public record Campo(
        int id,
        String nombre,
        TipoDato tipo,
        int longMax,
        boolean obligatorio,
        boolean llavePrimaria,
        Object valorPorDefecto) {

    public Campo conId(int nuevoId) {
        return new Campo(nuevoId, nombre, tipo, longMax,
                obligatorio, llavePrimaria, valorPorDefecto);
    }
}