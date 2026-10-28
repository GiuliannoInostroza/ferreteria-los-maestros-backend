package com.los_maestros.usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.exception.RecursoNoEncontradoException;
import com.los_maestros.usuarios.model.Modelo;
import com.los_maestros.usuarios.repository.ModeloRepository;

@Service
public class ModeloService {
private final ModeloRepository repository;
public ModeloService(ModeloRepository repository) {
this.repository = repository;
}
public List<Modelo> listar() { return repository.findAll(); }
public Modelo buscar(Long id) {
return repository.findById(id).orElseThrow(() ->
new RecursoNoEncontradoException("Modelo no encontrado: " + id));
}
public Modelo guardar(Modelo modelo) {
return repository.save(modelo);
}
public Modelo actualizar(Long id, Modelo datos) {
Modelo actual = buscar(id);
actual.setNombre(datos.getNombre());
actual.setDescripcion(datos.getDescripcion());
return repository.save(actual);
}
public void eliminar(Long id) {
repository.delete(buscar(id));
}
}
