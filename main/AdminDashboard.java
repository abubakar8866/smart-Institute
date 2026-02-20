package main;

import model.Course;
import model.Payment;
import model.PaymentMode;
import model.PaymentStatus;
import model.Role;
import model.Student;
import model.Teacher;
import model.User;
import service.*;
import service.impl.*;
import util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AdminDashboard {

	private final Scanner sc = new Scanner(System.in);

	private final TeacherService teacherService;
	private final CourseService courseService;
	private final AttendanceService attendanceService;
	private final PaymentService paymentService;
	private final StudentService studentService;
	private final RegistrationService registrationService;
	private final ReportService reportService;

	public AdminDashboard() {

		this.courseService = new CourseServiceImpl();
		this.teacherService = new TeacherServiceImpl(courseService);
		this.attendanceService = new AttendanceServiceImpl();
		this.paymentService = new PaymentServiceImpl(courseService);
		this.studentService = new StudentServiceImpl(paymentService, attendanceService);
		this.registrationService = new RegistrationServiceImpl();

		this.reportService = new ReportServiceImpl(studentService, teacherService, attendanceService, paymentService);
	}

	public void start() {

		boolean running = true;

		while (running) {

			System.out.println("""
					===== ADMIN DASHBOARD =====
					1. Student Management
					2. Teacher Management
					3. Course Management
					4. Payment Management
					5. Attendance Management
					6. Reports
					0. Logout
					""");

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			try {
				switch (choice) {
				case 1 -> studentMenu();
				case 2 -> teacherMenu();
				case 3 -> courseMenu();
				case 4 -> paymentMenu();
				case 5 -> attendanceMenu();
				case 6 -> generateReports();
				case 0 -> running = false;
				default -> System.out.println("Invalid choice");
				}
			} catch (Exception e) {
				System.out.println("❌ Error: " + e.getMessage());
			}
		}
	}

	private void studentMenu() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- STUDENT MANAGEMENT ---
					1. Add Student
					2. Update Student
					3. Delete Student
					4. View All Students
					5. Group Students By Course
					0. Back
					""");

			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addStudent();
			case 2 -> updateStudent();
			case 3 -> deleteStudent();
			case 4 -> studentService.getAllStudents().forEach(System.out::println);
			case 5 -> studentService.getStudentsGroupedByCourse().forEach((k, v) -> {
				System.out.println("Course ID: " + k);
				v.forEach(System.out::println);
			});
			case 0 -> running = false;
			}
		}
	}

	private void teacherMenu() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- TEACHER MANAGEMENT ---
					1. Add Teacher
					2. Update Teacher
					3. Delete Teacher
					4. View All Teachers
					5. View Teacher-Course Mapping
					0. Back
					""");

			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addTeacher();
			case 2 -> updateTeacher();
			case 3 -> deleteTeacher();
			case 4 -> teacherService.getAllTeachers().forEach(System.out::println);
			case 5 -> teacherService.getTeacherCourseMapping()
					.forEach((id, name) -> System.out.println("Teacher ID: " + id + " | Course: " + name));
			case 0 -> running = false;
			}
		}
	}

	private void courseMenu() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- COURSE MANAGEMENT ---
					1. Add Course
					2. Update Course
					3. Delete Course
					4. View All Courses
					0. Back
					""");

			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addCourse();
			case 2 -> updateCourse();
			case 3 -> deleteCourse();
			case 4 -> courseService.getAllCourses().values().forEach(System.out::println);
			case 0 -> running = false;
			}
		}
	}

	private void paymentMenu() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- PAYMENT MANAGEMENT ---
					1. Add Payment
					2. Delete Payment
					3. View All Payments
					4. View Pending Students
					0. Back
					""");

			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addPayment();
			case 2 -> deletePayment();
			case 3 -> paymentService.getAllPayments().forEach(System.out::println);
			case 4 ->
				paymentService.getStudentsWithPendingFees().forEach(id -> System.out.println("Student ID: " + id));
			case 0 -> running = false;
			}
		}
	}

	private void attendanceMenu() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- ATTENDANCE MANAGEMENT ---
					1. Mark Attendance
					2. View Attendance Percentage
					3. Students Below Threshold
					0. Back
					""");

			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> markAttendance();
			case 2 -> {
				System.out.print("Student ID: ");
				Integer id = Integer.parseInt(sc.nextLine());
				double percentage = attendanceService.calculateAttendancePercentage(id);
				System.out.println("Attendance: " + percentage + "%");
			}
			case 3 -> {
				System.out.print("Threshold: ");
				double t = Double.parseDouble(sc.nextLine());
				attendanceService.getStudentsBelowAttendance(t).forEach(id -> System.out.println("Student ID: " + id));
			}
			case 0 -> running = false;
			}
		}
	}

	private void addStudent() {

		System.out.print("Name: ");
		String name = sc.nextLine();

		System.out.print("Email: ");
		String email = sc.nextLine();

		System.out.print("Course ID: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		Integer studentId = IdGenerator.generateId();

		Map<String, User> allUsers = registrationService.getAllUsers();
		Set<Integer> linkedUserIds = studentService.getAllLinkedUserIds();

		List<User> availableUsers = allUsers.values().stream().filter(u -> u.getRole() == Role.USER)
				.filter(u -> !linkedUserIds.contains(u.getUserId())).toList();

		if (availableUsers.isEmpty()) {
			System.out.println("No available users to link.");
			return;
		}

		System.out.println("Available Users:");
		for (User u : availableUsers) {
			System.out.println("ID: " + u.getUserId() + " | Username: " + u.getUsername());
		}

		System.out.print("Select User ID: ");
		Integer selectedUserId = Integer.parseInt(sc.nextLine());

		Student student = new Student(studentId, selectedUserId, name, email, courseId);
		studentService.addStudent(student);

		System.out.println("✅ Student added successfully");
	}

	private void updateStudent() {

		System.out.print("Enter Student ID to update: ");
		Integer id = Integer.parseInt(sc.nextLine());

		Student existing = studentService.getStudentById(id);

		System.out.print("New Name: ");
		String name = sc.nextLine();

		System.out.print("New Email: ");
		String email = sc.nextLine();

		System.out.print("New Course ID: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		Student updated = new Student(existing.getStudentId(), existing.getUserId(), name, email, courseId);

		studentService.updateStudent(updated);

		System.out.println("✅ Student updated successfully");
	}

	private void deleteStudent() {

		System.out.print("Enter Student ID to delete: ");
		Integer studentId = Integer.parseInt(sc.nextLine());

		studentService.deleteStudent(studentId);

		System.out.println("✅ Student deleted successfully");
	}

	private void addTeacher() {

		System.out.print("Name: ");
		String name = sc.nextLine();

		System.out.print("Subject: ");
		String subject = sc.nextLine();

		System.out.print("Salary: ");
		BigDecimal salary = new BigDecimal(sc.nextLine());

		Integer teacherId = IdGenerator.generateId();

		Teacher teacher = new Teacher(teacherId, name, subject, salary);
		teacherService.addTeacher(teacher);

		System.out.println("✅ Teacher added successfully");
	}

	private void updateTeacher() {

		System.out.print("Enter Teacher ID to update: ");
		Integer id = Integer.parseInt(sc.nextLine());

		Teacher existing = teacherService.getTeacherById(id);

		if (existing == null) {
			System.out.println("❌ Teacher not found");
			return;
		}

		System.out.print("New Name (" + existing.getName() + "): ");
		String name = sc.nextLine();

		System.out.print("New Subject (" + existing.getSubject() + "): ");
		String subject = sc.nextLine();

		System.out.print("New Salary (" + existing.getSalary() + "): ");
		BigDecimal salary = new BigDecimal(sc.nextLine());

		Teacher updated = new Teacher(id, name, subject, salary);

		teacherService.updateTeacher(id, updated);

		System.out.println("✅ Teacher updated successfully");
	}

	private void deleteTeacher() {

		System.out.print("Enter Teacher ID to delete: ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		teacherService.deleteTeacher(teacherId);

		System.out.println("✅ Teacher deleted successfully");
	}

	private void addCourse() {

		Integer courseId = IdGenerator.generateId();

		System.out.print("Course name: ");
		String name = sc.nextLine();

		System.out.print("Duration (months): ");
		Integer duration = Integer.parseInt(sc.nextLine());

		System.out.print("Fees: ");
		BigDecimal fees = new BigDecimal(sc.nextLine());

		System.out.print("Teacher ID: ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		Course course = new Course(courseId, name, duration, fees, teacherId);
		courseService.addCourse(course);

		System.out.println("✅ Course added successfully");
	}

	private void updateCourse() {

		System.out.print("Enter Course ID to update: ");
		Integer id = Integer.parseInt(sc.nextLine());

		Course existing = courseService.getCourseById(id);

		if (existing == null) {
			System.out.println("❌ Course not found");
			return;
		}

		System.out.print("New Name (" + existing.getCourseName() + "): ");
		String name = sc.nextLine();

		System.out.print("New Duration (" + existing.getDuration() + "): ");
		Integer duration = Integer.parseInt(sc.nextLine());

		System.out.print("New Fees (" + existing.getFees() + "): ");
		BigDecimal fees = new BigDecimal(sc.nextLine());

		System.out.print("New Teacher ID (" + existing.getTeacherId() + "): ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		Course updated = new Course(id, name, duration, fees, teacherId);

		courseService.updateCourse(id, updated);

		System.out.println("✅ Course updated successfully");
	}

	private void deleteCourse() {

		System.out.print("Enter Course ID to delete: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		if (!studentService.getStudentsByCourse(courseId).isEmpty()) {
			throw new IllegalStateException("Cannot delete course. Students are enrolled in this course.");
		}

		courseService.deleteCourse(courseId);

		System.out.println("✅ Course deleted successfully");
	}

	private void addPayment() {

		System.out.print("Student ID: ");
		Integer studentId = Integer.parseInt(sc.nextLine());

		Student student = studentService.getStudentById(studentId);

		if (student == null) {
			System.out.println("❌ Student not found");
			return;
		}

		Integer courseId = student.getCourseId();

		System.out.print("Amount: ");
		BigDecimal amount = new BigDecimal(sc.nextLine());

		System.out.print("Payment Mode (CASH/UPI/CARD): ");
		PaymentMode mode = PaymentMode.valueOf(sc.nextLine().toUpperCase());

		Integer paymentId = IdGenerator.generateId();

		Payment payment = new Payment(paymentId, studentId, courseId, amount, mode, PaymentStatus.SUCCESS,
				LocalDateTime.now());

		paymentService.addPayment(payment);

		System.out.println("✅ Payment added successfully");
	}

	private void deletePayment() {

		System.out.print("Enter Payment ID to delete: ");
		Integer id = Integer.parseInt(sc.nextLine());

		paymentService.deletePayment(id);

		System.out.println("✅ Payment deleted successfully");
	}

	private void markAttendance() {

		System.out.print("Student ID: ");
		Integer studentId = Integer.parseInt(sc.nextLine());

		System.out.print("Present? (true/false): ");
		boolean present = Boolean.parseBoolean(sc.nextLine());

		attendanceService.markAttendance(studentId, present);

		System.out.println("✅ Attendance marked");
	}

	// ---------------- REPORTS ----------------

	private void generateReports() {

		boolean running = true;

		while (running) {

			System.out.println("""
					--- REPORT MENU ---
					1. Student Report
					2. Teacher Report
					3. Student By Course
					4. Pending Fees
					5. Low Attendance
					6. Teacher-Course Mapping
					7. Generate ALL
					0. Back
					""");

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {

			case 1 -> reportService.generateStudentReportAsync();
			case 2 -> reportService.generateTeacherReportAsync();
			case 3 -> reportService.generateStudentByCourseReportAsync();
			case 4 -> reportService.generatePendingFeesReportAsync();
			case 5 -> {
				System.out.print("Enter attendance threshold (%): ");
				double threshold = Double.parseDouble(sc.nextLine());
				reportService.generateLowAttendanceReportAsync(threshold);
			}
			case 6 -> reportService.generateTeacherCourseMappingReportAsync();
			case 7 -> reportService.generateAllReportsAsync();
			case 0 -> running = false;
			default -> System.out.println("Invalid choice");
			}

			if (choice != 0) {
				System.out.println("Reports generating in background...");
			}
		}
	}
}