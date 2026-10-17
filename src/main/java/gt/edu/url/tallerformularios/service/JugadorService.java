package gt.edu.url.tallerformularios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import gt.edu.url.tallerformularios.model.Jugador;
import gt.edu.url.tallerformularios.repository.JugadorRepository;

@Service
public class JugadorService {

    private final JugadorRepository repositorio;

    public JugadorService(JugadorRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Jugador> listarTodos() {
        return repositorio.findAll();
    }

    public Jugador buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un jugador con id " + id));
    }

    public Jugador guardar(Jugador jugador) {
        return repositorio.save(jugador);
    }

    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    public long contar() {
        return repositorio.count();
    }
}
