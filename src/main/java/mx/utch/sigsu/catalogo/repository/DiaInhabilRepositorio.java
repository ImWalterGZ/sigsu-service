package mx.utch.sigsu.catalogo.repository;

import mx.utch.sigsu.catalogo.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface DiaInhabilRepositorio extends JpaRepository<DiaInhabil, java.time.LocalDate> {}
