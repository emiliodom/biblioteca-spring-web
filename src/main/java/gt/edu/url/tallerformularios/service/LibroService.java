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
