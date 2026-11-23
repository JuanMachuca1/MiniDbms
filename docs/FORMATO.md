# Formato del archivo de datos (.mdat) — versión 1

Todos los números se guardan en **big-endian** (el byte más significativo primero).
Un `texto` se guarda como 2 bytes con su longitud en bytes, seguidos del texto en UTF-8.

## Organización general

```
0            128                       16512
├────────────┼─────────────────────────┼───────────────────────────
│ Encabezado │ Metadata │  reservado   │ registros y huecos (Parte 2)
│ 128 bytes  │ tamMeta  │ hasta 16 KB  │
```

La metadata vive dentro del archivo de datos, en una zona reservada de 16 KB.
Así el archivo nunca se separa de su descripción y la metadata puede crecer sin mover los registros.

## Encabezado (128 bytes)

| Offset | Bytes | Campo | Valor al crear |
|---|---|---|---|
| 0 | 4 | Número mágico `MDBU` (`4D 44 42 55`) | — |
| 4 | 2 | Versión del formato | 1 |
| 6 | 2 | Banderas | 0 |
| 8 | 4 | Tamaño del encabezado | 128 |
| 12 | 4 | Capacidad de la zona de metadata | 16384 |
| 16 | 4 | Bytes usados por la metadata | — |
| 20 | 4 | CRC32 de la metadata | — |
| 24 | 8 | Inicio de los datos | 16512 |
| 32 | 8 | Cabeza de la avail list | −1 (sin huecos) |
| 40 | 1 | Política: 1 first-fit, 2 best-fit, 3 worst-fit | la elegida |
| 41 | 3 | Reservado | 0 |
| 44 | 4 | Tamaño mínimo de hueco (25 a 100000) | 32 |
| 48 | 8 | Registros activos | 0 |
| 56 | 8 | Registros eliminados | 0 |
| 64 | 8 | Bytes libres | 0 |
| 72 | 4 | Cantidad de huecos | 0 |
| 76 | 4 | Siguiente id de campo | 1 |
| 80 | 8 | Fin de los datos | 16512 |
| 88 | 8 | Fecha de creación (milisegundos desde 1970) | — |
| 96 | 8 | Sello de cambios (para validar índices) | 0 |
| 104 | 8 | Bytes de relleno interno | 0 |
| 112 | 12 | Reservado | 0 |
| 124 | 4 | CRC32 de los bytes 0 a 123 | — |

## Metadata (desde el byte 128)

```
texto  nombre del archivo
2      cantidad de campos
  por campo:
    2      id (nunca cambia; índices y llaves foráneas usan el id)
    texto  nombre
    1      tipo: 1 entero, 2 decimal, 3 texto, 4 fecha, 5 booleano
    2      longitud máxima en caracteres (0 si no es texto)
    1      banderas: bit 0 obligatorio, bit 1 llave primaria, bit 2 tiene valor por defecto
    2 + n  si bit 2: longitud y bytes del valor por defecto
2      cantidad de índices
  por índice:
    2      id del campo
    2      orden del árbol B (3 a 1000; 6 por defecto)
    1      banderas: bit 0 único, bit 1 índice de la llave primaria, bit 2 construido
    texto  archivo del índice: <nombre>_c<id>.midx
2      cantidad de llaves foráneas
  por llave foránea:
    2      id del campo
    texto  archivo referido (misma carpeta; apunta a su llave primaria)
```

## Valores

| Tipo | Bytes | Representación |
|---|---|---|
| Entero | 8 | `long` con signo |
| Decimal | 8 | IEEE 754 doble precisión (`double`) |
| Texto | n | UTF-8 (la longitud va aparte) |
| Fecha | 4 | Días desde el 1970-01-01 |
| Booleano | 1 | 1 verdadero, 0 falso |

## Validación al abrir

1. El archivo mide al menos 128 bytes.
2. Número mágico → "No es un archivo de Mini DBMS".
3. Versión → "Versión de formato no soportada".
4. CRC del encabezado → "el encabezado fue alterado".
5. Valores del encabezado posibles → "valores imposibles".
6. Longitud del archivo ≥ fin de los datos → "truncado".
7. CRC de la metadata → "la metadata fue alterada".
8. Reglas de la metadata (nombres únicos, una sola llave primaria, longitudes válidas).

## Reglas de los campos

- Nombre: 1 a 64 letras, números o guion bajo; empieza con letra; no se repite (sin distinguir mayúsculas).
- Texto: longitud de 1 a 10000 caracteres. Los demás tipos no llevan longitud.
- Exactamente una llave primaria al guardar (si hay campos). Es obligatoria, sin valor por defecto y con índice único automático.
- No se puede borrar la llave primaria ni un campo con llave foránea. Borrar un campo indexado borra su índice.
- Una llave foránea exige el mismo tipo que la llave primaria del archivo referido.
