# Biblioteca — Spring Boot (Thymeleaf + JPA + Spring Security)

Aplicación web para registrar los libros de una biblioteca comunitaria. Se
construyó **a partir del Kit de la Semana 14** del curso *Programación I —
Introducción al Desarrollo Web con Java* (URL), que registraba jugadores de la
Escuela de Fútbol Los Cerritos. La arquitectura, el login, los perfiles y el
despliegue (Render + Neon) son los mismos; lo que cambia es la **entidad**
(`Jugador` → `Libro`) y el **segundo rol** (`ENTRENADOR` → `VISTA`).

| Pieza | Valor |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.1.1 (Spring Security 7, Hibernate, Thymeleaf) |
| Base de datos | H2 en archivo (perfil `dev`) · PostgreSQL en Neon (perfil `prod`) |
| Paquete base | `gt.edu.url.tallerformularios` (se conserva el del Kit) |
| Rutas | `/libros`, `/libros/nuevo`, `/libros/{id}/editar`, `POST /libros`, `POST /libros/{id}/eliminar` |

---

## 1. Cómo ejecutarlo

Las contraseñas de los dos usuarios iniciales **no están en el código**: llegan
por variables de entorno. Sin ellas la aplicación no arranca.

**Git Bash / macOS / Linux**

```bash
export ADMIN_PASSWORD='una-clave-para-admin'
export VISTA_PASSWORD='una-clave-para-vista'
./mvnw spring-boot:run
```

**PowerShell**

```powershell
$env:ADMIN_PASSWORD = 'una-clave-para-admin'
$env:VISTA_PASSWORD = 'una-clave-para-vista'
.\mvnw spring-boot:run
```

Abre <http://localhost:8080> e inicia sesión:

| Usuario | Rol | Qué puede hacer |
| --- | --- | --- |
| `admin` | `ADMIN` | Ver la lista, agregar, editar y eliminar libros |
| `vista` | `VISTA` | Solo ver la lista (cualquier otra acción → página *Acceso denegado*, 403) |

> Arranca desde la **terminal** donde definiste las variables. El botón ▶ Run
> de VS Code no ve las variables de esa terminal.

Para correr las pruebas también hacen falta las dos variables, y la app **no
debe estar abierta** (H2 en archivo admite un solo proceso a la vez; si la app
sigue corriendo, la prueba `contextLoads` falla con un engañoso
`Unable to determine Dialect`):

```bash
ADMIN_PASSWORD=x VISTA_PASSWORD=y ./mvnw test
```

---

## 2. Qué cambió respecto al Kit de la Semana 14

| Archivo | Kit Semana 14 | Biblioteca |
| --- | --- | --- |
| `model/Jugador.java` | Entidad con `nombre`, `edad`, `posicion` | **Eliminado** |
| `model/Libro.java` | — | **Nuevo**: 13 campos con JPA + Bean Validation |
| `repository/JugadorRepository.java` | `JpaRepository<Jugador, Long>` | **Eliminado** |
| `repository/LibroRepository.java` | — | **Nuevo**: `JpaRepository<Libro, Long>` |
| `service/JugadorService.java` | Listar, buscar, guardar, eliminar, contar | **Eliminado** |
| `service/LibroService.java` | — | **Nuevo**: los mismos 5 métodos, para `Libro` |
| `controller/JugadorController.java` | Rutas `/jugadores/**` | **Eliminado** |
| `controller/LibroController.java` | — | **Nuevo**: rutas `/libros/**` |
| `security/SecurityConfig.java` | Protege `/jugadores/**`; entra a `/jugadores` | Protege `/libros/**`; entra a `/libros` |
| `security/CargadorUsuarios.java` | Crea `admin` y `entrenador` | Crea `admin` y `vista` |
| `application.properties` | `app.entrenador.password=${ENTRENADOR_PASSWORD}` | `app.vista.password=${VISTA_PASSWORD}` |
| `templates/lista.html` | Tabla de jugadores | Tabla de libros (Id, Título, Autor, Año, Género, Páginas, Idioma) |
| `templates/formulario.html` | 3 campos | 13 campos + resumen de errores (ver **sección 4**, quedan pendientes de CSS) |
| `templates/login.html`, `error.html`, `acceso-denegado.html` | "Escuela de Fútbol Los Cerritos" | "Biblioteca" y enlaces a `/libros` |
| `AccesoController`, `Usuario`, `UsuarioRepository`, `UsuarioDetailsService` | — | **Sin cambios** |
| `application-dev.properties`, `application-prod.properties`, `Dockerfile`, `.dockerignore`, `pom.xml` | — | **Sin cambios** |
| `static/estilos.css` | — | **Sin cambios** |

