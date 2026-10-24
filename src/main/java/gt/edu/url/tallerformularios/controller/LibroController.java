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
            return "formulario";
        }
        libroService.guardar(libro);
        return "redirect:/libros";
    }

    @PostMapping("/libros/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return "redirect:/libros";
    }
}
