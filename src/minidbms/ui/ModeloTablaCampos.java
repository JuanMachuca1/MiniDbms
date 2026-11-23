/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.ui;

import javax.swing.table.AbstractTableModel;
import minidbms.metadata.Campo;
import minidbms.metadata.DefIndice;
import minidbms.metadata.LlaveForanea;
import minidbms.metadata.Metadata;
import minidbms.metadata.Valores;

public class ModeloTablaCampos extends AbstractTableModel {

    private static final String[] COLUMNAS = {"Id", "Nombre", "Tipo", "Longitud",
        "Obligatorio", "Llave primaria", "Por defecto", "Índice", "Llave foránea"};

    private Metadata metadata;

    public void mostrar(Metadata metadata) {
        this.metadata = metadata;
        fireTableDataChanged();
    }

    public Campo campoEn(int fila) {
        return metadata.campos().get(fila);
    }

    @Override
    public int getRowCount() {
        return metadata == null ? 0 : metadata.campos().size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return columna == 4 || columna == 5 ? Boolean.class : Object.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Campo c = campoEn(fila);
        DefIndice indice = metadata.indiceDe(c.id());
        LlaveForanea foranea = metadata.foraneaDe(c.id());
        return switch (columna) {
            case 0 -> c.id();
            case 1 -> c.nombre();
            case 2 -> c.tipo();
            case 3 -> c.longMax() > 0 ? c.longMax() : "";
            case 4 -> c.obligatorio();
            case 5 -> c.llavePrimaria();
            case 6 -> Valores.aTexto(c.valorPorDefecto());
            case 7 -> indice == null ? "" : (indice.unico() ? "Único" : "No único")
                    + ", orden " + indice.orden();
            default -> foranea == null ? "" : "→ " + foranea.archivoReferido();
        };
    }
}
