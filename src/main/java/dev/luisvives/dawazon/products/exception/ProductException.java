package dev.luisvives.dawazon.products.exception;

/**
 * Clase base para excepciones relacionadas con productos.
 *
 * @see ValidationException
 * @see NotFoundException
 */
public abstract class ProductException extends IllegalArgumentException {
    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param message Mensaje descriptivo del error
     */
    public ProductException(String message) {
        super(message);
    }

    /**
     * Excepción lanzada cuando hay errores de validación en operaciones con
     * productos.
     */
    public static class ValidationException extends ProductException {
        /**
         * Constructor que acepta un mensaje de validación.
         *
         * @param message Mensaje descriptivo del error de validación
         */
        public ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando no se encuentra un producto.
     */
    public static class NotFoundException extends ProductException {
        /**
         * Constructor que acepta un mensaje indicando que el producto no fue
         * encontrado.
         *
         * @param message Mensaje descriptivo del error de búsqueda
         */
        public NotFoundException(String message) {
            super(message);
        }
    }
}
