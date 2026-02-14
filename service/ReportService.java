package service;

public interface ReportService {

	    void generateStudentReportAsync();
	    void generateTeacherReportAsync();

	    void generateStudentByCourseReportAsync();
	    void generatePendingFeesReportAsync();
	    void generateLowAttendanceReportAsync(double threshold);
	    void generateTeacherCourseMappingReportAsync();
	    void generateAllReportsAsync();
	    void shutdown();
}
