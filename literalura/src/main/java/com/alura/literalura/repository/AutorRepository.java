package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT DISTINCT a FROM Autor a LEFT JOIN FETCH a.libros WHERE :anio BETWEEN a.fechaNacimiento AND a.fechaMuerte")
    List<Autor> encontrarAutoresVivosEnAnio(@Param("anio") Integer anio);

    @Query("SELECT DISTINCT a FROM Autor a LEFT JOIN FETCH a.libros")
    List<Autor> findAllWithLibros();
}
