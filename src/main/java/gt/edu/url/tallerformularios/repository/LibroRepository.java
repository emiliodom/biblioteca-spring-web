package gt.edu.url.tallerformularios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gt.edu.url.tallerformularios.model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}
