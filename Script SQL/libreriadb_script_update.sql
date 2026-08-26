drop database if exists libreriadb_in4cm;
create database if not exists libreriadb_in4cm;
use libreriadb_in4cm;
-- 1. TABLAS BASE
create table categorias(
    id_categoria int primary key auto_increment,
    nombre_categoria varchar(100)
);

-- editoriales
create table editoriales (
    nit varchar(20) primary key,
    nombre_editorial varchar(100) not null,
    telefono_editorial varchar(15),
    direccion_editorial varchar(100)
);

-- proveedores 
create table proveedores (
    id_proveedor int primary key auto_increment,
    nombre_proveedor varchar(100) not null,
    telefono_proveedor varchar(15),
    direccion_proveedor varchar(100),
    correo_proveedor varchar(100)
);

-- autores
create table autores(
    id_autor int primary key auto_increment,
    nombre_autor varchar(100) not null,
    apellido_autor varchar(100) not null,
    nacionalidad varchar(100),
    biografia text
);

-- clientes
create table clientes(
    cui bigint primary key,
    nombre_cliente varchar(100),
    apellido_cliente varchar(100),
    correo_electronico varchar(100)
);

-- usuarios
create table usuarios (
    id int primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    rol enum('admin', 'bodega', 'cajero') not null,
    nombre varchar(100),
    apellido varchar(100),
    correo varchar(100),
    activo boolean not null default true,
    fecha_creacion timestamp default current_timestamp,
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp
);

-- libros 
create table libros(
    isbn varchar(20) primary key,
    titulo varchar(100) not null,
    fecha_publicacion date,
    precio decimal(8,2) not null,
    id_categoria int,
    nit_editorial varchar(20),
    stock_actual int not null default 0,
    stock_minimo int not null default 0,
    activo boolean not null default true,
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp
);

-- autores_libro
create table autores_libro(
    id_autor_libro int auto_increment primary key,
    id_autor int,
    isbn varchar(20)
);
-- 2. VENTAS 
create table ventas(
    id_venta int primary key auto_increment,
    fecha_venta timestamp default current_timestamp,
    subtotal decimal(10,2) not null default 0,
    descuento decimal(10,2) not null default 0,
    total decimal(10,2) not null default 0,
    estado enum('COMPLETADA', 'ANULADA', 'DEVUELTA') not null default 'COMPLETADA',
    cui_cliente bigint,
    id_usuario int not null,
    usuario_autoriza_descuento int,
    fecha_anulacion timestamp null,
    usuario_anulacion int,
    motivo_anulacion varchar(255)
);

create table detalle_venta(
    id_detalle int primary key auto_increment,
    id_venta int,
    isbn varchar(20),
    cantidad int not null,
    precio_unitario decimal(10,2) not null,
    subtotal decimal(10,2) not null
);
-- 3. MOVIMIENTOS_INVENTARIO
create table movimientos_inventario(
    id_movimiento int primary key auto_increment,
    isbn varchar(20) not null,
    tipo_movimiento enum('INGRESO', 'VENTA', 'MERMA', 'TRASLADO', 'DEVOLUCION', 'AJUSTE') not null,
    cantidad int not null,
    fecha_movimiento timestamp default current_timestamp,
    id_usuario int not null,
    observacion varchar(255)
);
-- 4. LLAVES FORÁNEAS
alter table autores_libro
add constraint fk_a_autor foreign key (id_autor) references autores(id_autor) on delete cascade,
add constraint fk_a_libro foreign key (isbn) references libros(isbn) on delete cascade;

alter table libros
add constraint fk_a_categorias foreign key (id_categoria) references categorias(id_categoria) on delete cascade,
add constraint fk_a_editoriales foreign key (nit_editorial) references editoriales(nit) on delete cascade;

alter table ventas
add constraint fk_v_cliente foreign key (cui_cliente) references clientes(cui) on delete set null,
add constraint fk_v_usuario foreign key (id_usuario) references usuarios(id),
add constraint fk_v_autoriza_descuento foreign key (usuario_autoriza_descuento) references usuarios(id),
add constraint fk_v_usuario_anulacion foreign key (usuario_anulacion) references usuarios(id);

alter table detalle_venta
add constraint fk_dv_venta foreign key (id_venta) references ventas(id_venta) on delete cascade,
add constraint fk_dv_libro foreign key (isbn) references libros(isbn) on delete cascade;

alter table movimientos_inventario
add constraint fk_mi_libro foreign key (isbn) references libros(isbn) on delete cascade,
add constraint fk_mi_usuario foreign key (id_usuario) references usuarios(id);

use libreriadb_in4cm;
-- 5. CRUD: CATEGORIAS
delimiter $$

create procedure sp_insertarcategoria(
    in _nombre_categoria varchar(100)
)
begin
    insert into categorias(nombre_categoria)
    values (_nombre_categoria);
end $$

create procedure sp_listarcategorias()
begin
    select id_categoria, nombre_categoria from categorias;
end $$

create procedure sp_buscarcategoria(
    in _id_categoria int
)
begin
    select id_categoria, nombre_categoria
    from categorias
    where id_categoria = _id_categoria;
end $$

create procedure sp_actualizarcategoria(
    in _id_categoria int,
    in _nombre_categoria varchar(100)
)
begin
    update categorias
    set nombre_categoria = _nombre_categoria
    where id_categoria = _id_categoria;
end $$

create procedure sp_eliminarcategoria(
    in _id_categoria int
)
begin
    delete from categorias where id_categoria = _id_categoria;
end $$

delimiter ;
-- 6. CRUD: EDITORIALES
delimiter $$

create procedure sp_insertareditorial(
    in _nit varchar(20),
    in _nombre_editorial varchar(100),
    in _telefono_editorial varchar(15),
    in _direccion_editorial varchar(100)
)
begin
    insert into editoriales(nit, nombre_editorial, telefono_editorial, direccion_editorial)
    values (_nit, _nombre_editorial, _telefono_editorial, _direccion_editorial);
end $$

create procedure sp_listareditoriales()
begin
    select nit, nombre_editorial, telefono_editorial, direccion_editorial from editoriales;
