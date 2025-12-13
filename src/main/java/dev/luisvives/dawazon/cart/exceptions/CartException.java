package dev.luisvives.dawazon.cart.exceptions;

public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }

    public static class NotFoundException extends CartException {
        public NotFoundException(String id) {
            super("Carrito no encontrado con id: " + id);
        }
    }

    public static class ProductQuantityExceededException extends CartException {
        public ProductQuantityExceededException() {
            super("La cantidad de producto supera el stock o el producto no existe.");
        }
    }
}
