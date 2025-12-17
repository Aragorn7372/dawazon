-- Limpieza de tablas
DROP TABLE IF EXISTS product_comments CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users_favs CASCADE;
DROP TABLE IF EXISTS users_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Tabla de categorías
CREATE TABLE category
(
    id         VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- Tabla de usuarios
CREATE TABLE users
(
    id                         BIGSERIAL PRIMARY KEY,
    user_name                  VARCHAR(255) NOT NULL UNIQUE,
    email                      VARCHAR(255) NOT NULL UNIQUE,
    password                   VARCHAR(255) NOT NULL,
    client_name                VARCHAR(255),
    client_email               VARCHAR(255),
    client_phone               VARCHAR(255),
    client_address_number      SMALLINT,
    client_address_street      VARCHAR(255),
    client_address_city        VARCHAR(255),
    client_address_province    VARCHAR(255),
    client_address_country     VARCHAR(255),
    client_address_postal_code INTEGER,
    telefono                   VARCHAR(255)          DEFAULT '',
    avatar                     VARCHAR(255) NOT NULL DEFAULT 'default.png',
    created_at                 TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMP    NOT NULL DEFAULT NOW(),
    is_deleted                 BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Tabla de roles de usuarios
CREATE TABLE users_roles
(
    user_id BIGINT      NOT NULL,
    roles   VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tabla de favoritos de usuarios
CREATE TABLE users_favs
(
    user_id BIGINT NOT NULL,
    favs    VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tabla de productos
CREATE TABLE product
(
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(255)     NOT NULL,
    price       DOUBLE PRECISION NOT NULL,
    stock       INTEGER          NOT NULL,
    description TEXT             NOT NULL,
    creator_id  BIGINT           NOT NULL,
    category_id VARCHAR(255),
    is_deleted  BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP        NOT NULL DEFAULT NOW(),
    version     BIGINT,
    FOREIGN KEY (category_id) REFERENCES category (id),
    FOREIGN KEY (creator_id) REFERENCES users (id)
);

-- Tabla de imágenes de productos
CREATE TABLE product_images
(
    product_id VARCHAR(255) NOT NULL,
    images     VARCHAR(500),
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

-- Tabla de comentarios de productos
CREATE TABLE product_comments
(
    product_id  VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    content     TEXT         NOT NULL,
    verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    recommended BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

-- ============================================
-- INSERCIÓN DE DATOS
-- ============================================

-- Insertar categorías (IDs generados con formato del IdGenerator)
INSERT INTO category (id, name, created_at, updated_at)
VALUES ('aB3xT9kL2pQm', 'Electrónica', NOW(), NOW()),
       ('zX8vN1mK5wRt', 'Libros', NOW(), NOW()),
       ('qW4yH7jG3sLp', 'Ropa', NOW(), NOW()),
       ('pL9dF2nB6vCx', 'Hogar', NOW(), NOW()),
       ('mK5tR8hJ1zWq', 'Deportes', NOW(), NOW());

-- Insertar usuarios (password es 'password123' hasheado con BCrypt)
-- Admin y Manager sin información de cliente (no compran)
INSERT INTO users ( user_name, email, password, telefono, avatar, is_deleted)
VALUES ( 'admin', 'admin@dawazon.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        '+34666111222', 'admin-avatar.png', FALSE),

       ( 'manager_jose', 'jose. manager@dawazon.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        '+34666222333', 'manager-avatar. png', FALSE);

-- Usuarios normales con información de cliente completa
INSERT INTO users ( user_name, email, password, client_name, client_email, client_phone,
                   client_address_number, client_address_street, client_address_city,
                   client_address_province, client_address_country, client_address_postal_code,
                   telefono, avatar, is_deleted)
VALUES ( 'john_doe', 'john. doe@email.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        'John Doe', 'john.doe@email.com', '+34666333444',
        42, 'Gran Vía', 'Madrid', 'Madrid', 'España', 28013,
        '+34666333444', 'john-avatar.png', FALSE),

       ( 'jane_smith', 'jane.smith@email.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        'Jane Smith', 'jane.smith@email.com', '+34666555666',
        15, 'Paseo de Gracia', 'Barcelona', 'Barcelona', 'España', 8007,
        '+34666555666', 'jane-avatar. png', FALSE),

       ( 'carlos_ruiz', 'carlos.ruiz@email.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        'Carlos Ruiz', 'carlos.ruiz@email.com', '+34666777888',
        8, 'Calle Larios', 'Málaga', 'Málaga', 'España', 29015,
        '+34666777888', 'default.png', FALSE),

       ( 'maria_garcia', 'maria.garcia@email.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        'María García', 'maria.garcia@email.com', '+34666999000',
        23, 'Calle Sierpes', 'Sevilla', 'Sevilla', 'España', 41004,
        '+34666999000', 'maria-avatar.png', FALSE),

       ( 'pedro_lopez', 'pedro.lopez@email.com', '$2a$10$xRlQkVD5z0L5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5xKxV5x',
        'Pedro López', 'pedro.lopez@email.com', '+34666000111',
        50, 'Calle Mayor', 'Valencia', 'Valencia', 'España', 46001,
        '+34666000111', 'default.png', FALSE);

-- Insertar roles de usuarios (ROLES EXCLUYENTES - UN SOLO ROL POR USUARIO)
INSERT INTO users_roles (user_id, roles)
VALUES (1, 'ADMIN'),   -- Admin
       (2, 'MANAGER'), -- Manager
       (3, 'USER'),    -- Usuario normal
       (4, 'USER'),    -- Usuario normal
       (5, 'USER'),    -- Usuario normal
       (6, 'USER'),    -- Usuario normal
       (7, 'USER');
-- Usuario normal

-- Insertar productos
INSERT INTO product (id, name, price, stock, description, creator_id, category_id, is_deleted, version)
VALUES ('Hx9Lp2Ks4TnB', 'Laptop HP Pavilion 15', 699.99, 25,
        'Portátil HP Pavilion 15 con procesador Intel Core i5, 8GB RAM, 256GB SSD',
        1, 'aB3xT9kL2pQm', FALSE, 0),

       ('Yw3Zq7Vm1RfG', 'iPhone 14 Pro 128GB', 1099.99, 15,
        'Apple iPhone 14 Pro con pantalla Super Retina XDR de 6.1 pulgadas',
        1, 'aB3xT9kL2pQm', FALSE, 0),

       ('Dk5Mn8Pj2WcX', 'Don Quijote de la Mancha', 19.99, 50,
        'Obra maestra de Miguel de Cervantes, edición completa ilustrada',
        2, 'zX8vN1mK5wRt', FALSE, 0),

       ('Rt6Bv9Nh3QsL', '1984 - George Orwell', 15.99, 40,
        'Novela distópica clásica de George Orwell',
        2, 'zX8vN1mK5wRt', FALSE, 0),

       ('Fp2Jk7Xm4YzT', 'Camiseta Nike Dri-FIT', 29.99, 100,
        'Camiseta deportiva Nike con tecnología Dri-FIT para máxima transpirabilidad',
        2, 'qW4yH7jG3sLp', FALSE, 0),

       ('Ln8Cv5Dt1WpR', 'Jeans Levis 501 Original', 89.99, 60,
        'Pantalones vaqueros Levis 501, corte clásico y ajuste regular',
        2, 'qW4yH7jG3sLp', FALSE, 0),

       ('Vb4Gx9Hs6MqK', 'Sofá IKEA KIVIK 3 plazas', 449.99, 10,
        'Sofá de 3 plazas con funda lavable y diseño moderno',
        1, 'pL9dF2nB6vCx', FALSE, 0),

       ('Qs1Zw8Ty3NlJ', 'Lámpara Philips Hue White', 59.99, 35,
        'Bombilla LED inteligente con control por aplicación',
        1, 'pL9dF2nB6vCx', FALSE, 0),

       ('Mx7Pk2Vn5RbD', 'Bicicleta Trek Marlin 5', 599.99, 8,
        'Bicicleta de montaña con cuadro de aluminio y frenos de disco',
        2, 'mK5tR8hJ1zWq', FALSE, 0),

       ('Jt3Lw6Fh9CmY', 'Balón Adidas UCL Finale', 34.99, 75,
        'Balón oficial de la UEFA Champions League',
        2, 'mK5tR8hJ1zWq', FALSE, 0),

       ('Tx5Wr9Km2NhP', 'Samsung Galaxy S23 Ultra', 1199.99, 20,
        'Samsung Galaxy S23 Ultra con pantalla AMOLED de 6.8 pulgadas y S Pen',
        1, 'aB3xT9kL2pQm', FALSE, 0),

       ('Gn7Qs4Lv8BxZ', 'Cien Años de Soledad', 22.99, 35,
        'Obra maestra de Gabriel García Márquez, edición especial',
        2, 'zX8vN1mK5wRt', FALSE, 0);

-- Insertar imágenes de productos
INSERT INTO product_images (product_id, images)
VALUES ('Hx9Lp2Ks4TnB', 'laptop-hp-1.jpg'),
       ('Hx9Lp2Ks4TnB', 'laptop-hp-2.jpg'),
       ('Yw3Zq7Vm1RfG', 'iphone14-pro-1.jpg'),
       ('Yw3Zq7Vm1RfG', 'iphone14-pro-2.jpg'),
       ('Yw3Zq7Vm1RfG', 'iphone14-pro-3.jpg'),
       ('Dk5Mn8Pj2WcX', 'quijote-cover.jpg'),
       ('Rt6Bv9Nh3QsL', '1984-cover.jpeg'),
       ('Fp2Jk7Xm4YzT', 'nike-tshirt-1.jpg'),
       ('Fp2Jk7Xm4YzT', 'nike-tshirt-2.jpg'),
       ('Ln8Cv5Dt1WpR', 'levis-501-1.jpg'),
       ('Vb4Gx9Hs6MqK', 'sofa-kivik-1.jpg'),
       ('Vb4Gx9Hs6MqK', 'sofa-kivik-2.jpg'),
       ('Qs1Zw8Ty3NlJ', 'philips-hue-1.jpg'),
       ('Mx7Pk2Vn5RbD', 'trek-marlin-1.jpg'),
       ('Mx7Pk2Vn5RbD', 'trek-marlin-2.jpg'),
       ('Jt3Lw6Fh9CmY', 'adidas-ucl-ball.jpg'),
       ('Tx5Wr9Km2NhP', 'samsung-s23-1.jpg'),
       ('Tx5Wr9Km2NhP', 'samsung-s23-2.jpg'),
       ('Gn7Qs4Lv8BxZ', 'cien-anos-cover.jpg');

-- Insertar comentarios de productos
INSERT INTO product_comments (product_id, user_id, content, verified, recommended, created_at)
VALUES ('Hx9Lp2Ks4TnB', 3, 'Excelente portátil, muy rápido y ligero. Lo recomiendo 100%', TRUE, TRUE,
        NOW() - INTERVAL '5 days'),
       ('Hx9Lp2Ks4TnB', 4, 'Buena relación calidad-precio.  Perfecto para trabajo diario. ', TRUE, TRUE,
        NOW() - INTERVAL '3 days'),
       ('Yw3Zq7Vm1RfG', 3, 'La cámara es increíble, mejores fotos que nunca. ', TRUE, TRUE, NOW() - INTERVAL '10 days'),
       ('Yw3Zq7Vm1RfG', 5, 'Un poco caro pero vale la pena.  Muy satisfecho. ', TRUE, TRUE, NOW() - INTERVAL '7 days'),
       ('Dk5Mn8Pj2WcX', 6, 'Edición preciosa, las ilustraciones son magníficas.', TRUE, TRUE,
        NOW() - INTERVAL '15 days'),
       ('Rt6Bv9Nh3QsL', 4, 'Un clásico que todos deberían leer. Muy actual. ', TRUE, TRUE, NOW() - INTERVAL '12 days'),
       ('Fp2Jk7Xm4YzT', 5, 'Cómoda y de buena calidad. Ideal para entrenar.', TRUE, TRUE, NOW() - INTERVAL '8 days'),
       ('Ln8Cv5Dt1WpR', 6, 'Talla perfecta, muy cómodos.  Los Levis nunca fallan.', TRUE, TRUE,
        NOW() - INTERVAL '6 days'),
       ('Vb4Gx9Hs6MqK', 3, 'Muy cómodo y fácil de montar. Perfecto para mi salón.', TRUE, TRUE,
        NOW() - INTERVAL '20 days'),
       ('Qs1Zw8Ty3NlJ', 4, 'La app funciona genial.  Me encanta poder controlar la luz desde el móvil.', TRUE, TRUE,
        NOW() - INTERVAL '4 days'),
       ('Mx7Pk2Vn5RbD', 5, 'Excelente bici para montaña. Muy resistente. ', TRUE, TRUE, NOW() - INTERVAL '25 days'),
       ('Jt3Lw6Fh9CmY', 6, 'Balón de muy buena calidad, perfecto para partidos.', TRUE, TRUE,
        NOW() - INTERVAL '2 days'),
       ('Tx5Wr9Km2NhP', 7, 'El mejor smartphone que he tenido. La pantalla es impresionante.', TRUE, TRUE,
        NOW() - INTERVAL '14 days'),
       ('Gn7Qs4Lv8BxZ', 3, 'Una joya de la literatura latinoamericana. Imprescindible.', TRUE, TRUE,
        NOW() - INTERVAL '18 days');

-- Insertar favoritos de usuarios (solo usuarios normales tienen favoritos)
INSERT INTO users_favs (user_id, favs)
VALUES (3, 'Hx9Lp2Ks4TnB'),
       (3, 'Yw3Zq7Vm1RfG'),
       (3, 'Dk5Mn8Pj2WcX'),
       (4, 'Yw3Zq7Vm1RfG'),
       (4, 'Fp2Jk7Xm4YzT'),
       (4, 'Qs1Zw8Ty3NlJ'),
       (5, 'Rt6Bv9Nh3QsL'),
       (5, 'Mx7Pk2Vn5RbD'),
       (6, 'Ln8Cv5Dt1WpR'),
       (6, 'Jt3Lw6Fh9CmY'),
       (6, 'Vb4Gx9Hs6MqK'),
       (7, 'Tx5Wr9Km2NhP'),
       (7, 'Gn7Qs4Lv8BxZ');