Verificado el 24/10/2026 con una instancia aparte y base en memoria: compila,
arranca, `admin` entra a `/libros`, el formulario vacío devuelve los 8 mensajes
de validación, un libro válido se guarda y aparece en la lista, y `vista` ve la
lista sin el enlace *+ Agregar libro* y recibe 403 en `/libros/nuevo`.

---

## 3. Guía completa: del Kit de la Semana 14 a la Biblioteca

Punto de partida: el proyecto `taller-formularios` de la Semana 14 funcionando
(login, CRUD de jugadores, perfiles `dev`/`prod`, `Dockerfile`). Todas las rutas
de abajo son relativas a la carpeta del proyecto.

La idea general: **la Semana 6 en acción**. Se cambian las cuatro capas de la
entidad (Entidad → Repositorio → Servicio → Controlador) y sus vistas; la
seguridad solo cambia las rutas que protege. Nada más se toca.

### Paso 0 — Apaga la aplicación y respalda

Detén la app (si la dejas corriendo, H2 mantiene bloqueado el archivo de
`datos/`). Si el proyecto está en Git, haz un commit del Kit tal como está antes
de empezar, para poder volver.

### Paso 1 — La entidad: `Libro.java` reemplaza a `Jugador.java`

1. Borra `src/main/java/gt/edu/url/tallerformularios/model/Jugador.java`.
2. Crea `src/main/java/gt/edu/url/tallerformularios/model/Libro.java`:

```java
package gt.edu.url.tallerformularios.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- TEXTO obligatorio ----
    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 120, message = "El título admite hasta 120 caracteres.")
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio.")
    @Size(max = 80, message = "El autor admite hasta 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String autor;

    // ---- CÓDIGO con formato: es String, no número ----
    @NotBlank(message = "El ISBN es obligatorio.")
    @Pattern(regexp = "[0-9-]{10,17}", message = "El ISBN lleva de 10 a 17 dígitos o guiones.")
    @Column(nullable = false, length = 17)
    private String isbn;

    // ---- TEXTO opcional: sin @NotBlank y sin nullable=false ----
    @Size(max = 60, message = "La editorial admite hasta 60 caracteres.")
    @Column(length = 60)
    private String editorial;

    // ---- ENTEROS ----
    @NotNull(message = "El año de publicación es obligatorio.")
    @Min(value = 1450, message = "El año mínimo es 1450.")
    @Max(value = 2100, message = "El año máximo es 2100.")
    @Column(nullable = false)
    private Integer anioPublicacion;

    @Min(value = 1, message = "El libro debe tener al menos 1 página.")
    private Integer numeroPaginas;            // opcional

    // ---- OPCIÓN de una lista fija (select): String ----
    @NotBlank(message = "Elige un género.")
    @Column(nullable = false)
    private String genero;

    @NotBlank(message = "Elige un idioma.")
    @Column(nullable = false)
    private String idioma;

    @NotNull(message = "Indica cuántos ejemplares hay.")
    @Min(value = 0, message = "Los ejemplares no pueden ser negativos.")
    @Column(nullable = false)
    private Integer ejemplares;

    // ---- DINERO: BigDecimal, nunca double ----
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;                // opcional

    // ---- FECHA ----
    @NotNull(message = "La fecha de ingreso es obligatoria.")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura.")
    @Column(nullable = false)
    private LocalDate fechaIngreso;

    // ---- SÍ / NO ----
    private boolean disponible = true;

    // ---- TEXTO LARGO ----
    @Size(max = 500, message = "El resumen admite hasta 500 caracteres.")
    @Column(length = 500)
    private String resumen;

    public Libro() {
    }

    // Getters y setters de los 14 campos (id incluido).
    // En VS Code: clic derecho → Source Action → Generate Getters and Setters.
    // El campo boolean usa isDisponible() / setDisponible(boolean).
}
```

Cuatro decisiones de tipo que conviene explicar, porque son las que más fallan:

| Campo | Tipo | Por qué |
| --- | --- | --- |
| `anioPublicacion`, `numeroPaginas`, `ejemplares` | `Integer` (no `int`) | Con `int`, la casilla vacía produce un `typeMismatch` en inglés antes de que corran tus mensajes (Semana 11) |
| `isbn` | `String` | Lleva guiones y puede empezar con 0: es un código, no una cantidad |
| `precio` | `BigDecimal` | `double` redondea mal el dinero (0.1 + 0.2 ≠ 0.3) |
| `fechaIngreso` | `LocalDate` | Thymeleaf lo enlaza solo con `<input type="date">` (formato `aaaa-mm-dd`) |

