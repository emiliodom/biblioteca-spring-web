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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Integer getEjemplares() {
        return ejemplares;
    }

    public void setEjemplares(Integer ejemplares) {
        this.ejemplares = ejemplares;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }
 

    
}
