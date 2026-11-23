/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.comun;

public enum Politica {

    FIRST_FIT(1, "First-fit"),
    BEST_FIT(2, "Best-fit"),
    WORST_FIT(3, "Worst-fit");

    private final int codigo;
    private final String etiqueta;

    Politica(int codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public int codigo() {
        return codigo;
    }

    public static Politica desdeCodigo(int codigo) throws ErrorBD {
        for (Politica p : values()) {
            if (p.codigo == codigo) {
                return p;
            }
        }
        throw new ErrorBD("Política de reutilización desconocida: código " + codigo);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}