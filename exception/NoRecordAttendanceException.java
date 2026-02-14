package exception;

public class NoRecordAttendanceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoRecordAttendanceException(String msg) {
		super(msg);
	}
	
}