end $$

create procedure sp_buscareditorial(
    in _nit varchar(20)
)
begin
    select nit, nombre_editorial, telefono_editorial, direccion_editorial
    from editoriales
    where nit = _nit;
end $$

create procedure sp_actualizareditorial(
    in _nit varchar(20),
    in _nombre_editorial varchar(100),
    in _telefono_editorial varchar(15),
    in _direccion_editorial varchar(100)
)
begin
    update editoriales
    set nombre_editorial = _nombre_editorial,
        telefono_editorial = _telefono_editorial,
        direccion_editorial = _direccion_editorial
    where nit = _nit;
end $$

create procedure sp_eliminareditorial(
    in _nit varchar(20)
)
begin
    delete from editoriales where nit = _nit;
end $$

delimiter ;
-- 7. CRUD: PROVEEDORES (nuevo)
delimiter $$

create procedure sp_insertarproveedor(
    in _nombre_proveedor varchar(100),
    in _telefono_proveedor varchar(15),
    in _direccion_proveedor varchar(100),
    in _correo_proveedor varchar(100)
)
begin
    insert into proveedores(nombre_proveedor, telefono_proveedor, direccion_proveedor, correo_proveedor)
    values (_nombre_proveedor, _telefono_proveedor, _direccion_proveedor, _correo_proveedor);
end $$

create procedure sp_listarproveedores()
begin
    select id_proveedor, nombre_proveedor, telefono_proveedor, direccion_proveedor, correo_proveedor from proveedores;
end $$

create procedure sp_buscarproveedor(
    in _id_proveedor int
)
begin
    select id_proveedor, nombre_proveedor, telefono_proveedor, direccion_proveedor, correo_proveedor
    from proveedores
    where id_proveedor = _id_proveedor;
end $$

create procedure sp_actualizarproveedor(
    in _id_proveedor int,
    in _nombre_proveedor varchar(100),
    in _telefono_proveedor varchar(15),
    in _direccion_proveedor varchar(100),
    in _correo_proveedor varchar(100)
)
begin
    update proveedores
    set nombre_proveedor = _nombre_proveedor,
        telefono_proveedor = _telefono_proveedor,
        direccion_proveedor = _direccion_proveedor,
        correo_proveedor = _correo_proveedor
    where id_proveedor = _id_proveedor;
end $$

create procedure sp_eliminarproveedor(
    in _id_proveedor int
)
begin
    delete from proveedores where id_proveedor = _id_proveedor;
end $$

delimiter ;
-- 8. CRUD: AUTORES
delimiter $$

create procedure sp_insertarautor(
    in _nombre_autor varchar(100),
    in _apellido_autor varchar(100),
    in _nacionalidad varchar(100),
    in _biografia text
)
begin
    insert into autores(nombre_autor, apellido_autor, nacionalidad, biografia)
    values (_nombre_autor, _apellido_autor, _nacionalidad, _biografia);
end $$

create procedure sp_listarautores()
begin
    select id_autor, nombre_autor, apellido_autor, nacionalidad, biografia from autores;
end $$

create procedure sp_buscarautor(
    in _id_autor int
)
begin
    select id_autor, nombre_autor, apellido_autor, nacionalidad, biografia
    from autores
    where id_autor = _id_autor;
end $$

create procedure sp_actualizarautor(
    in _id_autor int,
    in _nombre_autor varchar(100),
    in _apellido_autor varchar(100),
    in _nacionalidad varchar(100),
    in _biografia text
)
begin
    update autores
    set nombre_autor = _nombre_autor,
        apellido_autor = _apellido_autor,
        nacionalidad = _nacionalidad,
        biografia = _biografia
    where id_autor = _id_autor;
end $$

create procedure sp_eliminarautor(
    in _id_autor int
)
begin
    delete from autores where id_autor = _id_autor;
end $$

delimiter ;
-- 9. CRUD: CLIENTES
delimiter $$

create procedure sp_insertarcliente(
    in _cui bigint,
    in _nombre_cliente varchar(100),
    in _apellido_cliente varchar(100),
    in _correo_electronico varchar(100)
)
begin
    insert into clientes(cui, nombre_cliente, apellido_cliente, correo_electronico)
    values (_cui, _nombre_cliente, _apellido_cliente, _correo_electronico);
end $$

create procedure sp_listarclientes()
begin
    select cui, nombre_cliente, apellido_cliente, correo_electronico from clientes;
end $$

create procedure sp_buscarcliente(
    in _cui bigint
)
begin
    select cui, nombre_cliente, apellido_cliente, correo_electronico
    from clientes
    where cui = _cui;
end $$

create procedure sp_actualizarcliente(
    in _cui bigint,
    in _nombre_cliente varchar(100),
    in _apellido_cliente varchar(100),
    in _correo_electronico varchar(100)
)
begin
    update clientes
    set nombre_cliente = _nombre_cliente,
        apellido_cliente = _apellido_cliente,
        correo_electronico = _correo_electronico
    where cui = _cui;
end $$

create procedure sp_eliminarcliente(
    in _cui bigint
)
begin
    delete from clientes where cui = _cui;
end $$

delimiter ;
-- 10. CRUD: USUARIOS
delimiter $$

create procedure sp_registrar_usuario(
    in _username varchar(50),
    in _password_hash varchar(255),
    in _rol enum('admin', 'bodega', 'cajero'),
    in _nombre varchar(100),
    in _apellido varchar(100),
    in _correo varchar(100)
)
begin
    insert into usuarios(username, password_hash, rol, nombre, apellido, correo)
    values (_username, _password_hash, _rol, _nombre, _apellido, _correo);
end $$

create procedure sp_iniciar_sesion(
    in _username varchar(50)
)
begin
    select id, username, password_hash, rol, activo
    from usuarios
    where username = _username;
end $$

create procedure sp_listarusuarios()
begin
    select id, username, rol, nombre, apellido, correo, activo, fecha_creacion from usuarios;
end $$

create procedure sp_cambiar_password(
    in _id int,
    in _password_actual_hash varchar(255),
    in _password_nuevo_hash varchar(255)
)
begin
    update usuarios
    set password_hash = _password_nuevo_hash,
        fecha_actualizacion = current_timestamp
    where id = _id and password_hash = _password_actual_hash;
