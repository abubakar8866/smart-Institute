package service.impl;

import model.Student;
import model.Teacher;
import model.Payment;
import model.PaymentStatus;
import service.*;
import util.FileUtil;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ReportServiceImpl implements ReportService {

	private final StudentService studentService;
	private final TeacherService teacherService;
	private final AttendanceService attendanceService;
	private final PaymentService paymentService;

	private final ExecutorService executor = new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>());

	private static final String REPORT_FOLDER = "reports/";

	public ReportServiceImpl(StudentService studentService, TeacherService teacherService,
			AttendanceService attendanceService, PaymentService paymentService) {

		this.studentService = studentService;
		this.teacherService = teacherService;
		this.attendanceService = attendanceService;
		this.paymentService = paymentService;

		new java.io.File(REPORT_FOLDER).mkdirs();
	}

	private void writeReport(String path, String content) {
		FileUtil.overwriteFile(path, content);
	}

	private void runAsync(Runnable task) {

		if (executor.isShutdown()) {
			throw new IllegalStateException("ReportService already shutdown");
		}

		executor.submit(() -> {
			try {
				task.run();
			} catch (Exception e) {
				System.err.println("Report generation failed: " + e.getMessage());
			}
		});
	}

	// ================= EXISTING REPORTS =================

	@Override
	public void generateStudentReportAsync() {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== STUDENT REPORT =====\n").append("Generated At: ")
					.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			for (Student student : studentService.getAllStudents()) {

				double attendance;

				try {
					attendance = attendanceService.calculateAttendancePercentage(student.getStudentId());
				} catch (Exception e) {
					attendance = 0.0; // No attendance yet
				}

				BigDecimal totalPaid = paymentService.getPaymentsByStudent(student.getStudentId()).stream()
						.filter(p -> p.getStatus() == PaymentStatus.SUCCESS).map(Payment::getAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				report.append("--------------------------------\n").append("ID           : ")
						.append(student.getStudentId()).append("\n").append("Name         : ").append(student.getName())
						.append("\n").append("Email        : ").append(student.getEmail()).append("\n")
						.append("Attendance % : ").append(String.format("%.2f", attendance)).append("\n")
						.append("Fees Paid    : ").append(totalPaid).append("\n");
			}

			writeReport(REPORT_FOLDER + "student-report.txt", report.toString());
		});
	}

	@Override
	public void generateTeacherReportAsync() {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== TEACHER REPORT =====\n").append("Generated At: ")
					.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			for (Teacher teacher : teacherService.getAllTeachers()) {

				report.append("--------------------------------\n").append("ID      : ").append(teacher.getTeacherId())
						.append("\n").append("Name    : ").append(teacher.getName()).append("\n").append("Subject : ")
						.append(teacher.getSubject()).append("\n").append("Salary  : ").append(teacher.getSalary())
						.append("\n");
			}

			writeReport(REPORT_FOLDER + "teacher-report.txt", report.toString());
		});
	}

	// ================= NEW REPORTS =================

	@Override
	public void generateStudentByCourseReportAsync() {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== STUDENT BY COURSE REPORT =====\n").append("Generated At: ")
					.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			Map<Integer, List<Student>> grouped = studentService.getStudentsGroupedByCourse();

			grouped.forEach((courseId, students) -> {
				report.append("Course ID: ").append(courseId).append("\n");
				students.forEach(s -> report.append("  - ").append(s.getStudentId()).append(" : ").append(s.getName())
						.append("\n"));
				report.append("\n");
			});

			writeReport(REPORT_FOLDER + "student-by-course-report.txt", report.toString());
		});
	}

	@Override
	public void generatePendingFeesReportAsync() {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== PENDING FEES REPORT =====\n").append("Generated At: ")
					.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			paymentService.getStudentsWithPendingFees()
					.forEach(id -> report.append("Student ID: ").append(id).append("\n"));

			writeReport(REPORT_FOLDER + "pending-fees-report.txt", report.toString());
		});
	}

	@Override
	public void generateLowAttendanceReportAsync(double threshold) {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== LOW ATTENDANCE REPORT =====\n").append("Threshold: ").append(threshold).append("%\n")
					.append("Generated At: ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			attendanceService.getStudentsBelowAttendance(threshold)
					.forEach(id -> report.append("Student ID: ").append(id).append("\n"));

			writeReport(REPORT_FOLDER + "low-attendance-report.txt", report.toString());
		});
	}

	@Override
	public void generateTeacherCourseMappingReportAsync() {

		runAsync(() -> {

			StringBuilder report = new StringBuilder(2048);

			report.append("===== TEACHER COURSE MAPPING =====\n").append("Generated At: ")
					.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).append("\n\n");

			teacherService.getTeacherCourseMapping()
					.forEach((teacher, course) -> report.append(teacher).append(" -> ").append(course).append("\n"));

			writeReport(REPORT_FOLDER + "teacher-course-mapping-report.txt", report.toString());
		});
	}

	// ================= ALL =================

	@Override
	public void generateAllReportsAsync() {

		runAsync(() -> {
			generateStudentReportAsync();
			generateTeacherReportAsync();
			generateStudentByCourseReportAsync();
			generatePendingFeesReportAsync();
			generateLowAttendanceReportAsync(75);
			generateTeacherCourseMappingReportAsync();
		});
	}

	@Override
	public void shutdown() {

		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}

		System.out.println("ReportService shutdown complete.");
	}

}
