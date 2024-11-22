package com.alura.literalura.principal;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Libro;
import com.alura.literalura.service.ApiConsumidorService;
import com.alura.literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class Principal implements CommandLineRunner {
    @Autowired
    private ApiConsumidorService apiService;

    @Autowired
    private LibroService libroService;

    @Autowired
    private ApplicationContext applicationContext;

    private Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1: buscarLibroPorTitulo(); break;
                    case 2: listarLibrosRegistrados(); break;
                    case 3: listarAutoresVivosEnAnio(); break;
                    case 4: listarAutoresRegistrados(); break;
                    case 5: listarLibrosPorIdioma(); break;
                    case 0:
                        System.out.println("Cerrando el programa...");
                        // Cerrar el contexto de Spring y terminar el programa
                        System.exit(SpringApplication.exit(applicationContext, () -> 0));
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, intente nuevamente.");
                }
            } catch (Exception e) {
                System.out.println("Ocurrió un error: " + e.getMessage());
                scanner.nextLine(); // Limpiar buffer del scanner
            }
        }
    }

    private void buscarLibroPorTitulo() {
        try {
            System.out.print("Ingrese el título del libro a buscar: ");
            String titulo = scanner.nextLine();

            // Consumir la API
            String jsonRespuesta = apiService.buscarLibroPorTitulo(titulo);

            // Guardar el libro
            Libro libroGuardado = libroService.guardarLibro(jsonRespuesta);

            System.out.println("Libro encontrado y guardado:");
            System.out.println("Título: " + libroGuardado.getTitulo());
            System.out.println("Autor: " + libroGuardado.getAutor().getNombre());
            System.out.println("Año: " + libroGuardado.getAnio());
            System.out.println("Idioma: " + libroGuardado.getIdioma());
        } catch (Exception e) {
            System.out.println("Error al buscar o guardar el libro: " + e.getMessage());
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroService.obtenerLibrosRegistrados();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }

        System.out.println("\n--- Libros Registrados ---");
        libros.forEach(libro -> {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor().getNombre());
            System.out.println("Año: " + libro.getAnio());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("------------------------");
        });
    }

    private void listarAutoresVivosEnAnio() {
        System.out.print("Ingrese el año para buscar autores vivos: ");
        int anio = scanner.nextInt();
        scanner.nextLine(); // Consumir salto de línea

        List<Autor> autores = libroService.obtenerAutoresVivosEnAnio(anio);

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anio);
            return;
        }

        System.out.println("\n--- Autores Vivos en " + anio + " ---");
        autores.forEach(autor -> {
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Año de Nacimiento: " + autor.getFechaNacimiento());
            System.out.println("Año de Muerte: " + autor.getFechaMuerte());
            System.out.println("------------------------");
        });
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = libroService.obtenerAutoresRegistrados();

        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
            return;
        }

        System.out.println("\n--- Autores Registrados ---");
        autores.forEach(autor -> {
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Año de Nacimiento: " + autor.getFechaNacimiento());
            System.out.println("Año de Muerte: " + autor.getFechaMuerte());
            System.out.println("Libros: " +
                    (autor.getLibros() != null ? autor.getLibros().size() : "0"));
            System.out.println("------------------------");
        });
    }

    private void listarLibrosPorIdioma() {
        System.out.println("\nSeleccione el idioma:");
        System.out.println("1. es (Español)");
        System.out.println("2. en (Inglés)");
        System.out.println("3. fr (Francés)");
        System.out.println("4. de (Alemán)");
        System.out.print("Elija una opción: ");

        int opcionIdioma = scanner.nextInt();
        scanner.nextLine(); // Consumir salto de línea

        String idioma;
        switch (opcionIdioma) {
            case 1: idioma = "es"; break;
            case 2: idioma = "en"; break;
            case 3: idioma = "fr"; break;
            case 4: idioma = "de"; break;
            default:
                System.out.println("Opción inválida");
                return;
        }

        List<Libro> libros = libroService.obtenerLibrosPorIdioma(idioma);

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma seleccionado.");
            return;
        }

        System.out.println("\n--- Libros en " + idioma + " ---");
        libros.forEach(libro -> {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor().getNombre());
            System.out.println("Año: " + libro.getAnio());
            System.out.println("------------------------");
        });
    }

    private void mostrarMenu() {
        System.out.println("\n--- LiteraLura ---");
        System.out.println("1. Buscar libro por título");
        System.out.println("2. Listar libros registrados");
        System.out.println("3. Listar autores vivos en un determinado año");
        System.out.println("4. Listar autores registrados");
        System.out.println("5. Listar libros por idioma");
        System.out.println("0. Salir");
        System.out.print("Elija una opción: ");
    }
}