> Conviven dos familias de anotaciones, como en la Semana 12:
> `jakarta.persistence` (cómo se guarda) y `jakarta.validation.constraints`
> (qué se acepta). Un campo opcional no lleva `@NotBlank`/`@NotNull` **ni**
> `nullable = false`.

### Paso 2 — El repositorio

Borra `repository/JugadorRepository.java` y crea
`src/main/java/gt/edu/url/tallerformularios/repository/LibroRepository.java`:

```java
package gt.edu.url.tallerformularios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gt.edu.url.tallerformularios.model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}
```

### Paso 3 — El servicio

Borra `service/JugadorService.java` y crea
`src/main/java/gt/edu/url/tallerformularios/service/LibroService.java`:

```java
package gt.edu.url.tallerformularios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import gt.edu.url.tallerformularios.model.Libro;
import gt.edu.url.tallerformularios.repository.LibroRepository;

@Service
public class LibroService {

    private final LibroRepository repositorio;

    public LibroService(LibroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> listarTodos() {
        return repositorio.findAll();
    }

    public Libro buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un libro con id " + id));
    }

    public Libro guardar(Libro libro) {
        return repositorio.save(libro);
    }

    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    public long contar() {
        return repositorio.count();
    }
}
```

### Paso 4 — El controlador

Borra `controller/JugadorController.java` y crea
`src/main/java/gt/edu/url/tallerformularios/controller/LibroController.java`.
**No toques `AccesoController.java`** (login y acceso denegado).

```java
package gt.edu.url.tallerformularios.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import gt.edu.url.tallerformularios.model.Libro;
import gt.edu.url.tallerformularios.service.LibroService;

@Controller
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping({"/", "/libros"})
    public String listar(Model model) {
        model.addAttribute("libros", libroService.listarTodos());
        model.addAttribute("total", libroService.contar());
        return "lista";
    }

    @GetMapping("/libros/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("libro", new Libro());
        return "formulario";
    }

    @GetMapping("/libros/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("libro", libroService.buscarPorId(id));
        return "formulario";
    }

    @PostMapping("/libros")
    public String guardar(@Valid @ModelAttribute("libro") Libro libro,
                          BindingResult resultado) {
        if (resultado.hasErrors()) {
            return "formulario";          // con errores: plantilla, NO redirect
        }
        libroService.guardar(libro);
        return "redirect:/libros";        // sin errores: guardar → redirigir → mostrar
    }

    @PostMapping("/libros/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return "redirect:/libros";
    }
}
```

El nombre `"libro"` de `@ModelAttribute` debe coincidir con el
`th:object="${libro}"` del formulario, y `BindingResult` va **inmediatamente
después** del objeto validado.

### Paso 5 — Seguridad: proteger `/libros` en vez de `/jugadores`

En `src/main/java/gt/edu/url/tallerformularios/security/SecurityConfig.java`
cambia solo estas tres líneas (el resto queda igual):

```java
.requestMatchers("/libros/nuevo", "/libros/*/editar").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/libros/**").hasRole("ADMIN")
```

```java
.defaultSuccessUrl("/libros", true))
```

Si olvidas este paso, la app compila y arranca, pero **cualquier usuario
autenticado puede crear y borrar libros**: las reglas siguen apuntando a
`/jugadores`, que ya no existe, y `/libros/**` cae en `anyRequest().authenticated()`.

### Paso 6 — El segundo rol: `entrenador` → `vista`

En `src/main/java/gt/edu/url/tallerformularios/security/CargadorUsuarios.java`
renombra el campo, el `@Value` y el usuario creado:

```java
    private final String passwordVista;

    public CargadorUsuarios(UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.password}") String passwordAdmin,
                            @Value("${app.vista.password}") String passwordVista) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordAdmin = passwordAdmin;
        this.passwordVista = passwordVista;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", passwordAdmin, "ADMIN");
        crearSiNoExiste("vista", passwordVista, "VISTA");
    }
```

Y en `src/main/resources/application.properties` cambia la última línea:

```properties
app.vista.password=${VISTA_PASSWORD}
```

Desde ahora la variable de entorno se llama **`VISTA_PASSWORD`** (ya no
`ENTRENADOR_PASSWORD`), en tu terminal **y en Render**.

### Paso 7 — Las plantillas

En `src/main/resources/templates/`:

