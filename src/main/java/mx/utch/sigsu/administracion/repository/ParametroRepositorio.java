package mx.utch.sigsu.administracion.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.administracion.model.*;
public interface ParametroRepositorio extends JpaRepository<Parametro,String> {  }