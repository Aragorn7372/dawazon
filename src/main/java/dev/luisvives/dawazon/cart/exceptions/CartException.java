package dev.luisvives.dawazon.cart.exceptions;

/**
 * Clase base para excepciones relacionadas con carritos.
 *
 * @see NotFoundException
 * @see ProductQuantityExceededException
 */
public abstract class CartException extends RuntimeException {
    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param message Mensaje descriptivo del error
     */
    public CartException(String message) {
        super(message);
    }

    /**
     * Excepción lanzada cuando no se encuentra un carrito.
     */
    public static class NotFoundException extends CartException {
        /**
         * Constructor que acepta el ID del carrito no encontrado.
         *
         * @param id ID del carrito
         */
        public NotFoundException(String id) {
            super("Carrito no encontrado con id: " + id);
        }
    }

    /**
     * Excepción lanzada cuando la cantidad excede el stock disponible.
     */
    public static class ProductQuantityExceededException extends CartException {
        public ProductQuantityExceededException() {
            super("La cantidad de producto supera el stock");
        }
    }

    /**
     * Excepción lanzada cuando se exceden los intentos permitidos.
     */
    public static class AttemptAmountExceededException extends CartException {
        public AttemptAmountExceededException() {
            super("La cantidad de tries supera el stock");
        }
    }

    /**
     * Excepción lanzada cuando un usuario no tiene autorización.
     */
    public static class UnauthorizedException extends RuntimeException {
        /**
         * Constructor que acepta un mensaje de error.
         *
         * @param message Mensaje descriptivo del error
         */
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