| Plantilla | Qué cambiar |
| --- | --- |
| `login.html` | El `<h1>` a "Biblioteca" |
| `error.html` | `<h1>` "Biblioteca", el texto "vuelve a la lista de libros" y el enlace `@{/libros}` |
| `acceso-denegado.html` | `<h1>` "Biblioteca", el texto "…editar o eliminar libros" y el enlace `@{/libros}` |
| `lista.html` | `th:each="libro : ${libros}"`, columnas de `Libro`, enlaces `@{/libros/nuevo}`, `@{/libros/{id}/editar(id=${libro.id})}` y `@{/libros/{id}/eliminar(id=${libro.id})}` |
| `formulario.html` | `th:action="@{/libros}"`, `th:object="${libro}"`, un bloque por cada campo de `Libro` (los 13 `th:field` deben llamarse **exactamente** como los atributos de la clase) |

La versión actual de cada plantilla está en este repositorio. El formulario
funciona, pero **todavía no usa las clases de error de `estilos.css`**: antes
de copiarlo, lee la sección 4.

Tipos de `<input>` que corresponden a cada tipo Java:

| Tipo Java | Control HTML |
| --- | --- |
| `String` corto | `<input type="text">` |
| `String` largo (`resumen`) | `<textarea>` |
| `Integer` | `<input type="number">` |
| `BigDecimal` | `<input type="number" step="0.01">` |
| `LocalDate` | `<input type="date">` |
| `boolean` | `<input type="checkbox">` (Thymeleaf agrega solo el `_disponible` oculto) |

### Paso 8 — Arrancar y comprobar

1. Define `ADMIN_PASSWORD` y `VISTA_PASSWORD` y arranca desde la terminal.
2. Entra como `admin` → debes caer en `/libros` con "Todavía no hay libros registrados."
3. *+ Agregar libro* → **Guardar** con todo vacío → deben aparecer 8 mensajes
   (título, autor, ISBN, año, género, idioma, ejemplares, fecha).
4. Llena los obligatorios y guarda → el libro aparece en la lista. Apaga y
   enciende la app: sigue ahí (H2 en archivo).
5. Cierra sesión, entra como `vista` → ves la lista sin *+ Agregar libro* ni
   la columna *Acciones*; escribir a mano `/libros/nuevo` lleva a *Acceso denegado*.

### Paso 9 — Base de datos local que ya existía

Si reutilizas la carpeta `datos/` del Kit, contiene la tabla `jugador` y el
usuario `entrenador`. Hibernate (`ddl-auto=update`) **agrega** la tabla
`libro` pero no borra nada, y `CargadorUsuarios` solo crea usuarios que no
existen. Consecuencia: el usuario **`entrenador` sigue pudiendo iniciar
sesión** y ver la lista (su rol `ENTRENADOR` no tiene permisos de escritura).
Para empezar limpio en desarrollo, con la app apagada, borra la carpeta
`datos/`; se vuelve a crear al arrancar.

### Paso 10 — Producción (Render + Neon)

Ningún archivo de despliegue cambia (`Dockerfile`, `.dockerignore`,
`application-prod.properties`). En Render → *Environment*:

| Variable | Acción |
| --- | --- |
| `ENTRENADOR_PASSWORD` | Eliminarla |
| `VISTA_PASSWORD` | **Agregarla** (sin ella la app no arranca y el deploy falla) |
| `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_PASSWORD` | Sin cambios |

Si la base de Neon es la misma del Kit, pasa lo del paso 9: la tabla
`jugador` y el usuario `entrenador` siguen ahí. Se pueden borrar desde el
*SQL Editor* de Neon (`DROP TABLE jugador;` y
`DELETE FROM usuario WHERE username = 'entrenador';`) o usar una rama/proyecto
nuevo de Neon.

---

## 4. Pendiente: asociar los errores del formulario con el CSS

`static/estilos.css` ya trae las clases para mostrar errores —son las mismas
del Kit—, pero el `formulario.html` nuevo casi no las usa. Hoy los mensajes
salen como texto plano y la casilla con error no se marca. **No se ha
corregido todavía**; esta sección documenta qué hay y qué falta.

### 4.1 Clases que ya existen en `estilos.css`

| Clase / selector | Qué hace | Dónde se debe usar |
| --- | --- | --- |
| `.campo-error` | Borde rojo de 2 px y fondo rosado | **En el `<input>`/`<select>`/`<textarea>`**, puesta por Thymeleaf con `th:errorclass="campo-error"` (solo aparece si ese campo tiene error) |
| `.mensaje-error` | Texto rojo en negrita con un "▲" delante | En el `<p>`/`<div>` que lleva `th:errors="*{campo}"`, debajo del control |
| `.aviso` | Recuadro con borde izquierdo azul | Mensajes informativos |
| `.aviso.error` | El mismo recuadro, en rojo | Resumen de errores arriba del formulario (con `role="alert"`), login fallido, acceso denegado |
| `input, select` | Ancho completo (máx. 480 px), padding, borde gris | Todos los controles de texto/número/fecha |
| `input:focus, select:focus, button:focus, a:focus` | Contorno azul de 3 px (foco visible, Semana 11) | Automático |
| `button`, `button.peligro`, `button.secundario` | Botón azul / rojo (eliminar) / gris (cerrar sesión) | Ya usados en `lista.html` |
| `form.en-linea`, `.barra`, `table`, `th`, `td` | Formularios en línea, barra de sesión, tabla | Ya usados en `lista.html` |

