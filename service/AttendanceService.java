package service;

public interface AttendanceService {

	void markAttendance(Integer studentId, boolean present);

    double calculateAttendancePercentage(Integer studentId);
	
}
