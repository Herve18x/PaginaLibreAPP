DROP DATABASE IF EXISTS libreriadb_in4cm;
CREATE DATABASE libreriadb_in4cm;
USE libreriadb_in4cm;

-- =============================================================================
-- 1. TABLAS BASE
-- =============================================================================

CREATE TABLE categoria (
    categoria_id INT PRIMARY KEY AUTO_INCREMENT,
    nombre_categoria VARCHAR(100) NOT NULL
);

CREATE TABLE editoriales (
    nit VARCHAR(20) PRIMARY KEY,
    nombre_editorial VARCHAR(100) NOT NULL,
    telefono_editorial VARCHAR(15),
    direccion_editorial VARCHAR(100)
);

CREATE TABLE proveedores (
    id_proveedor INT PRIMARY KEY AUTO_INCREMENT,
    nombre_proveedor VARCHAR(100) NOT NULL,
    telefono_proveedor VARCHAR(15),
    direccion_proveedor VARCHAR(100),
    correo_proveedor VARCHAR(100)
);

CREATE TABLE autores (
    id_autor INT PRIMARY KEY AUTO_INCREMENT,
    nombre_autor VARCHAR(100) NOT NULL,
    apellido_autor VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(100),
    biografia TEXT
);

CREATE TABLE clientes (
    cui BIGINT PRIMARY KEY,
    nombre_cliente VARCHAR(100),
    apellido_cliente VARCHAR(100),
    correo_electronico VARCHAR(100)
);

CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('admin','bodega','cajero') NOT NULL,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    correo VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE libros (
    isbn VARCHAR(20) PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    fecha_publicacion DATE,
    precio DECIMAL(8,2) NOT NULL,
    categoria_id INT,
    nit_editorial VARCHAR(20),
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE autores_libro (
    id_autor_libro INT AUTO_INCREMENT PRIMARY KEY,
    id_autor INT,
    isbn VARCHAR(20)
);

CREATE TABLE tipos_movimiento (
    id_tipo_movimiento INT PRIMARY KEY AUTO_INCREMENT,
    nombre_tipo VARCHAR(50) NOT NULL UNIQUE,
    operacion ENUM('SUMAR','RESTAR') NOT NULL
);

-- =============================================================================
-- 2. VENTAS
-- =============================================================================

CREATE TABLE ventas (
    id_venta INT PRIMARY KEY AUTO_INCREMENT,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado ENUM('COMPLETADA','ANULADA','DEVUELTA')
        NOT NULL DEFAULT 'COMPLETADA',
    cui_cliente BIGINT,
    id_usuario INT NOT NULL,
    usuario_autoriza_descuento INT,
    fecha_anulacion TIMESTAMP NULL,
    usuario_anulacion INT,
    motivo_anulacion VARCHAR(255)
);

CREATE TABLE detalle_venta (
    id_detalle INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT,
    isbn VARCHAR(20),
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

-- =============================================================================
-- 3. MOVIMIENTOS DE INVENTARIO
-- =============================================================================

CREATE TABLE movimientos_inventario (
    id_movimiento INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL,
    id_tipo_movimiento INT NOT NULL,
    cantidad INT NOT NULL,
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT NOT NULL,
    observacion VARCHAR(255),

    CONSTRAINT fk_mi_libro
        FOREIGN KEY (isbn)
        REFERENCES libros(isbn)
        ON DELETE CASCADE,

    CONSTRAINT fk_mi_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id),

    CONSTRAINT fk_mi_tipo
        FOREIGN KEY (id_tipo_movimiento)
        REFERENCES tipos_movimiento(id_tipo_movimiento)
);

-- =============================================================================
-- 4. LLAVES FORÁNEAS
-- =============================================================================

ALTER TABLE autores_libro
ADD CONSTRAINT fk_a_autor
    FOREIGN KEY (id_autor)
    REFERENCES autores(id_autor)
    ON DELETE CASCADE,
ADD CONSTRAINT fk_a_libro
    FOREIGN KEY (isbn)
    REFERENCES libros(isbn)
    ON DELETE CASCADE;

ALTER TABLE libros
ADD CONSTRAINT fk_a_categoria
    FOREIGN KEY (categoria_id)
    REFERENCES categoria(categoria_id)
    ON DELETE CASCADE,
ADD CONSTRAINT fk_a_editoriales
    FOREIGN KEY (nit_editorial)
    REFERENCES editoriales(nit)
    ON DELETE CASCADE;

ALTER TABLE ventas
ADD CONSTRAINT fk_v_cliente
    FOREIGN KEY (cui_cliente)
    REFERENCES clientes(cui)
    ON DELETE SET NULL,
ADD CONSTRAINT fk_v_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuarios(id),
ADD CONSTRAINT fk_v_autoriza_descuento
    FOREIGN KEY (usuario_autoriza_descuento)
    REFERENCES usuarios(id),
ADD CONSTRAINT fk_v_usuario_anulacion
    FOREIGN KEY (usuario_anulacion)
    REFERENCES usuarios(id);

ALTER TABLE detalle_venta
ADD CONSTRAINT fk_dv_venta
    FOREIGN KEY (id_venta)
    REFERENCES ventas(id_venta)
    ON DELETE CASCADE,
ADD CONSTRAINT fk_dv_libro
    FOREIGN KEY (isbn)
    REFERENCES libros(isbn)
    ON DELETE CASCADE;

-- =============================================================================
-- 5. CRUD CATEGORIAS
-- =============================================================================

DELIMITER $$

CREATE PROCEDURE sp_insertarcategoria(
    IN _nombre_categoria VARCHAR(100)
)
BEGIN
    INSERT INTO categoria(nombre_categoria)
    VALUES (_nombre_categoria);
END $$

CREATE PROCEDURE sp_listarcategorias()
BEGIN
    SELECT categoria_id, nombre_categoria
    FROM categoria;
END $$

CREATE PROCEDURE sp_buscarcategoria(
    IN _categoria_id INT
)
BEGIN
    SELECT categoria_id, nombre_categoria
    FROM categoria
    WHERE categoria_id = _categoria_id;
END $$

CREATE PROCEDURE sp_actualizarcategoria(
    IN _categoria_id INT,
    IN _nombre_categoria VARCHAR(100)
)
BEGIN
    UPDATE categoria
    SET nombre_categoria = _nombre_categoria
    WHERE categoria_id = _categoria_id;
END $$

CREATE PROCEDURE sp_eliminarcategoria(
    IN _categoria_id INT
)
BEGIN
    DELETE FROM categoria
    WHERE categoria_id = _categoria_id;
END $$

-- =============================================================================
-- 6. CRUD EDITORIALES
-- =============================================================================

CREATE PROCEDURE sp_insertareditorial(
    IN _nit VARCHAR(20),
    IN _nombre_editorial VARCHAR(100),
    IN _telefono_editorial VARCHAR(15),
    IN _direccion_editorial VARCHAR(100)
)
BEGIN
    INSERT INTO editoriales(
        nit,
        nombre_editorial,
        telefono_editorial,
        direccion_editorial
    )
    VALUES (
        _nit,
        _nombre_editorial,
        _telefono_editorial,
        _direccion_editorial
    );
END $$

CREATE PROCEDURE sp_listareditoriales()
BEGIN
    SELECT nit,
           nombre_editorial,
           telefono_editorial,
           direccion_editorial
    FROM editoriales;
END $$

CREATE PROCEDURE sp_buscareditorial(
    IN _nit VARCHAR(20)
)
BEGIN
    SELECT nit,
           nombre_editorial,
           telefono_editorial,
           direccion_editorial
    FROM editoriales
    WHERE nit = _nit;
END $$

CREATE PROCEDURE sp_actualizareditorial(
    IN _nit VARCHAR(20),
    IN _nombre_editorial VARCHAR(100),
    IN _telefono_editorial VARCHAR(15),
    IN _direccion_editorial VARCHAR(100)
)
BEGIN
    UPDATE editoriales
    SET nombre_editorial = _nombre_editorial,
        telefono_editorial = _telefono_editorial,
        direccion_editorial = _direccion_editorial
    WHERE nit = _nit;
END $$

CREATE PROCEDURE sp_eliminareditorial(
    IN _nit VARCHAR(20)
)
BEGIN
    DELETE FROM editoriales
    WHERE nit = _nit;
END $$

-- =============================================================================
-- 7. CRUD PROVEEDORES
-- =============================================================================

CREATE PROCEDURE sp_insertarproveedor(
    IN _nombre_proveedor VARCHAR(100),
    IN _telefono_proveedor VARCHAR(15),
    IN _direccion_proveedor VARCHAR(100),
    IN _correo_proveedor VARCHAR(100)
)
BEGIN
    INSERT INTO proveedores(
        nombre_proveedor,
        telefono_proveedor,
        direccion_proveedor,
        correo_proveedor
    )
    VALUES (
        _nombre_proveedor,
        _telefono_proveedor,
        _direccion_proveedor,
        _correo_proveedor
    );
END $$

CREATE PROCEDURE sp_listarproveedores()
BEGIN
    SELECT id_proveedor,
           nombre_proveedor,
           telefono_proveedor,
           direccion_proveedor,
           correo_proveedor
    FROM proveedores;
END $$

CREATE PROCEDURE sp_buscarproveedor(
    IN _id_proveedor INT
)
BEGIN
    SELECT id_proveedor,
           nombre_proveedor,
           telefono_proveedor,
           direccion_proveedor,
           correo_proveedor
    FROM proveedores
    WHERE id_proveedor = _id_proveedor;
END $$

CREATE PROCEDURE sp_actualizarproveedor(
    IN _id_proveedor INT,
    IN _nombre_proveedor VARCHAR(100),
    IN _telefono_proveedor VARCHAR(15),
    IN _direccion_proveedor VARCHAR(100),
    IN _correo_proveedor VARCHAR(100)
)
BEGIN
    UPDATE proveedores
    SET nombre_proveedor = _nombre_proveedor,
        telefono_proveedor = _telefono_proveedor,
        direccion_proveedor = _direccion_proveedor,
        correo_proveedor = _correo_proveedor
    WHERE id_proveedor = _id_proveedor;
END $$

CREATE PROCEDURE sp_eliminarproveedor(
    IN _id_proveedor INT
)
BEGIN
    DELETE FROM proveedores
    WHERE id_proveedor = _id_proveedor;
END $$

-- =============================================================================
-- 8. CRUD AUTORES
-- =============================================================================

CREATE PROCEDURE sp_insertarautor(
    IN _nombre_autor VARCHAR(100),
    IN _apellido_autor VARCHAR(100),
    IN _nacionalidad VARCHAR(100),
    IN _biografia TEXT
)
BEGIN
    INSERT INTO autores(
        nombre_autor,
        apellido_autor,
        nacionalidad,
        biografia
    )
    VALUES (
        _nombre_autor,
        _apellido_autor,
        _nacionalidad,
        _biografia
    );
END $$

CREATE PROCEDURE sp_listarautores()
BEGIN
    SELECT id_autor,
           nombre_autor,
           apellido_autor,
           nacionalidad,
           biografia
    FROM autores;
END $$

CREATE PROCEDURE sp_buscarautor(
    IN _id_autor INT
)
BEGIN
    SELECT id_autor,
           nombre_autor,
           apellido_autor,
           nacionalidad,
           biografia
    FROM autores
    WHERE id_autor = _id_autor;
END $$

CREATE PROCEDURE sp_actualizarautor(
    IN _id_autor INT,
    IN _nombre_autor VARCHAR(100),
    IN _apellido_autor VARCHAR(100),
    IN _nacionalidad VARCHAR(100),
    IN _biografia TEXT
)
BEGIN
    UPDATE autores
    SET nombre_autor = _nombre_autor,
        apellido_autor = _apellido_autor,
        nacionalidad = _nacionalidad,
        biografia = _biografia
    WHERE id_autor = _id_autor;
END $$

CREATE PROCEDURE sp_eliminarautor(
    IN _id_autor INT
)
BEGIN
    DELETE FROM autores
    WHERE id_autor = _id_autor;
END $$

-- =============================================================================
-- 9. CRUD CLIENTES
-- =============================================================================

CREATE PROCEDURE sp_insertarcliente(
    IN _cui BIGINT,
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _correo_electronico VARCHAR(100)
)
BEGIN
    INSERT INTO clientes(
        cui,
        nombre_cliente,
        apellido_cliente,
        correo_electronico
    )
    VALUES (
        _cui,
        _nombre_cliente,
        _apellido_cliente,
        _correo_electronico
    );
END $$

CREATE PROCEDURE sp_listarclientes()
BEGIN
    SELECT cui,
           nombre_cliente,
           apellido_cliente,
           correo_electronico
    FROM clientes;
END $$

CREATE PROCEDURE sp_buscarcliente(
    IN _cui BIGINT
)
BEGIN
    SELECT cui,
           nombre_cliente,
           apellido_cliente,
           correo_electronico
    FROM clientes
    WHERE cui = _cui;
END $$

CREATE PROCEDURE sp_actualizarcliente(
    IN _cui BIGINT,
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _correo_electronico VARCHAR(100)
)
BEGIN
    UPDATE clientes
    SET nombre_cliente = _nombre_cliente,
        apellido_cliente = _apellido_cliente,
        correo_electronico = _correo_electronico
    WHERE cui = _cui;
END $$

CREATE PROCEDURE sp_eliminarcliente(
    IN _cui BIGINT
)
BEGIN
    DELETE FROM clientes
    WHERE cui = _cui;
END $$

-- =============================================================================
-- 10. CRUD USUARIOS
-- =============================================================================

CREATE PROCEDURE sp_registrar_usuario(
    IN _username VARCHAR(50),
    IN _password_hash VARCHAR(255),
    IN _rol ENUM('admin','bodega','cajero'),
    IN _nombre VARCHAR(100),
    IN _apellido VARCHAR(100),
    IN _correo VARCHAR(100)
)
BEGIN
    INSERT INTO usuarios(
        username,
        password_hash,
        rol,
        nombre,
        apellido,
        correo
    )
    VALUES (
        _username,
        _password_hash,
        _rol,
        _nombre,
        _apellido,
        _correo
    );
END $$

CREATE PROCEDURE sp_iniciar_sesion(
    IN _username VARCHAR(50)
)
BEGIN
    SELECT id,
           username,
           password_hash,
           rol,
           activo
    FROM usuarios
    WHERE username = _username
      AND activo = TRUE;
END $$

CREATE PROCEDURE sp_listarusuarios()
BEGIN
    SELECT id,
           username,
           password_hash,
           rol,
           nombre,
           apellido,
           correo,
           activo,
           fecha_creacion
    FROM usuarios;
END $$

CREATE PROCEDURE sp_cambiar_password(
    IN _id INT,
    IN _password_actual_hash VARCHAR(255),
    IN _password_nuevo_hash VARCHAR(255)
)
BEGIN
    UPDATE usuarios
    SET password_hash = _password_nuevo_hash,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = _id
      AND password_hash = _password_actual_hash;
END $$

CREATE PROCEDURE sp_desactivarusuario(
    IN _id INT
)
BEGIN
    UPDATE usuarios
    SET activo = FALSE
    WHERE id = _id;
END $$

-- =============================================================================
-- 11. CRUD LIBROS
-- =============================================================================

CREATE PROCEDURE sp_insertarlibro(
    IN _isbn VARCHAR(20),
    IN _titulo VARCHAR(100),
    IN _fecha_publicacion DATE,
    IN _precio DECIMAL(8,2),
    IN _categoria_id INT,
    IN _nit_editorial VARCHAR(20),
    IN _stock_actual INT,
    IN _stock_minimo INT
)
BEGIN
    INSERT INTO libros(
        isbn,
        titulo,
        fecha_publicacion,
        precio,
        categoria_id,
        nit_editorial,
        stock_actual,
        stock_minimo
    )
    VALUES (
        _isbn,
        _titulo,
        _fecha_publicacion,
        _precio,
        _categoria_id,
        _nit_editorial,
        _stock_actual,
        _stock_minimo
    );
END $$

CREATE PROCEDURE sp_listarlibros()
BEGIN
    SELECT isbn,
           titulo,
           fecha_publicacion,
           precio,
           categoria_id,
           nit_editorial,
           stock_actual,
           stock_minimo,
           estado
    FROM libros;
END $$

CREATE PROCEDURE sp_buscarlibro(
    IN _isbn VARCHAR(20)
)
BEGIN
    SELECT isbn,
           titulo,
           fecha_publicacion,
           precio,
           categoria_id,
           nit_editorial,
           stock_actual,
           stock_minimo,
           estado
    FROM libros
    WHERE isbn = _isbn;
END $$

CREATE PROCEDURE sp_actualizarlibro(
    IN _isbn VARCHAR(20),
    IN _titulo VARCHAR(100),
    IN _fecha_publicacion DATE,
    IN _precio DECIMAL(8,2),
    IN _categoria_id INT,
    IN _nit_editorial VARCHAR(20)
)
BEGIN
    UPDATE libros
    SET titulo = _titulo,
        fecha_publicacion = _fecha_publicacion,
        precio = _precio,
        categoria_id = _categoria_id,
        nit_editorial = _nit_editorial
    WHERE isbn = _isbn;
END $$

CREATE PROCEDURE sp_libros_bajo_stock_minimo()
BEGIN
    SELECT isbn,
           titulo,
           stock_actual,
           stock_minimo
    FROM libros
    WHERE stock_actual <= stock_minimo
      AND estado = 1;
END $$

CREATE PROCEDURE sp_eliminarlibro(
    IN _isbn VARCHAR(20)
)
BEGIN
    UPDATE libros
    SET estado = 0
    WHERE isbn = _isbn;
END $$

-- =============================================================================
-- 12. AUTORES_LIBRO
-- =============================================================================

CREATE PROCEDURE sp_insertarautorlibro(
    IN _id_autor INT,
    IN _isbn VARCHAR(20)
)
BEGIN
    INSERT INTO autores_libro(id_autor, isbn)
    VALUES (_id_autor, _isbn);
END $$

CREATE PROCEDURE sp_listarautoreslibro()
BEGIN
    SELECT id_autor_libro,
           id_autor,
           isbn
    FROM autores_libro;
END $$

CREATE PROCEDURE sp_buscarautorlibro(
    IN _id_autor_libro INT
)
BEGIN
    SELECT id_autor_libro,
           id_autor,
           isbn
    FROM autores_libro
    WHERE id_autor_libro = _id_autor_libro;
END $$

CREATE PROCEDURE sp_actualizarautorlibro(
    IN _id_autor_libro INT,
    IN _id_autor INT,
    IN _isbn VARCHAR(20)
)
BEGIN
    UPDATE autores_libro
    SET id_autor = _id_autor,
        isbn = _isbn
    WHERE id_autor_libro = _id_autor_libro;
END $$

CREATE PROCEDURE sp_eliminarautorlibro(
    IN _id_autor_libro INT
)
BEGIN
    DELETE FROM autores_libro
    WHERE id_autor_libro = _id_autor_libro;
END $$

-- =============================================================================
-- 13. MOVIMIENTOS DE INVENTARIO
-- =============================================================================

CREATE PROCEDURE sp_registrar_movimiento_inventario(
    IN _isbn VARCHAR(20),
    IN _id_tipo_movimiento INT,
    IN _cantidad INT,
    IN _id_usuario INT,
    IN _observacion VARCHAR(255)
)
BEGIN
    DECLARE _delta INT DEFAULT 0;
    DECLARE _operacion VARCHAR(10);

    SELECT operacion
    INTO _operacion
    FROM tipos_movimiento
    WHERE id_tipo_movimiento = _id_tipo_movimiento;

    IF _operacion = 'SUMAR' THEN
        SET _delta = _cantidad;
    ELSE
        SET _delta = -_cantidad;
    END IF;

    INSERT INTO movimientos_inventario(
        isbn,
        id_tipo_movimiento,
        cantidad,
        id_usuario,
        observacion
    )
    VALUES (
        _isbn,
        _id_tipo_movimiento,
        _cantidad,
        _id_usuario,
        _observacion
    );

    UPDATE libros
    SET stock_actual = stock_actual + _delta
    WHERE isbn = _isbn;
END $$

CREATE PROCEDURE sp_agregardetalleventa(
    IN _id_venta INT,
    IN _isbn VARCHAR(20),
    IN _cantidad INT,
    IN _id_usuario INT
)
BEGIN
    DECLARE _precio DECIMAL(8,2);
    DECLARE _subtotal_linea DECIMAL(10,2);
    DECLARE _stock INT;

    SELECT precio, stock_actual
    INTO _precio, _stock
    FROM libros
    WHERE isbn = _isbn;

    IF _stock < _cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente para realizar la venta';
    END IF;

    SET _subtotal_linea = _precio * _cantidad;

    INSERT INTO detalle_venta(
        id_venta,
        isbn,
        cantidad,
        precio_unitario,
        subtotal
    )
    VALUES (
        _id_venta,
        _isbn,
        _cantidad,
        _precio,
        _subtotal_linea
    );

    CALL sp_registrar_movimiento_inventario(
        _isbn,
        2,
        _cantidad,
        _id_usuario,
        CONCAT('Venta #', _id_venta)
    );

    UPDATE ventas v
    SET v.subtotal = (
        SELECT COALESCE(SUM(subtotal),0)
        FROM detalle_venta
        WHERE id_venta = _id_venta
    ),
    v.total = v.subtotal - v.descuento
    WHERE v.id_venta = _id_venta;
END $$

-- =============================================================================
-- 14. VENTAS
-- =============================================================================

CREATE PROCEDURE sp_insertarventa(
    IN _cui_cliente BIGINT,
    IN _id_usuario INT,
    OUT _id_venta INT
)
BEGIN
    INSERT INTO ventas(
        subtotal,
        descuento,
        total,
        estado,
        cui_cliente,
        id_usuario
    )
    VALUES (
        0,
        0,
        0,
        'COMPLETADA',
        _cui_cliente,
        _id_usuario
    );

    SET _id_venta = LAST_INSERT_ID();
END $$

CREATE PROCEDURE sp_aplicardescuentoventa(
    IN _id_venta INT,
    IN _descuento DECIMAL(10,2),
    IN _usuario_autoriza INT
)
BEGIN
    UPDATE ventas
    SET descuento = _descuento,
        total = subtotal - _descuento,
        usuario_autoriza_descuento = _usuario_autoriza
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_listarventas()
BEGIN
    SELECT id_venta,
           fecha_venta,
           subtotal,
           descuento,
           total,
           estado,
           cui_cliente,
           id_usuario
    FROM ventas;
END $$

CREATE PROCEDURE sp_buscarventa(
    IN _id_venta INT
)
BEGIN
    SELECT id_venta,
           fecha_venta,
           subtotal,
           descuento,
           total,
           estado,
           cui_cliente,
           id_usuario
    FROM ventas
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_resumenventasdelcajero(
    IN _id_usuario INT,
    IN _fecha DATE
)
BEGIN
    SELECT id_venta,
           fecha_venta,
           subtotal,
           descuento,
           total,
           estado
    FROM ventas
    WHERE id_usuario = _id_usuario
      AND DATE(fecha_venta) = _fecha
      AND estado = 'COMPLETADA';
END $$

CREATE PROCEDURE sp_listardetalleventa(
    IN _id_venta INT
)
BEGIN
    SELECT id_detalle,
           id_venta,
           isbn,
           cantidad,
           precio_unitario,
           subtotal
    FROM detalle_venta
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_anularventa(
    IN _id_venta INT,
    IN _usuario_anulacion INT,
    IN _motivo VARCHAR(255)
)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_isbn VARCHAR(20);
    DECLARE v_cantidad INT;

    DECLARE cur CURSOR FOR
        SELECT isbn, cantidad
        FROM detalle_venta
        WHERE id_venta = _id_venta;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    read_loop: LOOP

        FETCH cur INTO v_isbn, v_cantidad;

        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        CALL sp_registrar_movimiento_inventario(
            v_isbn,
            5,
            v_cantidad,
            _usuario_anulacion,
            CONCAT('Anulación venta #', _id_venta)
        );

    END LOOP;

    CLOSE cur;

    UPDATE ventas
    SET estado = 'ANULADA',
        fecha_anulacion = CURRENT_TIMESTAMP,
        usuario_anulacion = _usuario_anulacion,
        motivo_anulacion = _motivo
    WHERE id_venta = _id_venta;
END $$

DELIMITER ;

-- =============================================================================
-- 15. VISTAS
-- =============================================================================

CREATE OR REPLACE VIEW vw_lista_categorias AS
SELECT
    categoria_id AS 'id categoría',
    nombre_categoria AS 'categoría'
FROM categoria;

CREATE OR REPLACE VIEW vw_lista_editoriales AS
SELECT
    nit AS 'nit editorial',
    nombre_editorial AS 'editorial',
    telefono_editorial AS 'teléfono',
    direccion_editorial AS 'dirección'
FROM editoriales;

CREATE OR REPLACE VIEW vw_lista_proveedores AS
SELECT
    id_proveedor AS 'id proveedor',
    nombre_proveedor AS 'proveedor',
    telefono_proveedor AS 'teléfono',
    direccion_proveedor AS 'dirección',
    correo_proveedor AS 'correo'
FROM proveedores;

CREATE OR REPLACE VIEW vw_lista_autores AS
SELECT
    id_autor AS 'id autor',
    CONCAT(nombre_autor,' ',apellido_autor) AS 'autor',
    nacionalidad AS 'nacionalidad',
    biografia AS 'biografía'
FROM autores;

CREATE OR REPLACE VIEW vw_lista_clientes AS
SELECT
    cui AS 'cui cliente',
    CONCAT(nombre_cliente,' ',apellido_cliente) AS 'cliente',
    correo_electronico AS 'correo electrónico'
FROM clientes;

CREATE OR REPLACE VIEW vw_lista_usuarios AS
SELECT
    id AS 'id usuario',
    username AS 'usuario',
    password_hash AS 'password_hash',
    rol AS 'rol',
    CONCAT(nombre,' ',apellido) AS 'nombre completo',
    correo AS 'correo',
    activo AS 'activo'
FROM usuarios;

CREATE OR REPLACE VIEW vw_lista_libros AS
SELECT
    l.isbn AS 'isbn',
    l.titulo AS 'título',
    l.fecha_publicacion AS 'fecha de publicación',
    l.precio AS 'precio',
    c.nombre_categoria AS 'categoría',
    e.nombre_editorial AS 'editorial',
    l.stock_actual AS 'stock actual',
    l.stock_minimo AS 'stock mínimo',
    l.estado AS 'activo'
FROM libros l
INNER JOIN categoria c
    ON l.categoria_id = c.categoria_id
INNER JOIN editoriales e
    ON l.nit_editorial = e.nit;

CREATE OR REPLACE VIEW vw_libros_bajo_stock AS
SELECT
    isbn AS 'isbn',
    titulo AS 'título',
    stock_actual AS 'stock actual',
    stock_minimo AS 'stock mínimo'
FROM libros
WHERE stock_actual <= stock_minimo
  AND estado = 1;

CREATE OR REPLACE VIEW vw_lista_autores_libro AS
SELECT
    al.id_autor_libro AS 'id relación',
    CONCAT(a.nombre_autor,' ',a.apellido_autor) AS 'autor',
    l.titulo AS 'título del libro',
    l.isbn AS 'isbn'
FROM autores_libro al
INNER JOIN autores a
    ON al.id_autor = a.id_autor
INNER JOIN libros l
    ON al.isbn = l.isbn;

CREATE OR REPLACE VIEW vw_lista_ventas AS
SELECT
    v.id_venta AS 'no. venta',
    v.fecha_venta AS 'fecha/hora',
    v.subtotal AS 'subtotal',
    v.descuento AS 'descuento',
    v.total AS 'total',
    v.estado AS 'estado',
    v.cui_cliente AS 'cui cliente',
    CONCAT(cl.nombre_cliente,' ',cl.apellido_cliente) AS 'cliente',
    u.username AS 'cajero'
FROM ventas v
LEFT JOIN clientes cl
    ON v.cui_cliente = cl.cui
INNER JOIN usuarios u
    ON v.id_usuario = u.id;

CREATE OR REPLACE VIEW vw_lista_detalle_venta AS
SELECT
    dv.id_detalle AS 'id detalle',
    dv.id_venta AS 'no. venta',
    l.titulo AS 'libro',
    l.isbn AS 'isbn',
    dv.cantidad AS 'cantidad',
    dv.precio_unitario AS 'precio unitario',
    dv.subtotal AS 'subtotal'
FROM detalle_venta dv
INNER JOIN libros l
    ON dv.isbn = l.isbn;

CREATE OR REPLACE VIEW vw_factura_ventas AS
SELECT
    v.id_venta AS 'numero_factura',
    v.fecha_venta AS 'fecha_emision',
    cl.cui AS 'cui_cliente',
    CONCAT(cl.nombre_cliente,' ',cl.apellido_cliente) AS 'nombre_cliente',
    cl.correo_electronico AS 'correo_cliente',
    l.isbn AS 'isbn_libro',
    l.titulo AS 'descripcion_libro',
    dv.cantidad AS 'cantidad',
    dv.precio_unitario AS 'precio_unitario',
    dv.subtotal AS 'subtotal_linea',
    v.descuento AS 'descuento',
    v.total AS 'gran_total',
    u.username AS 'atendido_por'
FROM ventas v
LEFT JOIN clientes cl
    ON v.cui_cliente = cl.cui
INNER JOIN detalle_venta dv
    ON v.id_venta = dv.id_venta
INNER JOIN libros l
    ON dv.isbn = l.isbn
INNER JOIN usuarios u
    ON v.id_usuario = u.id;

CREATE OR REPLACE VIEW vw_movimientos_inventario AS
SELECT
    mi.id_movimiento AS 'id movimiento',
    l.titulo AS 'libro',
    mi.isbn AS 'isbn',
    tm.nombre_tipo AS 'tipo',
    mi.cantidad AS 'cantidad',
    mi.fecha_movimiento AS 'fecha',
    u.username AS 'usuario',
    mi.observacion AS 'observación'
FROM movimientos_inventario mi
INNER JOIN libros l
    ON mi.isbn = l.isbn
INNER JOIN tipos_movimiento tm
    ON mi.id_tipo_movimiento = tm.id_tipo_movimiento
INNER JOIN usuarios u
    ON mi.id_usuario = u.id;

-- =============================================================================
-- 16. DATOS INICIALES
-- =============================================================================

-- TIPOS DE MOVIMIENTO
INSERT INTO tipos_movimiento(nombre_tipo, operacion)
VALUES
('INGRESO','SUMAR'),
('VENTA','RESTAR'),
('MERMA','RESTAR'),
('TRASLADO','RESTAR'),
('DEVOLUCION','SUMAR'),
('AJUSTE','RESTAR');

-- =============================================================================
-- CATEGORIAS
-- =============================================================================

CALL sp_insertarcategoria('Ficción Cósmica');
CALL sp_insertarcategoria('Fantasía Épica');
CALL sp_insertarcategoria('Ciencia Ficción');
CALL sp_insertarcategoria('Novela Negra');
CALL sp_insertarcategoria('Misterio');
CALL sp_insertarcategoria('Biografía');
CALL sp_insertarcategoria('Historia Universal');
CALL sp_insertarcategoria('Poesía Contemporánea');
CALL sp_insertarcategoria('Romance');
CALL sp_insertarcategoria('Terror Psicológico');
CALL sp_insertarcategoria('Autoayuda');
CALL sp_insertarcategoria('Desarrollo Personal');
CALL sp_insertarcategoria('Filosofía');
CALL sp_insertarcategoria('Arte Moderno');
CALL sp_insertarcategoria('Religión y Espiritualidad');
CALL sp_insertarcategoria('Cuentos Infantiles');
CALL sp_insertarcategoria('Literatura Juvenil');
CALL sp_insertarcategoria('Cómics y Manga');
CALL sp_insertarcategoria('Gastronomía');
CALL sp_insertarcategoria('Crónicas de Viajes');

-- =============================================================================
-- EDITORIALES
-- =============================================================================

CALL sp_insertareditorial('1001-A','Editorial Planeta','22334455','Zona 1, Ciudad');
CALL sp_insertareditorial('1002-B','Penguin Random House','22334456','Zona 10, Ciudad');
CALL sp_insertareditorial('1003-C','Editorial Santillana','22334457','Zona 9, Ciudad');
CALL sp_insertareditorial('1004-D','Ediciones Salamandra','22334458','Zona 14, Ciudad');
CALL sp_insertareditorial('1005-E','Anagrama','22334459','Zona 4, Ciudad');
CALL sp_insertareditorial('1006-F','Alfaguara','22334460','Zona 15, Ciudad');
CALL sp_insertareditorial('1007-G','Seix Barral','22334461','Zona 1, Ciudad');
CALL sp_insertareditorial('1008-H','Tusquets Editores','22334462','Zona 2, Ciudad');
CALL sp_insertareditorial('1009-I','Lumen','22334463','Zona 11, Ciudad');
CALL sp_insertareditorial('1010-J','Debolsillo','22334464','Zona 12, Ciudad');
CALL sp_insertareditorial('1011-K','Ediciones B','22334465','Zona 13, Ciudad');
CALL sp_insertareditorial('1012-L','Roca Editorial','22334466','Zona 16, Ciudad');
CALL sp_insertareditorial('1013-M','Ediciones Minotauro','22334467','Zona 5, Ciudad');
CALL sp_insertareditorial('1014-N','Suma de Letras','22334468','Zona 6, Ciudad');
CALL sp_insertareditorial('1015-O','Plaza & Janés','22334469','Zona 7, Ciudad');
CALL sp_insertareditorial('1016-P','Editorial Siruela','22334470','Zona 8, Ciudad');
CALL sp_insertareditorial('1017-Q','Ediciones Destino','22334471','Zona 18, Ciudad');
CALL sp_insertareditorial('1018-R','Acantilado','22334472','Zona 21, Ciudad');
CALL sp_insertareditorial('1019-S','Editorial Piedra Santa','22334473','Zona 1, Ciudad');
CALL sp_insertareditorial('1020-T','Fondo de Cultura Económica','22334474','Zona 9, Ciudad');

-- =============================================================================
-- PROVEEDORES
-- =============================================================================

CALL sp_insertarproveedor('Distribuidora Nacional de Libros','23001001','Zona 1, Ciudad','contacto@dnl.com');
CALL sp_insertarproveedor('Importadora Cultural S.A.','23001002','Zona 4, Ciudad','ventas@importcultural.com');
CALL sp_insertarproveedor('Papelera San Miguel','23001003','Zona 11, Ciudad','info@papelerasanmiguel.com');
CALL sp_insertarproveedor('Suministros Editoriales GT','23001004','Zona 7, Ciudad','contacto@sumeditorialesgt.com');
CALL sp_insertarproveedor('Distribuciones Quetzal','23001005','Zona 9, Ciudad','ventas@distquetzal.com');
CALL sp_insertarproveedor('Importaciones Libro Mundo','23001006','Zona 2, Ciudad','info@libromundo.com');
CALL sp_insertarproveedor('Grupo Distribuidor Maya','23001007','Zona 15, Ciudad','contacto@grupomaya.com');
CALL sp_insertarproveedor('Comercializadora Andina','23001008','Zona 13, Ciudad','ventas@comercialandina.com');
CALL sp_insertarproveedor('Distribuidora Continental de Libros','23001009','Zona 6, Ciudad','info@dcl.com');
CALL sp_insertarproveedor('Suministros y Papel S.A.','23001010','Zona 16, Ciudad','contacto@sumypapel.com');
CALL sp_insertarproveedor('Importadora del Atlántico','23001011','Zona 21, Ciudad','ventas@importatlantico.com');
CALL sp_insertarproveedor('Distribuidora Pacífico Libros','23001012','Zona 8, Ciudad','info@pacificolibros.com');
CALL sp_insertarproveedor('Grupo Editorial Insumos','23001013','Zona 12, Ciudad','contacto@geinsumos.com');
CALL sp_insertarproveedor('Papel y Tinta S.A.','23001014','Zona 5, Ciudad','ventas@papelytinta.com');
CALL sp_insertarproveedor('Distribuidora Metropolitana','23001015','Zona 10, Ciudad','info@distmetropolitana.com');
CALL sp_insertarproveedor('Importaciones Culturales Unidas','23001016','Zona 14, Ciudad','contacto@icunidas.com');
CALL sp_insertarproveedor('Suministros Gráficos GT','23001017','Zona 18, Ciudad','ventas@sumgraficosgt.com');
CALL sp_insertarproveedor('Distribuidora Central de Libros','23001018','Zona 3, Ciudad','info@dcentrallibros.com');
CALL sp_insertarproveedor('Comercial Andina de Papel','23001019','Zona 17, Ciudad','contacto@candinapapel.com');
CALL sp_insertarproveedor('Grupo Logístico Editorial','23001020','Zona 19, Ciudad','ventas@glogisticoeditorial.com');

-- =============================================================================
-- AUTORES
-- =============================================================================

CALL sp_insertarautor('Gabriel','García Márquez','Colombiana','Premio Nobel de Literatura 1982. Exponente del realismo mágico.');
CALL sp_insertarautor('Julio','Cortázar','Argentina','Maestro del relato corto y creador de Rayuela.');
CALL sp_insertarautor('Isabel','Allende','Chilena','Autora de La Casa de los Espíritus.');
CALL sp_insertarautor('Jorge Luis','Borges','Argentina','Escritor de ficciones, poemas y ensayos.');
CALL sp_insertarautor('Miguel','Ángel Asturias','Guatemalteca','Premio Nobel de Literatura 1967.');
CALL sp_insertarautor('J.K.','Rowling','Británica','Creadora del mundo mágico de Harry Potter.');
CALL sp_insertarautor('George R.R.','Martin','Estadounidense','Autor de Canción de Hielo y Fuego.');
CALL sp_insertarautor('Stephen','King','Estadounidense','Autor contemporáneo de terror y suspenso.');
CALL sp_insertarautor('Haruki','Murakami','Japonesa','Autor de Tokio Blues.');
CALL sp_insertarautor('Jane','Austen','Británica','Autora clásica de Orgullo y Prejuicio.');
CALL sp_insertarautor('Edgar Allan','Poe','Estadounidense','Padre del cuento de terror.');
CALL sp_insertarautor('Agatha','Christie','Británica','Autora de novelas de misterio.');
CALL sp_insertarautor('Isaac','Asimov','Rusa/Estadounidense','Maestro de la ciencia ficción.');
CALL sp_insertarautor('J.R.R.','Tolkien','Británica','Creador de la Tierra Media.');
CALL sp_insertarautor('Virginia','Woolf','Británica','Figura del modernismo literario.');
CALL sp_insertarautor('Fiódor','Dostoievski','Rusa','Autor de Crimen y Castigo.');
CALL sp_insertarautor('Franz','Kafka','Checa','Autor de La Metamorfosis.');
CALL sp_insertarautor('Oscar','Wilde','Irlandesa','Autor de El Retrato de Dorian Gray.');
CALL sp_insertarautor('Mario','Vargas Llosa','Peruana','Premio Nobel de Literatura 2010.');
CALL sp_insertarautor('Margaret','Atwood','Canadiense','Autora de El cuento de la criada.');

-- =============================================================================
-- CLIENTES
-- =============================================================================

CALL sp_insertarcliente(2000100010101,'Ana','López','ana.l@gmail.com');
CALL sp_insertarcliente(2000100020101,'Carlos','Méndez','cmendez@yahoo.com');
CALL sp_insertarcliente(2000100030101,'Luis','Pérez','lperez@hotmail.com');
CALL sp_insertarcliente(2000100040101,'María','García','mgarcia@gmail.com');
CALL sp_insertarcliente(2000100050101,'Jorge','Castillo','jcastillo@gmail.com');
CALL sp_insertarcliente(2000100060101,'Lucía','Fernández','lfernandez@yahoo.com');
CALL sp_insertarcliente(2000100070101,'Mario','Gómez','mgomez@gmail.com');
CALL sp_insertarcliente(2000100080101,'Elena','Morales','emorales@hotmail.com');
CALL sp_insertarcliente(2000100090101,'Pedro','Ramírez','pramirez@gmail.com');
CALL sp_insertarcliente(2000100100101,'Sofía','Vásquez','svasquez@gmail.com');
CALL sp_insertarcliente(2000100110101,'Diego','Hernández','dhernandez@yahoo.com');
CALL sp_insertarcliente(2000100120101,'Camila','Cruz','ccruz@hotmail.com');
CALL sp_insertarcliente(2000100130101,'Andrés','Reyes','areyes@gmail.com');
CALL sp_insertarcliente(2000100140101,'Valeria','Ortiz','vortiz@gmail.com');
CALL sp_insertarcliente(2000100150101,'Javier','Flores','jflores@yahoo.com');
CALL sp_insertarcliente(2000100160101,'Daniela','Díaz','ddiaz@gmail.com');
CALL sp_insertarcliente(2000100170101,'Ricardo','Alonso','ralonso@hotmail.com');
CALL sp_insertarcliente(2000100180101,'Gabriela','Rojas','grojas@gmail.com');
CALL sp_insertarcliente(2000100190101,'Héctor','Salazar','hsalazar@yahoo.com');
CALL sp_insertarcliente(2000100200101,'Mónica','Herrera','mherrera@gmail.com');

INSERT INTO clientes(
    cui,
    nombre_cliente,
    apellido_cliente,
    correo_electronico
)
VALUES (
    0,
    'Consumidor',
    'Final',
    'cf@correo.com'
);

-- =============================================================================
-- USUARIOS
-- =============================================================================

CALL sp_registrar_usuario(
    'admin1',
    SHA2('Admin#2026',256),
    'admin',
    'Sofía',
    'Reyes',
    'sofia.reyes@libreria.com'
);

CALL sp_registrar_usuario(
    'bodega1',
    SHA2('Bodega#2026',256),
    'bodega',
    'Luis',
    'Ramírez',
    'luis.ramirez@libreria.com'
);

CALL sp_registrar_usuario(
    'cajero1',
    SHA2('Cajero#2026',256),
    'cajero',
    'Paola',
    'Cruz',
    'paola.cruz@libreria.com'
);

-- =============================================================================
-- LIBROS
-- =============================================================================

CALL sp_insertarlibro('978-0-123','Cien Años de Soledad','1967-05-30',150.00,1,'1001-A',0,10);
CALL sp_insertarlibro('978-0-124','Rayuela','1963-06-28',135.50,1,'1002-B',0,8);
CALL sp_insertarlibro('978-0-125','El Señor Presidente','1946-01-01',120.00,1,'1019-S',0,10);
CALL sp_insertarlibro('978-0-126','Harry Potter y la Piedra Filosofal','1997-06-26',180.00,2,'1004-D',0,15);
CALL sp_insertarlibro('978-0-127','El Resplandor','1977-01-28',165.00,10,'1005-E',0,5);
CALL sp_insertarlibro('978-0-128','Fundación','1951-05-01',140.00,3,'1013-M',0,10);
CALL sp_insertarlibro('978-0-129','El Señor de los Anillos','1954-07-29',250.00,2,'1013-M',0,12);
CALL sp_insertarlibro('978-0-130','Crimen y Castigo','1866-01-01',95.00,1,'1007-G',0,6);
CALL sp_insertarlibro('978-0-131','Diez Negritos','1939-11-06',110.00,5,'1008-H',0,8);
CALL sp_insertarlibro('978-0-132','Orgullo y Prejuicio','1813-01-28',85.00,9,'1009-I',0,10);
CALL sp_insertarlibro('978-0-133','La Casa de los Espíritus','1982-01-01',145.00,1,'1001-A',0,8);
CALL sp_insertarlibro('978-0-134','El Cuento de la Criada','1985-01-01',160.00,3,'1004-D',0,5);
CALL sp_insertarlibro('978-0-135','La Metamorfosis','1915-01-01',90.00,10,'1006-F',0,5);
CALL sp_insertarlibro('978-0-136','Tokio Blues (Norwegian Wood)','1987-08-04',140.00,9,'1009-I',0,6);
CALL sp_insertarlibro('978-0-137','Narraciones Extraordinarias','1845-01-01',95.00,10,'1016-P',0,5);
CALL sp_insertarlibro('978-0-138','El Retrato de Dorian Gray','1890-07-01',100.00,14,'1018-R',0,5);
CALL sp_insertarlibro('978-0-139','Mrs. Dalloway','1925-05-14',110.00,1,'1005-E',0,4);
CALL sp_insertarlibro('978-0-140','La Ciudad y los Perros','1963-01-01',130.00,4,'1015-O',0,6);
CALL sp_insertarlibro('978-0-141','Juego de Tronos','1996-08-01',200.00,2,'1011-K',0,10);
CALL sp_insertarlibro('978-0-142','Los Hermanos Karamazov','1880-11-01',190.00,1,'1007-G',0,5);

-- =============================================================================
-- AUTORES_LIBRO
-- =============================================================================

CALL sp_insertarautorlibro(1,'978-0-123');
CALL sp_insertarautorlibro(2,'978-0-124');
CALL sp_insertarautorlibro(5,'978-0-125');
CALL sp_insertarautorlibro(6,'978-0-126');
CALL sp_insertarautorlibro(8,'978-0-127');
CALL sp_insertarautorlibro(13,'978-0-128');
CALL sp_insertarautorlibro(14,'978-0-129');
CALL sp_insertarautorlibro(16,'978-0-130');
CALL sp_insertarautorlibro(12,'978-0-131');
CALL sp_insertarautorlibro(10,'978-0-132');
CALL sp_insertarautorlibro(3,'978-0-133');
CALL sp_insertarautorlibro(20,'978-0-134');
CALL sp_insertarautorlibro(17,'978-0-135');
CALL sp_insertarautorlibro(9,'978-0-136');
CALL sp_insertarautorlibro(11,'978-0-137');
CALL sp_insertarautorlibro(18,'978-0-138');
CALL sp_insertarautorlibro(15,'978-0-139');
CALL sp_insertarautorlibro(19,'978-0-140');
CALL sp_insertarautorlibro(7,'978-0-141');
CALL sp_insertarautorlibro(16,'978-0-142');



CALL sp_registrar_movimiento_inventario('978-0-123',1,40,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-124',1,25,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-125',1,30,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-126',1,50,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-127',1,20,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-128',1,35,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-129',1,45,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-130',1,22,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-131',1,28,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-132',1,33,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-133',1,26,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-134',1,18,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-135',1,20,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-136',1,24,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-137',1,20,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-138',1,18,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-139',1,15,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-140',1,22,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-141',1,35,2,'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-142',1,15,2,'Carga inicial de stock');


CALL sp_insertarventa(2000100010101,3,@v1);
CALL sp_agregardetalleventa(@v1,'978-0-123',2,3);

CALL sp_insertarventa(2000100020101,3,@v2);
CALL sp_agregardetalleventa(@v2,'978-0-124',3,3);

CALL sp_insertarventa(2000100030101,3,@v3);
CALL sp_agregardetalleventa(@v3,'978-0-125',1,3);

CALL sp_insertarventa(2000100040101,3,@v4);
CALL sp_agregardetalleventa(@v4,'978-0-126',2,3);

CALL sp_insertarventa(2000100050101,3,@v5);
CALL sp_agregardetalleventa(@v5,'978-0-127',3,3);

CALL sp_insertarventa(2000100060101,3,@v6);
CALL sp_agregardetalleventa(@v6,'978-0-128',1,3);

CALL sp_insertarventa(2000100070101,3,@v7);
CALL sp_agregardetalleventa(@v7,'978-0-129',2,3);

CALL sp_insertarventa(2000100080101,3,@v8);
CALL sp_agregardetalleventa(@v8,'978-0-130',3,3);

CALL sp_insertarventa(2000100090101,3,@v9);
CALL sp_agregardetalleventa(@v9,'978-0-131',1,3);

CALL sp_insertarventa(2000100100101,3,@v10);
CALL sp_agregardetalleventa(@v10,'978-0-132',2,3);

CALL sp_insertarventa(2000100110101,3,@v11);
CALL sp_agregardetalleventa(@v11,'978-0-133',3,3);

CALL sp_insertarventa(2000100120101,3,@v12);
CALL sp_agregardetalleventa(@v12,'978-0-134',1,3);

CALL sp_insertarventa(2000100130101,3,@v13);
CALL sp_agregardetalleventa(@v13,'978-0-135',2,3);

CALL sp_insertarventa(2000100140101,3,@v14);
CALL sp_agregardetalleventa(@v14,'978-0-136',3,3);

CALL sp_insertarventa(2000100150101,3,@v15);
CALL sp_agregardetalleventa(@v15,'978-0-137',1,3);

CALL sp_insertarventa(2000100160101,3,@v16);
CALL sp_agregardetalleventa(@v16,'978-0-138',2,3);

CALL sp_insertarventa(2000100170101,3,@v17);
CALL sp_agregardetalleventa(@v17,'978-0-139',3,3);

CALL sp_insertarventa(2000100180101,3,@v18);
CALL sp_agregardetalleventa(@v18,'978-0-140',1,3);

CALL sp_insertarventa(2000100190101,3,@v19);
CALL sp_agregardetalleventa(@v19,'978-0-141',2,3);

CALL sp_insertarventa(2000100200101,3,@v20);
CALL sp_agregardetalleventa(@v20,'978-0-142',3,3);

-- =============================================================================
-- CONSULTAS DE PRUEBA
-- =============================================================================

SELECT * FROM vw_lista_categorias;
SELECT * FROM vw_lista_editoriales;
SELECT * FROM vw_lista_proveedores;
SELECT * FROM vw_lista_autores;
SELECT * FROM vw_lista_clientes;
SELECT * FROM vw_lista_usuarios;
SELECT * FROM vw_lista_libros;
SELECT * FROM vw_lista_autores_libro;
SELECT * FROM vw_lista_ventas;
SELECT * FROM vw_lista_detalle_venta;
SELECT * FROM vw_movimientos_inventario;

-- =============================================================================
-- VERIFICACIONES
-- =============================================================================

SELECT
    id,
    username,
    rol,
    activo
FROM usuarios;

SELECT
    isbn,
    titulo,
    stock_actual,
    stock_minimo
FROM libros;

SELECT
    id_movimiento,
    isbn,
    id_usuario,
    cantidad
FROM movimientos_inventario;

SELECT
    id_venta,
    id_usuario,
    subtotal,
    total,
    estado
FROM ventas;