### 4.2 Qué falta en `formulario.html`

| # | Dónde | Situación actual | Lo que corresponde |
| --- | --- | --- | --- |
| 1 | Resumen de errores (`<!--RESUMEN ERRORES-->`) | `<div>` sin clase: sale como texto sin formato | `class="aviso error"` y `role="alert"` en ese `<div>` (sigue siendo lo primero dentro del `<form>`, como quedó corregido en S11/S12) |
| 2 | Campo `titulo` | `class="campo-error"` está puesta en un `<div>` de **mensaje** (arriba de la etiqueta), así que pinta un recuadro rojo con el texto | `.campo-error` va en el `<input>` vía `th:errorclass`; ese `<div>` sobra |
| 3 | Campo `titulo` | El mensaje de error aparece **dos veces** (el `<div>` de arriba y el `.mensaje-error` de abajo) | Dejar solo el `.mensaje-error` de abajo |
| 4 | Campo `titulo` | Falta el `<div>` de apertura del bloque y sobra un `</div>` de cierre | Un `<div>` que envuelva etiqueta + input + mensaje, como los demás campos |
| 5 | Los otros 11 campos con validación (`autor` … `resumen`) | El `<input>` no tiene `th:errorclass="campo-error"` | Agregarlo a cada control para que la casilla se marque en rojo |
| 6 | Los mismos 11 campos | El `<div th:errors>` no tiene clase | `class="mensaje-error"` |
| 7 | Campo `disponible` | Envuelto en `<data class="disponible">`: `<data>` no es un contenedor, y la clase `.disponible` no existe en el CSS | Un `<div>` normal |
| 8 | `<textarea>` de `resumen` | El CSS solo da estilo a `input, select`: el textarea queda con el aspecto del navegador y sin el contorno de foco azul | Incluir `textarea` en esos dos selectores de `estilos.css` |
| 9 | Checkbox `disponible` | La regla `input { display:block; width:100% }` también le aplica: la casilla se estira o queda desalineada con su etiqueta | Una regla aparte para `input[type="checkbox"]` (ancho automático) |
| 10 | Accesibilidad (Semana 11) | Ningún control tiene `aria-describedby` hacia su mensaje de error | `id` en cada mensaje y `aria-describedby` en su control |

Así se ve el patrón de un campo ya resuelto en el Kit original (referencia, no
cambio aplicado):

```html
<label for="nombre">Nombre completo</label>
<input id="nombre" type="text" th:field="*{nombre}" th:errorclass="campo-error">
<p class="mensaje-error" th:if="${#fields.hasErrors('nombre')}" th:errors="*{nombre}">Error</p>
```

### 4.3 Otros detalles del formulario (no son CSS)

- **`required` en campos opcionales.** `editorial`, `numeroPaginas`, `precio` y
  `resumen` son opcionales en `Libro`, pero sus controles llevan `required`.
  Hoy no estorba porque el `<form>` tiene `novalidate`; si se quita, el
  navegador exigirá campos que el servidor acepta vacíos.
- **`genero` e `idioma`.** La entidad los comenta como "opción de una lista
  fija (select)" y sus mensajes dicen "Elige…", pero el formulario usa
  `<input type="text">`. Un `<select>` con opciones fijas (como `posicion` en
  el Kit) evitaría valores como "novela", "Novela" y "NOVELA" mezclados.
- **`disponible`** no aparece en la tabla de `lista.html`.

### 4.4 Restos del Kit que conviene limpiar

- `application-dev.properties` sigue apuntando a `jdbc:h2:file:./datos/jugadores`.
  Funciona, pero el nombre engaña; si se cambia a `./datos/biblioteca`, la app
  arranca con una base nueva y vacía.
- El comentario de `model/Usuario.java` todavía dice `"ADMIN" o "ENTRENADOR"`.
- El `artifactId`, el paquete `gt.edu.url.tallerformularios` y la clase
  `TallerFormulariosApplication` conservan el nombre del Kit. Es válido; solo
  se renombraría si el proyecto deja de ser el taller del curso.
