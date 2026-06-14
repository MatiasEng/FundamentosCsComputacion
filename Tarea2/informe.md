# Informe Tarea 2 — LRC Player

**Asignatura:** Fundamentos de Ciencias de la Computación
**Tema:** Reproductor de audio con letra sincronizada usando SableCC

---

## 1. Introducción

El objetivo de esta tarea es construir un reproductor de audio en Java que lea archivos de letra en formato LRC (LyRiCs) y muestre la letra sincronizada con la reproducción de la canción. Para el parseo del formato LRC se utiliza **SableCC**, un generador de compiladores que produce un lexer, parser y AST (Abstract Syntax Tree) a partir de una gramática formal.

El proyecto integra:
- SableCC para el análisis sintáctico del archivo `.lrc`.
- BasicPlayer (biblioteca Java) para la reproducción de audio MP3.
- Una interfaz gráfica Swing que muestra la letra en tiempo real.
- Un sistema de temporización con `java.util.Timer` para disparar las líneas de letra en sus marcas de tiempo correspondientes.

---

## 2. Gramática de SableCC (`lrc.sablecc`)

### 2.1 Estructura general

La gramática define el formato de un archivo `.lrc`. Está compuesta por tres secciones: **Helpers**, **Tokens**, **Ignored Tokens** y **Productions**.

### 2.2 Helpers

```sablecc
Helpers
    digit = ['0' .. '9'];
    letter = ['a' .. 'z'] | ['A' .. 'Z'];
    cr = 13;
    lf = 10;
    newline = cr lf | lf | cr;
    apostrophe = 39;
```

Los helpers son macros que definen conjuntos de caracteres reutilizables en los tokens:
- `digit`: cualquier dígito del 0 al 9.
- `letter`: cualquier letra mayúscula o minúscula.
- `cr` y `lf`: caracteres de retorno de carro y salto de línea (códigos ASCII 13 y 10).
- `newline`: secuencias que representan un salto de línea (Windows `CR+LF`, Unix `LF`, o Mac clásico `CR`).
- `apostrophe`: el carácter comilla simple (código ASCII 39), necesario porque SableCC no lo permite directamente en ciertos contextos.

### 2.3 Tokens

```sablecc
Tokens
    l_bracket = '[';
    r_bracket = ']';
    bcontent = (letter | digit | ' ' | '.' | ',' | '!' | '?' | '-' | '_'
              | ';' | '/' | '\\' | '(' | ')' | '#' | '$' | '%' | '&'
              | '*' | '+' | '=' | '<' | '>' | '@' | '^' | '`' | '{'
              | '|' | '}' | '~' | apostrophe | ':')+;
    blank = ' ' | 9 | newline;
```

Se definen tres tokens:
- **`l_bracket`**: el carácter `[` de apertura.
- **`r_bracket`**: el carácter `]` de cierre.
- **`bcontent`**: el contenido entre corchetes. Es una secuencia de uno o más caracteres que incluye letras, dígitos, espacios, signos de puntuación y símbolos comunes. Este token captura tanto timestamps (`mm:ss.xx`) como metadatos (`ar:Artista`).
- **`blank`**: espacios en blanco, tabulaciones (`\t`, ASCII 9) y saltos de línea, que serán ignorados.

### 2.4 Tokens ignorados

```sablecc
Ignored Tokens
    blank;
```

Los espacios en blanco y saltos de línea se ignoran durante el parseo, lo que permite flexibilidad en el formato del archivo `.lrc`.

### 2.5 Productions

```sablecc
Productions
    lrc_file = element*;
    element =
        {tag_line} l_bracket bcontent r_bracket |
        {tagged_line} l_bracket [tag]:bcontent r_bracket [lyric]:bcontent;
