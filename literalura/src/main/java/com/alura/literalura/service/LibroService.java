package com.alura.literalura.service;

import com.alura.literalura.model.Libro;
import com.alura.literalura.model.Autor;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.repository.AutorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibroService {
    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public Libro guardarLibro(String jsonRespuesta) throws Exception {
        JsonNode root = mapper.readTree(jsonRespuesta);

        // Verificar si hay resultados
        JsonNode resultados = root.get("results");
        if (resultados == null || resultados.isEmpty()) {
            throw new Exception("No se encontraron resultados para el libro");
        }

        JsonNode resultado = resultados.get(0);

        // Verificar si ya existe el libro
        Optional<Libro> libroExistente = libroRepository.findByTitulo(resultado.get("title").asText());
        if (libroExistente.isPresent()) {
            return libroExistente.get();
        }

        Libro libro = new Libro();
        libro.setTitulo(resultado.get("title").asText());

        // Usar download_count como año (ajustar si es necesario)
        libro.setAnio(resultado.has("download_count") ?
                resultado.get("download_count").asInt() : null);

        // Manejar idiomas
        JsonNode languagesNode = resultado.get("languages");
        libro.setIdioma(languagesNode != null && !languagesNode.isEmpty() ?
                languagesNode.get(0).asText() : "Desconocido");

        // Manejar autores
        JsonNode autoresNode = resultado.get("authors");
        if (autoresNode != null && !autoresNode.isEmpty()) {
            JsonNode autorNode = autoresNode.get(0);

            Autor autor = new Autor();
            autor.setNombre(autorNode.get("name").asText());

            // Manejar años de nacimiento y muerte con null safety
            autor.setFechaNacimiento(
                    autorNode.has("birth_year") && !autorNode.get("birth_year").isNull() ?
                            autorNode.get("birth_year").asInt() : null
            );
            autor.setFechaMuerte(
                    autorNode.has("death_year") && !autorNode.get("death_year").isNull() ?
                            autorNode.get("death_year").asInt() : null
            );

            libro.setAutor(autor);
        }

        return libroRepository.save(libro);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosRegistrados() {
        List<Libro> libros = libroRepository.findAll();

        // Inicializar los autores de cada libro
        for (Libro libro : libros) {
            Hibernate.initialize(libro.getAutor());
        }

        return libros;
    }

    @Transactional(readOnly = true)
    public List<Autor> obtenerAutoresVivosEnAnio(Integer anio) {
        // Opción de consulta personalizada
        return autorRepository.encontrarAutoresVivosEnAnio(anio);
    }

    @Transactional(readOnly = true)
    public List<Autor> obtenerAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();

        // Inicializar la colección de libros para cada autor
        for (Autor autor : autores) {
            Hibernate.initialize(autor.getLibros());
        }

        return autores;
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosPorIdioma(String idioma) {
        return libroRepository.findByIdioma(idioma);
    }

    @Transactional(readOnly = true)
    public Optional<Libro> buscarLibroPorTitulo(String titulo) {
        return libroRepository.findByTitulo(titulo);
    }

    // Método para manejar libros duplicados
    @Transactional
    public Libro guardarOActualizarLibro(Libro libro) {
        Optional<Libro> libroExistente = libroRepository.findByTitulo(libro.getTitulo());

        if (libroExistente.isPresent()) {
            Libro existente = libroExistente.get();
            // Actualizar campos si es necesario
            existente.setAnio(libro.getAnio());
            existente.setIdioma(libro.getIdioma());
            return libroRepository.save(existente);
        }

        return libroRepository.save(libro);
    }

    // Método para obtener estadísticas básicas
    @Transactional(readOnly = true)
    public LibroStats obtenerEstadisticas() {
        List<Libro> libros = libroRepository.findAll();
        List<Autor> autores = autorRepository.findAll();

        return new LibroStats(
                libros.size(),
                autores.size(),
                libros.stream()
                        .collect(Collectors.groupingBy(Libro::getIdioma, Collectors.counting()))
        );
    }

    // Clase interna para estadísticas
    public static class LibroStats {
        private final int totalLibros;
        private final int totalAutores;
        private final java.util.Map<String, Long> librosPorIdioma;

        public LibroStats(int totalLibros, int totalAutores, java.util.Map<String, Long> librosPorIdioma) {
            this.totalLibros = totalLibros;
            this.totalAutores = totalAutores;
            this.librosPorIdioma = librosPorIdioma;
        }

        // Getters
        public int getTotalLibros() { return totalLibros; }
        public int getTotalAutores() { return totalAutores; }
        public java.util.Map<String, Long> getLibrosPorIdioma() { return librosPorIdioma; }
    }
}