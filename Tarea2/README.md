# Tarea 2 - LRC Player

Reproductor de audio con letra sincronizada en formato LRC.  
Usa SableCC para generar un parser de archivos `.lrc`.

## Estructura del proyecto

```
Tarea2/
├── lrc.sablecc              # Gramática SableCC
├── lrc/                     # Código generado por SableCC
│   ├── analysis/
│   │   └── LyricVisitor.java   # Visitador del AST
│   ├── lexer/
│   ├── node/
│   └── parser/
├── Reproductor.java         # Clase principal con interfaz gráfica
├── lib/                     # Librerías (BasicPlayer, etc.)
├── *.lrc                    # Archivos de letra
└── *.mp3                    # Archivos de audio
```

## Requisitos

- Java 8+
- SableCC 3.7 (solo para regenerar el parser)

## Compilación

```bash
# 1. Regenerar parser (solo si modificas la gramática)
java -jar sablecc-3.7/lib/sablecc.jar lrc.sablecc

# 2. Compilar todo
javac -cp "lib/*:." lrc/analysis/*.java lrc/lexer/*.java lrc/node/*.java lrc/parser/*.java Reproductor.java
```

## Uso

```bash
# Con ambos archivos
java -cp "lib/*:." Reproductor cancion.mp3 cancion.lrc

# Si el .lrc tiene el mismo nombre que el .mp3
java -cp "lib/*:." Reproductor cancion.mp3

# Ejemplo real
java -cp "lib/*:." Reproductor "Evanescence - Bring Me To Life (Official HD Music Video).mp3" "bring me to life.lrc"
```

## Gramática (lrc.sablecc)

```
lrc_file = element*
element = {tag_line} '[' bcontent ']'
        | {tagged_line} '[' tag:bcontent ']' lyric:bcontent
```

- `bcontent` captura todo entre `[` y `]` (timestamps o metadata)
- `tag_line` es un tag sin letra (timestamp vacío o metadata)
- `tagged_line` es un tag seguido de texto (timestamp + letra)

El visitador `LyricVisitor` distingue entre metadata (`ar:`, `ti:`, `al:`) y timestamps (`mm:ss.xx`) analizando el contenido del tag como string.

## Formato LRC soportado

```
[ar: Artista]
[ti: Título]
[al: Álbum]

[mm:ss.xx]Letra sincronizada
[mm:ss.xx]Siguiente línea
```

## Funcionalidades

- Parseo de archivos `.lrc` con SableCC
- Extracción de metadatos (artista, título, álbum)
- Sincronización letra-audio con `Timer`
- Interfaz gráfica con letra en tiempo real
- Salida en consola simultánea
