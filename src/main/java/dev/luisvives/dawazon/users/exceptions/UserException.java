package dev.luisvives.dawazon.users.exceptions;

/**
 * Excepción base abstracta para errores relacionados con usuarios.
 */
public abstract class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    /**
     * Excepción lanzada cuando no se encuentra un usuario.
     */
    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando la contraseña no coincide.
     */
    public static class UserPasswordNotMatchException extends UserException {
        public UserPasswordNotMatchException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando se deniega un permiso al usuario.
     */
    public static class UserPermissionDeclined extends UserException {
        public UserPermissionDeclined(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando el usuario ya tiene un producto en favoritos.
     */
    public static class UserHasThatFavProductException extends UserException {
        public UserHasThatFavProductException(String message) {
            super(message);
        }
    }
}
