package com.los_maestros.cuentas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.los_maestros.cuentas.model.CuentaCorriente;

@Repository
public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, Long> {
    List<CuentaCorriente> findByCorreoClienteIgnoreCase(String correoCliente);
}
