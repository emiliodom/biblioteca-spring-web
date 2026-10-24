package gt.edu.url.tallerformularios.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import gt.edu.url.tallerformularios.model.Usuario;
import gt.edu.url.tallerformularios.repository.UsuarioRepository;

/**
 * Se ejecuta una vez al arrancar la aplicación. Crea los dos usuarios iniciales
 * si todavía no existen. Las contraseñas llegan desde variables de entorno:
 * no están escritas en ningún archivo del proyecto.
 */
@Component
public class CargadorUsuarios implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String passwordAdmin;
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

    private void crearSiNoExiste(String username, String passwordPlano, String rol) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            String hash = passwordEncoder.encode(passwordPlano);
            usuarioRepository.save(new Usuario(username, hash, rol));
        }
    }
}
