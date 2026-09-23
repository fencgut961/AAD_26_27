package com.ia.geminis.sql_agent;

import jakarta.persistence.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseTools {

    @PersistenceContext
    private final EntityManager entityManager;

    public DatabaseTools(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Tool(description = "Ejecuta consultas SQL de lectura (SELECT) en la base de datos PostgreSQL local mediante JPA EntityManager para obtener información de usuarios y ventas.")
    public String runSqlQuery(String query) {
        String cleanQuery = query.trim();

        // Seguridad: Solo permitir consultas de lectura (SELECT)
        if (!cleanQuery.toUpperCase().startsWith("SELECT")) {
            return "Error de seguridad: Solo se permiten consultas de lectura (SELECT).";
        }

        try {
            // Ejecutamos la consulta nativa en JPA usando Tuple para capturar los nombres de columnas dinámicamente
            Query nativeQuery = entityManager.createNativeQuery(cleanQuery, Tuple.class);

            @SuppressWarnings("unchecked")
            List<Tuple> tuples = nativeQuery.getResultList();

            if (tuples.isEmpty()) {
                return "La consulta no devolvió ningún registro.";
            }

            // Convertimos la lista de Tuplas de JPA a una lista de Mapas fácil de interpretar para la IA
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Tuple tuple : tuples) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (TupleElement<?> element : tuple.getElements()) {
                    row.put(element.getAlias(), tuple.get(element));
                }
                resultList.add(row);
            }

            return resultList.toString();

        } catch (Exception e) {
            return "Error al ejecutar la consulta SQL con JPA: " + e.getMessage();
        }
    }
}