end $$

create procedure sp_desactivarusuario(
    in _id int
)
begin
    update usuarios set activo = false where id = _id;
end $$

delimiter ;
-- 11. CRUD: LIBROS (con control de stock)
delimiter $$

create procedure sp_insertarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20),
    in _stock_actual int,
    in _stock_minimo int
)
begin
    insert into libros(isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial, stock_actual, stock_minimo)
    values (_isbn, _titulo, _fecha_publicacion, _precio, _id_categoria, _nit_editorial, _stock_actual, _stock_minimo);
end $$

create procedure sp_listarlibros()
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial, stock_actual, stock_minimo, activo
    from libros;
end $$

create procedure sp_buscarlibro(
    in _isbn varchar(20)
)
begin
    select isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial, stock_actual, stock_minimo, activo
    from libros
    where isbn = _isbn;
end $$

create procedure sp_actualizarlibro(
    in _isbn varchar(20),
    in _titulo varchar(100),
    in _fecha_publicacion date,
    in _precio decimal(8,2),
    in _id_categoria int,
    in _nit_editorial varchar(20)
)
begin
    update libros
    set titulo = _titulo,
        fecha_publicacion = _fecha_publicacion,
        precio = _precio,
        id_categoria = _id_categoria,
        nit_editorial = _nit_editorial
    where isbn = _isbn;
end $$

create procedure sp_libros_bajo_stock_minimo()
begin
    select isbn, titulo, stock_actual, stock_minimo
    from libros
    where stock_actual <= stock_minimo and activo = true;
end $$

create procedure sp_eliminarlibro(
    in _isbn varchar(20)
)
begin
    update libros set activo = false where isbn = _isbn;
end $$

delimiter ;
-- 12. CRUD: AUTORES_LIBRO (tabla intermedia)
delimiter $$

create procedure sp_insertarautorlibro(
    in _id_autor int,
    in _isbn varchar(20)
)
begin
    insert into autores_libro(id_autor, isbn)
    values (_id_autor, _isbn);
end $$

create procedure sp_listarautoreslibro()
begin
    select id_autor_libro, id_autor, isbn from autores_libro;
end $$

create procedure sp_buscarautorlibro(
    in _id_autor_libro int
)
begin
    select id_autor_libro, id_autor, isbn
    from autores_libro
    where id_autor_libro = _id_autor_libro;
end $$

create procedure sp_actualizarautorlibro(
    in _id_autor_libro int,
    in _id_autor int,
    in _isbn varchar(20)
)
begin
    update autores_libro
    set id_autor = _id_autor,
        isbn = _isbn
    where id_autor_libro = _id_autor_libro;
end $$

create procedure sp_eliminarautorlibro(
    in _id_autor_libro int
)
begin
    delete from autores_libro where id_autor_libro = _id_autor_libro;
end $$

delimiter ;
-- 13. MOVIMIENTOS_INVENTARIO 

delimiter $$

create procedure sp_registrar_movimiento_inventario(
    in _isbn varchar(20),
    in _tipo_movimiento enum('INGRESO', 'VENTA', 'MERMA', 'TRASLADO', 'DEVOLUCION', 'AJUSTE'),
    in _cantidad int,
    in _id_usuario int,
    in _observacion varchar(255)
)
begin
    declare _delta int;

    -- INGRESO/DEVOLUCION/AJUSTE positivo suman stock; VENTA/MERMA/TRASLADO restan
    if _tipo_movimiento in ('INGRESO', 'DEVOLUCION') then
        set _delta = _cantidad;
    else
        set _delta = -_cantidad;
    end if;

    insert into movimientos_inventario(isbn, tipo_movimiento, cantidad, id_usuario, observacion)
    values (_isbn, _tipo_movimiento, _cantidad, _id_usuario, _observacion);

    update libros
    set stock_actual = stock_actual + _delta
    where isbn = _isbn;
end $$

create procedure sp_listarmovimientosinventario()
begin
    select id_movimiento, isbn, tipo_movimiento, cantidad, fecha_movimiento, id_usuario, observacion
    from movimientos_inventario
    order by fecha_movimiento desc;
end $$

create procedure sp_movimientos_por_libro(
    in _isbn varchar(20)
)
begin
    select id_movimiento, tipo_movimiento, cantidad, fecha_movimiento, id_usuario, observacion
    from movimientos_inventario
    where isbn = _isbn
    order by fecha_movimiento desc;
end $$

delimiter ;
-- 14. VENTAS Y DETALLE_VENTA 
delimiter $$

create procedure sp_insertarventa(
    in _cui_cliente bigint,
    in _id_usuario int,
    out _id_venta int
)
begin
    insert into ventas(subtotal, descuento, total, estado, cui_cliente, id_usuario)
    values (0, 0, 0, 'COMPLETADA', _cui_cliente, _id_usuario);

    set _id_venta = last_insert_id();
end $$

create procedure sp_agregardetalleventa(
    in _id_venta int,
    in _isbn varchar(20),
    in _cantidad int,
    in _id_usuario int
)
begin
    declare _precio decimal(8,2);
    declare _subtotal_linea decimal(10,2);

    select precio into _precio from libros where isbn = _isbn;
    set _subtotal_linea = _precio * _cantidad;

    insert into detalle_venta(id_venta, isbn, cantidad, precio_unitario, subtotal)
    values (_id_venta, _isbn, _cantidad, _precio, _subtotal_linea);

    call sp_registrar_movimiento_inventario(_isbn, 'VENTA', _cantidad, _id_usuario, concat('Venta #', _id_venta));

    update ventas v
    join (select sum(subtotal) as total_sub from detalle_venta where id_venta = _id_venta) d
    set v.subtotal = d.total_sub,
        v.total = d.total_sub - v.descuento
    where v.id_venta = _id_venta;
end $$

create procedure sp_aplicardescuentoventa(
    in _id_venta int,
    in _descuento decimal(10,2),
    in _usuario_autoriza int
)
begin
    update ventas
    set descuento = _descuento,
        total = subtotal - _descuento,
        usuario_autoriza_descuento = _usuario_autoriza
    where id_venta = _id_venta;
