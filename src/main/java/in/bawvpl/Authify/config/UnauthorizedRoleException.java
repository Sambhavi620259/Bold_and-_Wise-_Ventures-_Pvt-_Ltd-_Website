package in.bawvpl.Authify.config;

public class UnauthorizedRoleException extends RuntimeException {

    public UnauthorizedRoleException(String message) {
        super(message);
    }

    public UnauthorizedRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}

