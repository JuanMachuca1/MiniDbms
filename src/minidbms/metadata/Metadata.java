/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minidbms.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import minidbms.comun.ErrorBD;

public class Metadata {

    public static final int ORDEN_POR_DEFECTO = 6;

    private final String nombre;
    private int siguienteId;
    private final List<Campo> campos = new ArrayList<>();
    private final List<DefIndice> indices = new ArrayList<>();
    private final List<LlaveForanea> foraneas = new ArrayList<>();

    public Metadata(String nombre) {
        this(nombre, 1, List.of(), List.of(), List.of());
    }

    public Metadata(String nombre, int siguienteId, List<Campo> campos,
            List<DefIndice> indices, List<LlaveForanea> foraneas) {
        this.nombre = nombre;
        this.siguienteId = siguienteId;
        this.campos.addAll(campos);
        this.indices.addAll(indices);
        this.foraneas.addAll(foraneas);
    }

    public String nombre() {
        return nombre;
    }

    public int siguienteId() {
        return siguienteId;
    }

    public List<Campo> campos() {
        return Collections.unmodifiableList(campos);
    }

    public List<DefIndice> indices() {
        return Collections.unmodifiableList(indices);
    }

    public List<LlaveForanea> foraneas() {
        return Collections.unmodifiableList(foraneas);
    }
  // ---------- CAMPOS ----------

    public void agregarCampo(Campo borrador) throws ErrorBD {
        if (campos.size() >= Validador.MAX_CAMPOS) {
            throw new ErrorBD("Un archivo admite hasta " + Validador.MAX_CAMPOS + " campos");
        }
        Campo nuevo = borrador.conId(siguienteId);
        Validador.validarCampo(nuevo, campos);
        campos.add(nuevo);
        siguienteId++;
        sincronizarPrimaria();
    }

    public void modificarCampo(Campo nuevo) throws ErrorBD {
        Campo viejo = campoPorId(nuevo.id());
        Validador.validarCampo(nuevo, campos);
        if (viejo.tipo() != nuevo.tipo() && foraneaDe(nuevo.id()) != null) {
            throw new ErrorBD("Quite primero la llave foránea de " + viejo.nombre());
        }
        campos.set(campos.indexOf(viejo), nuevo);
        sincronizarPrimaria();
    }

    public void borrarCampo(int id) throws ErrorBD {
        Campo c = campoPorId(id);
        if (c.llavePrimaria()) {
            throw new ErrorBD("No se puede borrar la llave primaria");
        }
        if (foraneaDe(id) != null) {
            throw new ErrorBD("No se puede borrar: " + c.nombre() + " es llave foránea");
        }
        campos.remove(c);
        indices.removeIf(i -> i.idCampo() == id);
    }

    public Campo campoPorId(int id) throws ErrorBD {
        for (Campo c : campos) {
            if (c.id() == id) {
                return c;
            }
        }
        throw new ErrorBD("No existe un campo con id " + id);
    }

    public Campo llavePrimaria() {
        for (Campo c : campos) {
            if (c.llavePrimaria()) {
                return c;
            }
        }
        return null;
    }
   // ---------- INDICES ----------

    public DefIndice indiceDe(int idCampo) {
        for (DefIndice i : indices) {
            if (i.idCampo() == idCampo) {
                return i;
            }
        }
        return null;
    }

    public void definirIndice(int idCampo, int orden, boolean unico) throws ErrorBD {
        Campo c = campoPorId(idCampo);
        Validador.validarOrden(orden);
        if (c.llavePrimaria() && !unico) {
            throw new ErrorBD("El índice de la llave primaria debe ser único");
        }
        indices.removeIf(i -> i.idCampo() == idCampo);
        indices.add(new DefIndice(idCampo, orden, unico, c.llavePrimaria(),
                false, nombre + "_c" + idCampo + ".midx"));
    }

    public void quitarIndice(int idCampo) throws ErrorBD {
        if (campoPorId(idCampo).llavePrimaria()) {
            throw new ErrorBD("El índice de la llave primaria es obligatorio");
        }
        indices.removeIf(i -> i.idCampo() == idCampo);
    }

    private void sincronizarPrimaria() throws ErrorBD {
        Campo pk = llavePrimaria();
        indices.removeIf(i -> i.dePrimaria() && (pk == null || i.idCampo() != pk.id()));
        if (pk == null) {
            return;
        }
        DefIndice actual = indiceDe(pk.id());
        if (actual == null) {
            definirIndice(pk.id(), ORDEN_POR_DEFECTO, true);
        } else if (!actual.dePrimaria()) {
            definirIndice(pk.id(), actual.orden(), true);
        }
    }
  // ---------- LLAVES FORANEAS ----------

    public LlaveForanea foraneaDe(int idCampo) {
        for (LlaveForanea f : foraneas) {
            if (f.idCampo() == idCampo) {
                return f;
            }
        }
        return null;
    }

    public void definirForanea(int idCampo, String archivo) throws ErrorBD {
        campoPorId(idCampo);
        Validador.validarNombre(archivo, "Archivo referido");
        foraneas.removeIf(f -> f.idCampo() == idCampo);
        foraneas.add(new LlaveForanea(idCampo, archivo));
    }

    public void quitarForanea(int idCampo) {
        foraneas.removeIf(f -> f.idCampo() == idCampo);
    }
}