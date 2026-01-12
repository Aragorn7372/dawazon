package dev.luisvives.dawazon.products.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un producto en el sistema Dawazon.
 * <p>
 * Esta clase modela los productos disponibles en la plataforma de comercio
 * electrónico,
 * incluyendo información básica del producto, su categoría, stock, imágenes y
 * comentarios
 * de usuarios.
 * </p>
 *
 * <p>
 * Características principales:
 * <ul>
 * <li>Generación automática de ID personalizado mediante
 * {@link dev.luisvives.dawazon.common.utils.IdGenerator}</li>
 * <li>Relación {@link ManyToOne} con {@link Category} para clasificación de
 * productos</li>
 * <li>Colección de imágenes asociadas al producto</li>
 * <li>Sistema de comentarios embebidos mediante {@link Comment}</li>
 * <li>Soft delete mediante el campo {@code isDeleted}</li>
 * <li>Auditoría automática con fechas de creación y actualización</li>
 * <li>Control de concurrencia optimista mediante {@code @Version}</li>
 * </ul>
 * </p>
 *
 * @see Category
 * @see Comment
 * @see dev.luisvives.dawazon.common.utils.IdGenerator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Product {
        /**
         * Identificador único del producto.
         * <p>
         * Generado automáticamente mediante
         * {@link dev.luisvives.dawazon.common.utils.IdGenerator}
         * que crea IDs alfanuméricos personalizados.
         * </p>
         */
        @Id
        @GeneratedValue(generator = "custom-id")
        @GenericGenerator(name = "custom-id", strategy = "dev.luisvives.dawazon.common.utils.IdGenerator")
        private String id;
        /**
         * Nombre del producto.
         */
        @Column(nullable = false)
        private String name;
        /**
         * Precio del producto.
         */
        @Column(nullable = false)
        private Double price;
        /**
         * Cantidad disponible en inventario.
         */
        @Column(nullable = false)
        private Integer stock;
        /**
         * Descripción detallada del producto.
         */
        @Column(nullable = false)
        private String description;
        /**
         * ID del usuario que creó este producto.
         * <p>
         * Permite rastrear qué vendedor creó el producto.
         * </p>
         */
        @Column(nullable = false)
        private Long creatorId;

        /**
         * Lista de rutas a las imágenes del producto.
         * <p>
         * Se cargan de forma EAGER para tener las imágenes disponibles inmediatamente.
         * Las rutas corresponden a archivos almacenados por el
         * {@link dev.luisvives.dawazon.common.storage.service.StorageService}.
         * </p>
         */
        @Column(nullable = false)
        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        @Builder.Default
        private List<String> images = new java.util.ArrayList<>();

        /**
         * Categoría a la que pertenece el producto.
         * <p>
         * Relación {@link ManyToOne} con carga EAGER para tener la categoría disponible
         * sin necesidad de consultas adicionales.
         * </p>
         */
        @ManyToOne(fetch = FetchType.EAGER)
        private Category category;

        /**
         * Lista de comentarios asociados al producto.
         * <p>
         * Los comentarios son objetos embebibles ({@link Comment}) almacenados en una
         * tabla
         * separada {@code product_comments} mediante {@link CollectionTable}.
         * Se cargan de forma EAGER para mostrarlos junto con el producto.
         * </p>
         */
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "product_comments", joinColumns = @JoinColumn(name = "product_id"))
        @Builder.Default
        private List<Comment> comments = new java.util.ArrayList<>();

        /**
         * Indica si el producto ha sido eliminado lógicamente.
         * <p>
         * Se usa soft delete para mantener el historial de productos sin eliminarlos
         * físicamente de la base de datos.
         * </p>
         */
        @Column(nullable = false)
        private boolean isDeleted;

        /**
         * Fecha y hora de creación del producto.
         * <p>
         * Se establece automáticamente al crear el producto mediante
         * {@link CreatedDate}.
         * </p>
         */
        @Column(nullable = false)
        @CreatedDate
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        /**
         * Fecha y hora de la última actualización del producto.
         * <p>
         * Se actualiza automáticamente en cada modificación mediante
         * {@link LastModifiedDate}.
         * </p>
         */
        @Column(nullable = false)
        @LastModifiedDate
        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();
        /**
         * Versión del producto para control de concurrencia optimista.
         * <p>
         * Previene conflictos cuando múltiples usuarios intentan modificar el mismo
         * producto
         * simultáneamente. Hibérnate incrementa este valor automáticamente en cada
         * actualización.
         * </p>
         */
        @Version
        @Builder.Default
        private Long version = 0L;
}
