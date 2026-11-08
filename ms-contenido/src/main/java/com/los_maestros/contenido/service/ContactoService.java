
package com.los_maestros.contenido.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.contenido.exception.RecursoNoEncontradoException;
import com.los_maestros.contenido.model.Contacto;
import com.los_maestros.contenido.repository.ContactoRepository;

@Service
public class ContactoService {

    private final ContactoRepository repository;

    public ContactoService(ContactoRepository repository) {
        this.repository = repository;
    }

    public List<Contacto> listar() {
        return repository.findAll();
    }

    public Contacto buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new RecursoNoEncontradoException(
                    "Mensaje no encontrado: " + id
                )
            );
    }

    public Contacto crear(Contacto contacto) {
        contacto.setId(null);
        return repository.save(contacto);
    }

    public void eliminar(Long id) {
        Contacto actual = buscar(id);
        repository.delete(actual);
    }
}
