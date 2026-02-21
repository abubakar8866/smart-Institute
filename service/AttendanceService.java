package service;

import java.util.List;

public interface AttendanceService {

	void markAttendance(Integer studentId, boolean present);

    double calculateAttendancePercentage(Integer studentId);
    
    List<Integer> getStudentsBelowAttendance(double threshold);
    
    boolean hasAttendance(Integer studentId);
	
}
