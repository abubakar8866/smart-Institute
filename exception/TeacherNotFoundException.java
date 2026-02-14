package exception;

public class TeacherNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeacherNotFoundException(String msg) {
		super(msg);
	}
	
}
