package dev.luisvives.dawazon.products.exception;

public abstract class ProductException extends IllegalArgumentException {
	public ProductException(String message) {
		super(message);
	}
    public static class ValidationException extends ProductException {
        public ValidationException(String message) {
            super(message);
        }
    }
    public static class NotFoundException extends ProductException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
