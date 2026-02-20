package exception;

public class AdminAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AdminAlreadyExistsException(String msg) {
        super(msg);
    }
}