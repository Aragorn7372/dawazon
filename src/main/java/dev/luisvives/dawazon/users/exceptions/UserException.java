package dev.luisvives.dawazon.users.exceptions;

public abstract class UserException extends RuntimeException{
    public UserException(String message){
        super(message);
    }
    public static class UserNotFoundException extends UserException{
        public UserNotFoundException(String message){
            super(message);
        }
    }
    public static class UserPasswordNotMatchException extends UserException{
        public UserPasswordNotMatchException(String message){
            super(message);
        }
    }
    public static class UserPermisionDeclined extends UserException{
        public UserPermisionDeclined(String message){
            super(message);
        }
    }
    public static class UserHasThatFavProductException extends UserException{
        public UserHasThatFavProductException(String message){
            super(message);
        }
    }
}
