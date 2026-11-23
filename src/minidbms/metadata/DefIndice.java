/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

public record DefIndice(
        int idCampo,
        int orden,
        boolean unico,
        boolean dePrimaria,
        boolean construido,
        String archivo) {
}