end $$

create procedure sp_listarventas()
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, cui_cliente, id_usuario
    from ventas;
end $$

create procedure sp_buscarventa(
    in _id_venta int
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado, cui_cliente, id_usuario
    from ventas
    where id_venta = _id_venta;
end $$

create procedure sp_resumenventasdelcajero(
    in _id_usuario int,
    in _fecha date
)
begin
    select id_venta, fecha_venta, subtotal, descuento, total, estado
    from ventas
    where id_usuario = _id_usuario
      and date(fecha_venta) = _fecha
      and estado = 'COMPLETADA';
end $$

create procedure sp_listardetalleventa(
    in _id_venta int
)
begin
    select id_detalle, id_venta, isbn, cantidad, precio_unitario, subtotal
    from detalle_venta
    where id_venta = _id_venta;
end $$

create procedure sp_anularventa(
    in _id_venta int,
    in _usuario_anulacion int,
    in _motivo varchar(255)
)
begin
    declare done int default 0;
    declare v_isbn varchar(20);
    declare v_cantidad int;
    declare cur cursor for
        select isbn, cantidad from detalle_venta where id_venta = _id_venta;
    declare continue handler for not found set done = 1;

    open cur;
    read_loop: loop
        fetch cur into v_isbn, v_cantidad;
        if done = 1 then
            leave read_loop;
        end if;
        call sp_registrar_movimiento_inventario(v_isbn, 'DEVOLUCION', v_cantidad, _usuario_anulacion, concat('Anulación venta #', _id_venta));
    end loop;
    close cur;

    update ventas
    set estado = 'ANULADA',
        fecha_anulacion = current_timestamp,
        usuario_anulacion = _usuario_anulacion,
        motivo_anulacion = _motivo
    where id_venta = _id_venta;
end $$

delimiter ;
create or replace view vw_lista_categorias as
select
    id_categoria as 'id categoría',
    nombre_categoria as 'categoría'
from categorias;

create or replace view vw_lista_editoriales as
select
    nit as 'nit editorial',
    nombre_editorial as 'editorial',
    telefono_editorial as 'teléfono',
    direccion_editorial as 'dirección'
from editoriales;

create or replace view vw_lista_proveedores as
select
    id_proveedor as 'id proveedor',
    nombre_proveedor as 'proveedor',
    telefono_proveedor as 'teléfono',
    direccion_proveedor as 'dirección',
    correo_proveedor as 'correo'
from proveedores;

create or replace view vw_lista_autores as
select
    id_autor as 'id autor',
    concat(nombre_autor, ' ', apellido_autor) as 'autor',
    nacionalidad as 'nacionalidad',
    biografia as 'biografía'
from autores;

create or replace view vw_lista_clientes as
select
    cui as 'cui cliente',
    concat(nombre_cliente, ' ', apellido_cliente) as 'cliente',
    correo_electronico as 'correo electrónico'
from clientes;

create or replace view vw_lista_usuarios as
select
    id as 'id usuario',
    username as 'usuario',
    rol as 'rol',
    concat(nombre, ' ', apellido) as 'nombre completo',
    correo as 'correo',
    activo as 'activo'
from usuarios;

create or replace view vw_lista_libros as
select
    l.isbn as 'isbn',
    l.titulo as 'título',
    l.fecha_publicacion as 'fecha de publicación',
    l.precio as 'precio',
    c.nombre_categoria as 'categoría',
    e.nombre_editorial as 'editorial',
    l.stock_actual as 'stock actual',
    l.stock_minimo as 'stock mínimo',
    l.activo as 'activo'
from libros l
inner join categorias c on l.id_categoria = c.id_categoria
inner join editoriales e on l.nit_editorial = e.nit;

create or replace view vw_libros_bajo_stock as
select
    isbn as 'isbn',
    titulo as 'título',
    stock_actual as 'stock actual',
    stock_minimo as 'stock mínimo'
from libros
where stock_actual <= stock_minimo and activo = true;

create or replace view vw_lista_autores_libro as
select
    al.id_autor_libro as 'id relación',
    concat(a.nombre_autor, ' ', a.apellido_autor) as 'autor',
    l.titulo as 'título del libro',
    l.isbn as 'isbn'
from autores_libro al
inner join autores a on al.id_autor = a.id_autor
inner join libros l on al.isbn = l.isbn;

create or replace view vw_lista_ventas as
select
    v.id_venta as 'no. venta',
    v.fecha_venta as 'fecha/hora',
    v.subtotal as 'subtotal',
    v.descuento as 'descuento',
    v.total as 'total',
    v.estado as 'estado',
    v.cui_cliente as 'cui cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'cliente',
    u.username as 'cajero'
from ventas v
left join clientes cl on v.cui_cliente = cl.cui
inner join usuarios u on v.id_usuario = u.id;

create or replace view vw_lista_detalle_venta as
select
    dv.id_detalle as 'id detalle',
    dv.id_venta as 'no. venta',
    l.titulo as 'libro',
    l.isbn as 'isbn',
    dv.cantidad as 'cantidad',
    dv.precio_unitario as 'precio unitario',
    dv.subtotal as 'subtotal'
from detalle_venta dv
inner join libros l on dv.isbn = l.isbn;

-- factura de venta: encabezado + cliente + desglose de libros
create or replace view vw_factura_ventas as
select
    v.id_venta as 'numero_factura',
    v.fecha_venta as 'fecha_emision',
    cl.cui as 'cui_cliente',
    concat(cl.nombre_cliente, ' ', cl.apellido_cliente) as 'nombre_cliente',
    cl.correo_electronico as 'correo_cliente',
    l.isbn as 'isbn_libro',
    l.titulo as 'descripcion_libro',
    dv.cantidad as 'cantidad',
    dv.precio_unitario as 'precio_unitario',
    dv.subtotal as 'subtotal_linea',
    v.descuento as 'descuento',
    v.total as 'gran_total',
    u.username as 'atendido_por'
from ventas v
left join clientes cl on v.cui_cliente = cl.cui
inner join detalle_venta dv on v.id_venta = dv.id_venta
inner join libros l on dv.isbn = l.isbn
inner join usuarios u on v.id_usuario = u.id;

create or replace view vw_movimientos_inventario as
select
    mi.id_movimiento as 'id movimiento',
    l.titulo as 'libro',
    mi.isbn as 'isbn',
    mi.tipo_movimiento as 'tipo',
    mi.cantidad as 'cantidad',
    mi.fecha_movimiento as 'fecha',
    u.username as 'usuario',
    mi.observacion as 'observación'
from movimientos_inventario mi
inner join libros l on mi.isbn = l.isbn
inner join usuarios u on mi.id_usuario = u.id;

-- =============================================================================
-- 1. CATEGORIAS
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
-- 2. EDITORIALES
-- =============================================================================
CALL sp_insertareditorial('1001-A', 'Editorial Planeta', '22334455', 'Zona 1, Ciudad');
CALL sp_insertareditorial('1002-B', 'Penguin Random House', '22334456', 'Zona 10, Ciudad');
CALL sp_insertareditorial('1003-C', 'Editorial Santillana', '22334457', 'Zona 9, Ciudad');
CALL sp_insertareditorial('1004-D', 'Ediciones Salamandra', '22334458', 'Zona 14, Ciudad');
CALL sp_insertareditorial('1005-E', 'Anagrama', '22334459', 'Zona 4, Ciudad');
CALL sp_insertareditorial('1006-F', 'Alfaguara', '22334460', 'Zona 15, Ciudad');
CALL sp_insertareditorial('1007-G', 'Seix Barral', '22334461', 'Zona 1, Ciudad');
CALL sp_insertareditorial('1008-H', 'Tusquets Editores', '22334462', 'Zona 2, Ciudad');
CALL sp_insertareditorial('1009-I', 'Lumen', '22334463', 'Zona 11, Ciudad');
CALL sp_insertareditorial('1010-J', 'Debolsillo', '22334464', 'Zona 12, Ciudad');
CALL sp_insertareditorial('1011-K', 'Ediciones B', '22334465', 'Zona 13, Ciudad');
CALL sp_insertareditorial('1012-L', 'Roca Editorial', '22334466', 'Zona 16, Ciudad');
CALL sp_insertareditorial('1013-M', 'Ediciones Minotauro', '22334467', 'Zona 5, Ciudad');
CALL sp_insertareditorial('1014-N', 'Suma de Letras', '22334468', 'Zona 6, Ciudad');
CALL sp_insertareditorial('1015-O', 'Plaza & Janés', '22334469', 'Zona 7, Ciudad');
CALL sp_insertareditorial('1016-P', 'Editorial Siruela', '22334470', 'Zona 8, Ciudad');
CALL sp_insertareditorial('1017-Q', 'Ediciones Destino', '22334471', 'Zona 18, Ciudad');
CALL sp_insertareditorial('1018-R', 'Acantilado', '22334472', 'Zona 21, Ciudad');
CALL sp_insertareditorial('1019-S', 'Editorial Piedra Santa', '22334473', 'Zona 1, Ciudad');
CALL sp_insertareditorial('1020-T', 'Fondo de Cultura Económica', '22334474', 'Zona 9, Ciudad');

-- =============================================================================
-- 3. PROVEEDORES
-- =============================================================================
CALL sp_insertarproveedor('Distribuidora Nacional de Libros', '23001001', 'Zona 1, Ciudad', 'contacto@dnl.com');
CALL sp_insertarproveedor('Importadora Cultural S.A.', '23001002', 'Zona 4, Ciudad', 'ventas@importcultural.com');
CALL sp_insertarproveedor('Papelera San Miguel', '23001003', 'Zona 11, Ciudad', 'info@papelerasanmiguel.com');
CALL sp_insertarproveedor('Suministros Editoriales GT', '23001004', 'Zona 7, Ciudad', 'contacto@sumeditorialesgt.com');
CALL sp_insertarproveedor('Distribuciones Quetzal', '23001005', 'Zona 9, Ciudad', 'ventas@distquetzal.com');
CALL sp_insertarproveedor('Importaciones Libro Mundo', '23001006', 'Zona 2, Ciudad', 'info@libromundo.com');
CALL sp_insertarproveedor('Grupo Distribuidor Maya', '23001007', 'Zona 15, Ciudad', 'contacto@grupomaya.com');
CALL sp_insertarproveedor('Comercializadora Andina', '23001008', 'Zona 13, Ciudad', 'ventas@comercialandina.com');
CALL sp_insertarproveedor('Distribuidora Continental de Libros', '23001009', 'Zona 6, Ciudad', 'info@dcl.com');
CALL sp_insertarproveedor('Suministros y Papel S.A.', '23001010', 'Zona 16, Ciudad', 'contacto@sumypapel.com');
CALL sp_insertarproveedor('Importadora del Atlántico', '23001011', 'Zona 21, Ciudad', 'ventas@importatlantico.com');
CALL sp_insertarproveedor('Distribuidora Pacífico Libros', '23001012', 'Zona 8, Ciudad', 'info@pacificolibros.com');
CALL sp_insertarproveedor('Grupo Editorial Insumos', '23001013', 'Zona 12, Ciudad', 'contacto@geinsumos.com');
CALL sp_insertarproveedor('Papel y Tinta S.A.', '23001014', 'Zona 5, Ciudad', 'ventas@papelytinta.com');
CALL sp_insertarproveedor('Distribuidora Metropolitana', '23001015', 'Zona 10, Ciudad', 'info@distmetropolitana.com');
CALL sp_insertarproveedor('Importaciones Culturales Unidas', '23001016', 'Zona 14, Ciudad', 'contacto@icunidas.com');
CALL sp_insertarproveedor('Suministros Gráficos GT', '23001017', 'Zona 18, Ciudad', 'ventas@sumgraficosgt.com');
CALL sp_insertarproveedor('Distribuidora Central de Libros', '23001018', 'Zona 3, Ciudad', 'info@dcentrallibros.com');
CALL sp_insertarproveedor('Comercial Andina de Papel', '23001019', 'Zona 17, Ciudad', 'contacto@candinapapel.com');
CALL sp_insertarproveedor('Grupo Logístico Editorial', '23001020', 'Zona 19, Ciudad', 'ventas@glogisticoeditorial.com');

-- =============================================================================
-- 4. AUTORES (20)
-- =============================================================================
CALL sp_insertarautor('Gabriel', 'García Márquez', 'Colombiana', 'Premio Nobel de Literatura 1982. Exponente del realismo mágico.');
CALL sp_insertarautor('Julio', 'Cortázar', 'Argentina', 'Maestro del relato corto y creador de Rayuela.');
CALL sp_insertarautor('Isabel', 'Allende', 'Chilena', 'Autora de La Casa de los Espíritus. Gran exponente latinoamericana.');
CALL sp_insertarautor('Jorge Luis', 'Borges', 'Argentina', 'Escritor de ficciones, poemas y ensayos aclamado mundialmente.');
CALL sp_insertarautor('Miguel', 'Ángel Asturias', 'Guatemalteca', 'Premio Nobel de Literatura 1967. Autor de El Señor Presidente.');
CALL sp_insertarautor('J.K.', 'Rowling', 'Británica', 'Creadora del famoso mundo mágico de Harry Potter.');
CALL sp_insertarautor('George R.R.', 'Martin', 'Estadounidense', 'Autor de la saga Canción de Hielo y Fuego.');
CALL sp_insertarautor('Stephen', 'King', 'Estadounidense', 'El maestro contemporáneo del terror y el suspenso.');
CALL sp_insertarautor('Haruki', 'Murakami', 'Japonesa', 'Autor de Tokio Blues, conocido por su surrealismo melancólico.');
CALL sp_insertarautor('Jane', 'Austen', 'Británica', 'Autora clásica conocida por Orgullo y Prejuicio.');
CALL sp_insertarautor('Edgar Allan', 'Poe', 'Estadounidense', 'Padre del cuento de terror y precursor de la novela policíaca.');
CALL sp_insertarautor('Agatha', 'Christie', 'Británica', 'Reina del misterio y creadora de Hércules Poirot.');
CALL sp_insertarautor('Isaac', 'Asimov', 'Rusa/Estadounidense', 'Uno de los grandes maestros de la ciencia ficción.');
CALL sp_insertarautor('J.R.R.', 'Tolkien', 'Británica', 'Creador de la Tierra Media, El Hobbit y El Señor de los Anillos.');
CALL sp_insertarautor('Virginia', 'Woolf', 'Británica', 'Figura destacada del modernismo literario del siglo XX.');
CALL sp_insertarautor('Fiódor', 'Dostoievski', 'Rusa', 'Autor de Crimen y Castigo, maestro en psicología humana.');
CALL sp_insertarautor('Franz', 'Kafka', 'Checa', 'Conocido por obras existencialistas como La Metamorfosis.');
CALL sp_insertarautor('Oscar', 'Wilde', 'Irlandesa', 'Dramaturgo y autor de El Retrato de Dorian Gray.');
CALL sp_insertarautor('Mario', 'Vargas Llosa', 'Peruana', 'Premio Nobel de Literatura 2010.');
CALL sp_insertarautor('Margaret', 'Atwood', 'Canadiense', 'Autora de El cuento de la criada, fuerte exponente distópica.');


-- =============================================================================
-- 5. CLIENTES
-- =============================================================================
CALL sp_insertarcliente(2000100010101, 'Ana', 'López', 'ana.l@gmail.com');
CALL sp_insertarcliente(2000100020101, 'Carlos', 'Méndez', 'cmendez@yahoo.com');
CALL sp_insertarcliente(2000100030101, 'Luis', 'Pérez', 'lperez@hotmail.com');
CALL sp_insertarcliente(2000100040101, 'María', 'García', 'mgarcia@gmail.com');
CALL sp_insertarcliente(2000100050101, 'Jorge', 'Castillo', 'jcastillo@gmail.com');
CALL sp_insertarcliente(2000100060101, 'Lucía', 'Fernández', 'lfernandez@yahoo.com');
CALL sp_insertarcliente(2000100070101, 'Mario', 'Gómez', 'mgomez@gmail.com');
CALL sp_insertarcliente(2000100080101, 'Elena', 'Morales', 'emorales@hotmail.com');
CALL sp_insertarcliente(2000100090101, 'Pedro', 'Ramírez', 'pramirez@gmail.com');
CALL sp_insertarcliente(2000100100101, 'Sofía', 'Vásquez', 'svasquez@gmail.com');
CALL sp_insertarcliente(2000100110101, 'Diego', 'Hernández', 'dhernandez@yahoo.com');
CALL sp_insertarcliente(2000100120101, 'Camila', 'Cruz', 'ccruz@hotmail.com');
CALL sp_insertarcliente(2000100130101, 'Andrés', 'Reyes', 'areyes@gmail.com');
CALL sp_insertarcliente(2000100140101, 'Valeria', 'Ortiz', 'vortiz@gmail.com');
CALL sp_insertarcliente(2000100150101, 'Javier', 'Flores', 'jflores@yahoo.com');
CALL sp_insertarcliente(2000100160101, 'Daniela', 'Díaz', 'ddiaz@gmail.com');
CALL sp_insertarcliente(2000100170101, 'Ricardo', 'Alonso', 'ralonso@hotmail.com');
CALL sp_insertarcliente(2000100180101, 'Gabriela', 'Rojas', 'grojas@gmail.com');
CALL sp_insertarcliente(2000100190101, 'Héctor', 'Salazar', 'hsalazar@yahoo.com');
CALL sp_insertarcliente(2000100200101, 'Mónica', 'Herrera', 'mherrera@gmail.com');

-- =============================================================================
-- 6. USUARIOS 
-- =============================================================================
CALL sp_registrar_usuario('admin1', SHA2('Admin#2026',256), 'admin', 'Sofía', 'Reyes', 'sofia.reyes@libreria.com');
CALL sp_registrar_usuario('admin2', SHA2('Admin#2026',256), 'admin', 'Diego', 'Morales', 'diego.morales@libreria.com');
CALL sp_registrar_usuario('admin3', SHA2('Admin#2026',256), 'admin', 'Carmen', 'López', 'carmen.lopez@libreria.com');
CALL sp_registrar_usuario('bodega1', SHA2('Bodega#2026',256), 'bodega', 'Luis', 'Ramírez', 'luis.ramirez@libreria.com');
CALL sp_registrar_usuario('bodega2', SHA2('Bodega#2026',256), 'bodega', 'Marta', 'González', 'marta.gonzalez@libreria.com');
CALL sp_registrar_usuario('bodega3', SHA2('Bodega#2026',256), 'bodega', 'Pedro', 'Sánchez', 'pedro.sanchez@libreria.com');
CALL sp_registrar_usuario('bodega4', SHA2('Bodega#2026',256), 'bodega', 'Ana', 'Torres', 'ana.torres@libreria.com');
CALL sp_registrar_usuario('bodega5', SHA2('Bodega#2026',256), 'bodega', 'Jorge', 'Castillo', 'jorge.castillo@libreria.com');
CALL sp_registrar_usuario('bodega6', SHA2('Bodega#2026',256), 'bodega', 'Elena', 'Vargas', 'elena.vargas@libreria.com');
CALL sp_registrar_usuario('bodega7', SHA2('Bodega#2026',256), 'bodega', 'Ricardo', 'Méndez', 'ricardo.mendez@libreria.com');
CALL sp_registrar_usuario('cajero1', SHA2('Cajero#2026',256), 'cajero', 'Paola', 'Cruz', 'paola.cruz@libreria.com');
CALL sp_registrar_usuario('cajero2', SHA2('Cajero#2026',256), 'cajero', 'Fernando', 'Ortiz', 'fernando.ortiz@libreria.com');
CALL sp_registrar_usuario('cajero3', SHA2('Cajero#2026',256), 'cajero', 'Karla', 'Díaz', 'karla.diaz@libreria.com');
CALL sp_registrar_usuario('cajero4', SHA2('Cajero#2026',256), 'cajero', 'Hugo', 'Palacios', 'hugo.palacios@libreria.com');
CALL sp_registrar_usuario('cajero5', SHA2('Cajero#2026',256), 'cajero', 'Silvia', 'Guerra', 'silvia.guerra@libreria.com');
CALL sp_registrar_usuario('cajero6', SHA2('Cajero#2026',256), 'cajero', 'Mauricio', 'Cardona', 'mauricio.cardona@libreria.com');
CALL sp_registrar_usuario('cajero7', SHA2('Cajero#2026',256), 'cajero', 'Vivian', 'Solares', 'vivian.solares@libreria.com');
CALL sp_registrar_usuario('cajero8', SHA2('Cajero#2026',256), 'cajero', 'Christian', 'Orellana', 'christian.orellana@libreria.com');
CALL sp_registrar_usuario('cajero9', SHA2('Cajero#2026',256), 'cajero', 'Natalia', 'Sandoval', 'natalia.sandoval@libreria.com');
CALL sp_registrar_usuario('cajero10', SHA2('Cajero#2026',256), 'cajero', 'Pablo', 'Archila', 'pablo.archila@libreria.com');


-- =============================================================================
-- 7. LIBROS
-- =============================================================================
CALL sp_insertarlibro('978-0-123', 'Cien Años de Soledad', '1967-05-30', 150.00, 1, '1001-A', 0, 10);
CALL sp_insertarlibro('978-0-124', 'Rayuela', '1963-06-28', 135.50, 1, '1002-B', 0, 8);
CALL sp_insertarlibro('978-0-125', 'El Señor Presidente', '1946-01-01', 120.00, 1, '1019-S', 0, 10);
CALL sp_insertarlibro('978-0-126', 'Harry Potter y la Piedra Filosofal', '1997-06-26', 180.00, 2, '1004-D', 0, 15);
CALL sp_insertarlibro('978-0-127', 'El Resplandor', '1977-01-28', 165.00, 10, '1005-E', 0, 5);
CALL sp_insertarlibro('978-0-128', 'Fundación', '1951-05-01', 140.00, 3, '1013-M', 0, 10);
CALL sp_insertarlibro('978-0-129', 'El Señor de los Anillos', '1954-07-29', 250.00, 2, '1013-M', 0, 12);
CALL sp_insertarlibro('978-0-130', 'Crimen y Castigo', '1866-01-01', 95.00, 1, '1007-G', 0, 6);
CALL sp_insertarlibro('978-0-131', 'Diez Negritos', '1939-11-06', 110.00, 5, '1008-H', 0, 8);
CALL sp_insertarlibro('978-0-132', 'Orgullo y Prejuicio', '1813-01-28', 85.00, 9, '1009-I', 0, 10);
CALL sp_insertarlibro('978-0-133', 'La Casa de los Espíritus', '1982-01-01', 145.00, 1, '1001-A', 0, 8);
CALL sp_insertarlibro('978-0-134', 'El Cuento de la Criada', '1985-01-01', 160.00, 3, '1004-D', 0, 5);
CALL sp_insertarlibro('978-0-135', 'La Metamorfosis', '1915-01-01', 90.00, 10, '1006-F', 0, 5);
CALL sp_insertarlibro('978-0-136', 'Tokio Blues (Norwegian Wood)', '1987-08-04', 140.00, 9, '1009-I', 0, 6);
CALL sp_insertarlibro('978-0-137', 'Narraciones Extraordinarias', '1845-01-01', 95.00, 10, '1016-P', 0, 5);
CALL sp_insertarlibro('978-0-138', 'El Retrato de Dorian Gray', '1890-07-01', 100.00, 14, '1018-R', 0, 5);
CALL sp_insertarlibro('978-0-139', 'Mrs. Dalloway', '1925-05-14', 110.00, 1, '1005-E', 0, 4);
CALL sp_insertarlibro('978-0-140', 'La Ciudad y los Perros', '1963-01-01', 130.00, 4, '1015-O', 0, 6);
CALL sp_insertarlibro('978-0-141', 'Juego de Tronos', '1996-08-01', 200.00, 2, '1011-K', 0, 10);
CALL sp_insertarlibro('978-0-142', 'Los Hermanos Karamazov', '1880-11-01', 190.00, 1, '1007-G', 0, 5);

-- =============================================================================
-- 8. AUTORES_LIBRO
-- =============================================================================
CALL sp_insertarautorlibro(1, '978-0-123');   -- García Márquez
CALL sp_insertarautorlibro(2, '978-0-124');   -- Cortázar
CALL sp_insertarautorlibro(5, '978-0-125');   -- Asturias
CALL sp_insertarautorlibro(6, '978-0-126');   -- Rowling
CALL sp_insertarautorlibro(8, '978-0-127');   -- King
CALL sp_insertarautorlibro(13, '978-0-128');  -- Asimov
CALL sp_insertarautorlibro(14, '978-0-129');  -- Tolkien
CALL sp_insertarautorlibro(16, '978-0-130');  -- Dostoievski
CALL sp_insertarautorlibro(12, '978-0-131');  -- Christie
CALL sp_insertarautorlibro(10, '978-0-132');  -- Austen
CALL sp_insertarautorlibro(3, '978-0-133');   -- Allende
CALL sp_insertarautorlibro(20, '978-0-134');  -- Atwood
CALL sp_insertarautorlibro(17, '978-0-135');  -- Kafka
CALL sp_insertarautorlibro(9, '978-0-136');   -- Murakami
CALL sp_insertarautorlibro(11, '978-0-137');  -- Poe
CALL sp_insertarautorlibro(18, '978-0-138');  -- Wilde
CALL sp_insertarautorlibro(15, '978-0-139');  -- Woolf
CALL sp_insertarautorlibro(19, '978-0-140');  -- Vargas Llosa
CALL sp_insertarautorlibro(7, '978-0-141');   -- Martin
CALL sp_insertarautorlibro(16, '978-0-142');  -- Dostoievski (2do título)

-- =============================================================================
-- 9. MOVIMIENTOS_INVENTARIO 
-- =============================================================================
CALL sp_registrar_movimiento_inventario('978-0-123', 'INGRESO', 40, 4,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-124', 'INGRESO', 25, 5,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-125', 'INGRESO', 30, 6,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-126', 'INGRESO', 50, 7,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-127', 'INGRESO', 20, 8,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-128', 'INGRESO', 35, 9,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-129', 'INGRESO', 45, 10, 'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-130', 'INGRESO', 22, 4,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-131', 'INGRESO', 28, 5,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-132', 'INGRESO', 33, 6,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-133', 'INGRESO', 26, 7,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-134', 'INGRESO', 18, 8,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-135', 'INGRESO', 20, 9,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-136', 'INGRESO', 24, 10, 'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-137', 'INGRESO', 20, 4,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-138', 'INGRESO', 18, 5,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-139', 'INGRESO', 15, 6,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-140', 'INGRESO', 22, 7,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-141', 'INGRESO', 35, 8,  'Carga inicial de stock');
CALL sp_registrar_movimiento_inventario('978-0-142', 'INGRESO', 15, 9,  'Carga inicial de stock');

-- =============================================================================
-- 10. VENTAS y DETALLE_VENTA 
-- =============================================================================
CALL sp_insertarventa(2000100010101, 11, @v1);  CALL sp_agregardetalleventa(@v1, '978-0-123', 2, 11);
CALL sp_insertarventa(2000100020101, 12, @v2);  CALL sp_agregardetalleventa(@v2, '978-0-124', 3, 12);
CALL sp_insertarventa(2000100030101, 13, @v3);  CALL sp_agregardetalleventa(@v3, '978-0-125', 1, 13);
CALL sp_insertarventa(2000100040101, 14, @v4);  CALL sp_agregardetalleventa(@v4, '978-0-126', 2, 14);
CALL sp_insertarventa(2000100050101, 15, @v5);  CALL sp_agregardetalleventa(@v5, '978-0-127', 3, 15);
CALL sp_insertarventa(2000100060101, 16, @v6);  CALL sp_agregardetalleventa(@v6, '978-0-128', 1, 16);
CALL sp_insertarventa(2000100070101, 17, @v7);  CALL sp_agregardetalleventa(@v7, '978-0-129', 2, 17);
CALL sp_insertarventa(2000100080101, 18, @v8);  CALL sp_agregardetalleventa(@v8, '978-0-130', 3, 18);
CALL sp_insertarventa(2000100090101, 19, @v9);  CALL sp_agregardetalleventa(@v9, '978-0-131', 1, 19);
CALL sp_insertarventa(2000100100101, 20, @v10); CALL sp_agregardetalleventa(@v10, '978-0-132', 2, 20);
CALL sp_insertarventa(2000100110101, 11, @v11); CALL sp_agregardetalleventa(@v11, '978-0-133', 3, 11);
CALL sp_insertarventa(2000100120101, 12, @v12); CALL sp_agregardetalleventa(@v12, '978-0-134', 1, 12);
CALL sp_insertarventa(2000100130101, 13, @v13); CALL sp_agregardetalleventa(@v13, '978-0-135', 2, 13);
CALL sp_insertarventa(2000100140101, 14, @v14); CALL sp_agregardetalleventa(@v14, '978-0-136', 3, 14);
CALL sp_insertarventa(2000100150101, 15, @v15); CALL sp_agregardetalleventa(@v15, '978-0-137', 1, 15);
CALL sp_insertarventa(2000100160101, 16, @v16); CALL sp_agregardetalleventa(@v16, '978-0-138', 2, 16);
CALL sp_insertarventa(2000100170101, 17, @v17); CALL sp_agregardetalleventa(@v17, '978-0-139', 3, 17);
CALL sp_insertarventa(2000100180101, 18, @v18); CALL sp_agregardetalleventa(@v18, '978-0-140', 1, 18);
CALL sp_insertarventa(2000100190101, 19, @v19); CALL sp_agregardetalleventa(@v19, '978-0-141', 2, 19);
CALL sp_insertarventa(2000100200101, 20, @v20); CALL sp_agregardetalleventa(@v20, '978-0-142', 3, 20);


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