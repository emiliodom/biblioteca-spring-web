package gt.edu.url.tallerformularios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gt.edu.url.tallerformularios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
