-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios
(
    id
    SERIAL
    PRIMARY
    KEY,
    nombre
    VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    100
) UNIQUE NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Tabla de ventas
CREATE TABLE IF NOT EXISTS ventas
(
    id
    SERIAL
    PRIMARY
    KEY,
    usuario_id
    INT
    REFERENCES
    usuarios
(
    id
),
    producto VARCHAR
(
    100
) NOT NULL,
    monto NUMERIC
(
    10,
    2
) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Datos iniciales de prueba
INSERT INTO usuarios (nombre, email)
VALUES ('Ana Gómez', 'ana@example.com'),
       ('Carlos López', 'carlos@example.com'),
       ('Lucía Fernández', 'lucia@example.com');

INSERT INTO ventas (usuario_id, producto, monto)
VALUES (1, 'Curso de SQL', 49.99),
       (1, 'Libro Java', 29.50),
       (2, 'Curso de Spring Boot', 89.00),
       (3, 'Suscripción Mensual', 15.00);