```

La gramática define dos producciones:

#### `lrc_file`
Es la raíz del AST. Representa un archivo LRC completo como una secuencia de cero o más `element`.

#### `element`
Tiene dos alternativas:

1. **`tag_line`**: Una línea con solo un tag entre corchetes, sin texto de letra. Ejemplos:
   - `[ar:Artista]` — metadato
   - `[ti:Título]` — metadato
   - `[mm:ss.xx]` — timestamp sin letra (pausa instrumental)

   Su estructura es: `'[' bcontent ']'`

2. **`tagged_line`**: Una línea con un tag entre corchetes seguido de texto de letra. Ejemplos:
   - `[mm:ss.xx]Letra de la canción`

   Su estructura es: `'[' tag:bcontent ']' lyric:bcontent`
   - `tag`: el contenido del corchete (timestamp).
   - `lyric`: el texto de la letra después del corchete de cierre.

### 2.6 Árbol de sintaxis abstracta (AST)

SableCC genera automáticamente las siguientes clases en Java a partir de esta gramática:

| Clase | Propósito |
|---|---|
| `ALrcFile` | Nodo raíz que contiene la lista de `element` |
| `ATagLineElement` | Nodo para un tag sin letra (`[contenido]`) |
| `ATaggedLineElement` | Nodo para un tag con letra (`[tag]lyric`) |
| `TLBracket` | Token `[` |
| `TRBracket` | Token `]` |
| `TBcontent` | Token con el contenido textual entre corchetes o la letra |
| `Start` | Nodo inicial del AST que envuelve a `ALrcFile` |

---

## 3. El Visitador: `LyricVisitor`

### 3.1 Propósito

`LyricVisitor` extiende `DepthFirstAdapter` (generado por SableCC) y recorre el AST para extraer la información relevante: metadatos y líneas de letra con sus timestamps.

### 3.2 Funcionamiento

#### Clase interna `LyricEntry`

```java
public static class LyricEntry {
    private int minutes, seconds, centiseconds;
    private String text;

    public long getMilliseconds() {
        return (minutes * 60 * 1000L) + (seconds * 1000L) + (centiseconds * 10L);
    }
}
```

Almacena un timestamp desglosado en minutos, segundos y centésimas, más el texto de la línea. El método `getMilliseconds()` convierte el timestamp a milisegundos para usarlo con `java.util.Timer`.

#### Método `caseATagLineElement`

Se invoca al visitar un nodo `ATagLineElement` (tag sin letra). El contenido puede ser:
- **Metadato** (contiene `:`): extrae `ar:`, `ti:` o `al:` y guarda el valor en los campos `artista`, `titulo` o `album`.
- **Timestamp sin letra** (coincide con `\d+:\d+\.\d+`): lo agrega a la lista de letras con texto vacío (útil para pausas instrumentales donde no hay letra pero se marca el tiempo).

#### Método `caseATaggedLineElement`

Se invoca al visitar un nodo `ATaggedLineElement` (tag con letra). Toma el `tag` (timestamp) y el `lyric` (texto). Si el tag es un timestamp válido, crea un `LyricEntry` con ambos valores.

#### Detección de timestamps

```java
private boolean isTimestamp(String s) {
    return s.matches("\\d+:\\d+\\.\\d+");
}
```

Usa una expresión regular para verificar si el contenido de un tag tiene el formato `mm:ss.xx` (minutos, segundos, centésimas).

### 3.3 Flujo del visitador

```
Archivo LRC
    ↓ (SableCC Parser)
AST (Start → ALrcFile → lista de elementos)
    ↓ (LyricVisitor aplicado al AST)
Lista de LyricEntry con timestamps en ms + texto
    ↓ (Reproductor)
Timer sincroniza cada línea con la reproducción
```

---

## 4. La clase `Reproductor`

### 4.1 Ubicación

La clase `Reproductor` pertenece al paquete `lrc` (archivo `lrc/Reproductor.java`).

### 4.2 Componentes principales

| Componente | Propósito |
|---|---|
| `BasicPlayer` | Biblioteca para reproducir MP3 (pausa, reanudar, detener) |
| `java.util.Timer` + `TimerTask` | Programar la aparición de cada línea de letra en su momento exacto |
| `JFrame` + `JLabel` | Interfaz gráfica que muestra la letra centrada |
| `LyricVisitor` | Visitador que analiza el AST generado por SableCC |

### 4.3 Métodos principales

| Método | Descripción |
|---|---|
| `AbrirFichero(String)` | Abre un archivo MP3 para reproducción |
| `Play()` | Inicia o reanuda la reproducción |
| `Pausa()` | Pausa la reproducción |
| `Continuar()` | Reanuda desde pausa |
| `Stop()` | Detiene la reproducción y cancela el timer |
| `parsearLRC(String)` | Parsea un archivo `.lrc` usando SableCC y el `LyricVisitor` |
| `iniciarLetra(List)` | Programa cada línea de letra en el Timer según su timestamp |

### 4.4 Flujo del programa

```
main()
  ↓
