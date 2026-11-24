package com.los_maestros.pedidos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CatalogoCliente {
    private final RestClient cliente;

    public CatalogoCliente(@Value("${app.catalogo.url}") String url) {
        this.cliente = RestClient.create(url);
    }

    public Map<String, ProductoVigente> productosVigentes(String authorization) {
        Map<String, Object> catalogo = cliente.get().uri("/catalogo")
            .header("Authorization", authorization)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<Long, Integer> descuentos = new HashMap<>();
        for (Map<String, Object> oferta : lista(catalogo, "ofertas")) {
            descuentos.put(numero(oferta.get("productoId")).longValue(),
                numero(oferta.get("porcentaje")).intValue());
        }
        Map<String, ProductoVigente> productos = new HashMap<>();
        for (Map<String, Object> producto : lista(catalogo, "productos")) {
            int precio = numero(producto.get("precio")).intValue();
            Integer porcentaje = descuentos.get(numero(producto.get("id")).longValue());
            if (porcentaje != null) {
                precio = precioConOferta(precio, porcentaje);
            }
            productos.put((String) producto.get("codigo"),
                new ProductoVigente((String) producto.get("nombre"), precio));
        }
        return productos;
    }

    public static int precioConOferta(int precio, int porcentaje) {
        return precio * (100 - porcentaje) / 100;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Map<String, Object> datos, String clave) {
        Object valor = datos == null ? null : datos.get(clave);
        return valor == null ? List.of() : (List<Map<String, Object>>) valor;
    }

    private static Number numero(Object valor) {
        return (Number) valor;
    }

    public record ProductoVigente(String nombre, int precio) {}
}
