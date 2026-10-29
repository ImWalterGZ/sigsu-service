package mx.utch.sigsu.catalogo.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.catalogo.model.*;
public interface DiaInhabilRepositorio extends JpaRepository<DiaInhabil,java.time.LocalDate> {  }