1. Parsear argumentos: mp3Path y lrcPath
2. Crear ventana Swing (JFrame con JLabel)
3. parsearLRC(lrcPath) → lista de LyricEntry
4. AbrirFichero(mp3Path)
5. iniciarLetra(lyrics) → programa TimerTask para cada línea
6. Play() → comienza reproducción
```

### 4.5 Manejo de argumentos

- **2 argumentos**: `java lrc.Reproductor cancion.mp3 cancion.lrc` — rutas exactas (pueden incluir directorios)
- **1 argumento**: `java lrc.Reproductor cancion.mp3` — busca el MP3 en `audio/` y deriva el LRC en `lyrics/`
- **0 argumentos**: usa valores por defecto `audio/cancion.mp3` y `lyrics/cancion.lrc`

### 4.6 Organización de archivos

Los archivos de audio y letra se organizan en directorios separados para mantener el proyecto ordenado:

```
Tarea2/
├── audio/       # Archivos .mp3
├── lyrics/      # Archivos .lrc
└── ...          # código fuente, librerías, etc.
```

Esta separación permite escalar el proyecto agregando más canciones sin desordenar la raíz del proyecto. La resolución de rutas en `main()` antepone automáticamente el directorio correspondiente según la extensión del archivo.

---

## 5. Pruebas realizadas

Se probó el reproductor con las siguientes canciones:

| Canción | MP3 | LRC | Funciona |
|---|---|---|---|
| Evanescence — Bring Me To Life | `audio/Evanescence - Bring Me To Life.mp3` | `lyrics/bring me to life.lrc` | ✓ |
| Pop Smoke — Dior | `audio/Pop Smoke - Dior (Lyrics).mp3` | `lyrics/Pop Smoke - Dior.lrc` | ✓ |
| Metallica — Nothing Else Matters | *(pendiente de agregar)* | `lyrics/nothing else matters.lrc` | — |

> **Nota:** Se requiere agregar el archivo MP3 correspondiente a "Nothing Else Matters" para completar las 3 canciones solicitadas. El archivo `.lrc` ya está presente en el proyecto.

### 5.1 Comando de prueba

```bash
# Pop Smoke - Dior (1 argumento, busca LRC automáticamente)
cd ~/FundamentosCsComputacion/Tarea2
java -cp "lib/*:." lrc.Reproductor "Pop Smoke - Dior (Lyrics).mp3"

# Evanescence - Bring Me To Life (2 argumentos, rutas explícitas)
java -cp "lib/*:." lrc.Reproductor "audio/Evanescence - Bring Me To Life.mp3" "lyrics/bring me to life.lrc"
```

---

## 6. Compilación y ejecución

### 6.1 Compilación

```bash
javac -cp "lib/*:." lrc/analysis/*.java lrc/lexer/*.java lrc/node/*.java lrc/parser/*.java lrc/Reproductor.java
```

### 6.2 Regenerar parser (solo si se modifica la gramática)

```bash
java -jar sablecc-3.7/lib/sablecc.jar lrc.sablecc
```

### 6.3 Ejecución

```bash
# Con 1 argumento (MP3 en audio/, LRC se deriva en lyrics/)
java -cp "lib/*:." lrc.Reproductor <cancion.mp3>

# Con 2 argumentos (rutas explícitas)
java -cp "lib/*:." lrc.Reproductor <ruta_mp3> <ruta_lrc>
```

---

## 7. Estructura del proyecto

```
Tarea2/
├── audio/                     # Archivos de audio (.mp3)
│   ├── Evanescence - Bring Me To Life.mp3
│   └── Pop Smoke - Dior (Lyrics).mp3
├── lyrics/                    # Archivos de letra (.lrc)
│   ├── bring me to life.lrc
│   ├── nothing else matters.lrc
│   └── Pop Smoke - Dior.lrc
├── informe.md                 # Este informe
├── README.md                  # Instrucciones de uso
├── lrc.sablecc                # Gramática SableCC
├── lrc/                       # Código fuente
│   ├── Reproductor.java       # Clase principal
│   ├── analysis/
│   │   ├── Analysis.java
│   │   ├── AnalysisAdapter.java
│   │   ├── DepthFirstAdapter.java
│   │   ├── LyricVisitor.java  # Visitador del AST
│   │   └── ReversedDepthFirstAdapter.java
│   ├── lexer/                 # Lexer generado
│   ├── node/                  # Nodos del AST generados
│   └── parser/                # Parser generado
└── lib/                       # Bibliotecas (BasicPlayer, etc.)
```

---

## 8. Conclusiones

- SableCC permite generar un parser completo para un lenguaje relativamente simple como LRC con solo 24 líneas de gramática.
- La separación entre la definición de la gramática (SableCC), el visitador (lógica de extracción) y la clase principal (integración con audio/UI) facilita el mantenimiento y la extensión del proyecto.
- El formato LRC, aunque simple, requiere manejar correctamente timestamps con minutos, segundos y centésimas para lograr una sincronización precisa.
- La biblioteca BasicPlayer junto con `java.util.Timer` proporciona una solución funcional para la reproducción sincronizada de audio y letra.

---

## 9. Referencias

- SableCC: https://sablecc.org/
- Formato LRC: https://en.wikipedia.org/wiki/LRC_(file_format)
- BasicPlayer: https://github.com/creepid/BasicPlayer
