package com.los_maestros.usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.los_maestros.usuarios.model.Modelo;

@Repository 
public interface ModeloRepository extends JpaRepository<Modelo, Long> {

}
