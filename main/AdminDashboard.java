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
import java.util.Optional;
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

		this.teacherService = new TeacherServiceImpl();
		this.courseService = new CourseServiceImpl(teacherService);
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
				System.out.println("Error: " + e.getMessage());
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

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addStudent();
			case 2 -> updateStudent();
			case 3 -> deleteStudent();
			case 4 -> {
				List<Student> students = studentService.getAllStudents();

				if (students.isEmpty()) {
					System.out.println("No students found.");
				} else {
					students.forEach(System.out::println);
				}
			}
			case 5 -> {
				Map<Integer, List<Student>> grouped = studentService.getStudentsGroupedByCourse();

				if (grouped.isEmpty()) {
					System.out.println("No students enrolled in any course.");
				} else {
					grouped.forEach((k, v) -> {
						System.out.println("Course ID: " + k);
						v.forEach(System.out::println);
					});
				}
			}
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

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addTeacher();
			case 2 -> updateTeacher();
			case 3 -> deleteTeacher();
			case 4 -> {
				List<Teacher> teachers = teacherService.getAllTeachers();

				if (teachers.isEmpty()) {
					System.out.println("No teachers available.");
					return;
				}

				System.out.println("----- TEACHER LIST -----");

				teachers.forEach(t -> System.out.printf("ID: %-5d | Name: %-20s | Subject: %-15s | Salary: %s%n",
						t.getTeacherId(), t.getName(), t.getSubject(), t.getSalary().toPlainString()));

				System.out.println("------------------------");
			}
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

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addCourse();
			case 2 -> updateCourse();
			case 3 -> deleteCourse();
			case 4 -> {
				List<Course> courses = courseService.getAllCourses();

				if (courses.isEmpty()) {
					System.out.println("No courses available.");
				} else {
					courses.forEach(System.out::println);
				}
			}
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

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addPayment();
			case 2 -> deletePayment();
			case 3 -> paymentService.getAllPayments().stream().findAny().ifPresentOrElse(
					list -> paymentService.getAllPayments().forEach(System.out::println),
					() -> System.out.println("No payments found."));

			case 4 -> {
				Map<Integer, BigDecimal> pendingFees = paymentService.getStudentsWithPendingFees();

				if (pendingFees.isEmpty()) {
					System.out.println("No students with pending fees.");
				} else {
					System.out.println("---- Pending Fees ----");
					pendingFees.forEach((studentId, amount) -> System.out
							.println("Student ID: " + studentId + " | Pending Fees: " + amount));
					System.out.println("----------------------");
				}
			}
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

			System.out.print("Choose: ");
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
				double threshold;

				// ✅ Validate threshold input
				while (true) {
					try {
						System.out.print("Threshold (0-100): ");
						threshold = Double.parseDouble(sc.nextLine());

						if (threshold < 0 || threshold > 100) {
							System.out.println("❌ Invalid input! Enter a number between 0 and 100.");
							continue;
						}

						break; // valid threshold
					} catch (NumberFormatException e) {
						System.out.println("❌ Invalid input! Enter a numeric value between 0 and 100.");
					}
				}

				// ✅ Get students below threshold
				List<Integer> studentsBelow = attendanceService.getStudentsBelowAttendance(threshold);

				if (studentsBelow.isEmpty()) {
					System.out.println("All students have attendance above the threshold.");
				} else {
					System.out.println("Students below " + threshold + "% attendance:");
					studentsBelow.forEach(id -> System.out.println("Student ID: " + id));
				}
			}
			case 0 -> running = false;
			}
		}
	}

	private void addStudent() {

		// 1️⃣ Get all courses
		List<Course> courses = courseService.getAllCourses();
		if (courses.isEmpty()) {
			System.out.println("❌ No courses available. Please add a course first.");
			return;
		}

		System.out.println("Available Courses:");
		courses.forEach(course -> System.out.println(
				"ID: " + course.getCourseId() + " | Name: " + course.getCourseName() + " | Fees: " + course.getFees()));

		// 2️⃣ Select course
		System.out.print("Select Course ID: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		boolean courseExists = courses.stream().anyMatch(c -> c.getCourseId().equals(courseId));
		if (!courseExists) {
			System.out.println("❌ Invalid Course ID.");
			return;
		}

		// 3️⃣ Enter student details
		System.out.print("Student Name: ");
		String name = sc.nextLine();

		System.out.print("Student Email: ");
		String email = sc.nextLine();

		// 4️⃣ Generate student ID and add student
		Integer studentId = IdGenerator.generateId();
		Student student = new Student(studentId, name, email, courseId); // No user linking
		studentService.addStudent(student);

		// 5️⃣ Create pending payment for full course fee
		BigDecimal courseFee = courseService.getCourseById(courseId).getFees();
		paymentService.createPendingPayment(studentId, courseId, courseFee);

		System.out.println("✅ Student added successfully with pending fees created");
	}

	private void updateStudent() {

		System.out.print("Enter Student ID to update: ");
		Integer id = Integer.parseInt(sc.nextLine());

		Student existing = studentService.getStudentById(id);

		System.out.print("New Name: ");
		String name = sc.nextLine();

		System.out.print("New Email: ");
		String email = sc.nextLine();

		List<Course> courses = courseService.getAllCourses();

		System.out.println("\nAvailable Courses:");
		courses.forEach(
				course -> System.out.println("ID: " + course.getCourseId() + " | Name: " + course.getCourseName()));

		System.out.print("New Course ID: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		boolean courseExists = courses.stream().anyMatch(course -> course.getCourseId().equals(courseId));

		if (!courseExists) {
			System.out.println("❌ Invalid Course ID! Student not updated.");
			return;
		}

		Student updated = new Student(existing.getStudentId(), name, email, courseId);

		studentService.updateStudent(updated);

		System.out.println("✅ Student updated successfully");
	}

	private void deleteStudent() {

		System.out.print("Enter Student ID to delete: ");
		Integer studentId = Integer.parseInt(sc.nextLine());

		studentService.deleteStudent(studentId);

		System.out.println("Student deleted successfully");
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

		System.out.println("Teacher added successfully");
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

		System.out.println("Teacher updated successfully");
	}

	private void deleteTeacher() {

		System.out.print("Enter Teacher ID to delete: ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		boolean assigned = courseService.getAllCourses().stream().anyMatch(c -> teacherId.equals(c.getTeacherId()));

		if (assigned) {
			System.out.println("❌ Cannot delete teacher. Assigned to a course.");
			return;
		}

		teacherService.deleteTeacher(teacherId);

		System.out.println("Teacher deleted successfully");
	}

	private void addCourse() {

		if (teacherService.getAllTeachers().isEmpty()) {
			System.out.println("❌ No teachers available. Please add teacher first.");
			return;
		}

		System.out.println("Available Teachers:");
		teacherService.getAllTeachers().forEach(t -> System.out
				.println("ID: " + t.getTeacherId() + " | Name: " + t.getName() + " | Subject: " + t.getSubject()));

		Integer courseId = IdGenerator.generateId();

		System.out.print("Course name: ");
		String name = sc.nextLine();

		System.out.print("Duration (months): ");
		Integer duration = Integer.parseInt(sc.nextLine());

		System.out.print("Fees: ");
		BigDecimal fees = new BigDecimal(sc.nextLine());

		System.out.print("Select Teacher ID: ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		Course course = new Course(courseId, name, duration, fees, teacherId);

		courseService.addCourse(course);

		System.out.println("Course added successfully");
	}

	private void updateCourse() {

		System.out.print("Enter Course ID to update: ");
		Integer id = Integer.parseInt(sc.nextLine());

		Course existing = courseService.getCourseById(id);

		if (existing == null) {
			System.out.println("Course not found");
			return;
		}

		System.out.print("New Name (" + existing.getCourseName() + "): ");
		String name = sc.nextLine();

		System.out.print("New Duration (" + existing.getDuration() + "): ");
		Integer duration = Integer.parseInt(sc.nextLine());

		System.out.print("New Fees (" + existing.getFees() + "): ");
		BigDecimal fees = new BigDecimal(sc.nextLine());

		System.out.println("Available Teachers:");
		teacherService.getAllTeachers()
				.forEach(t -> System.out.println("ID: " + t.getTeacherId() + " | Name: " + t.getName()));

		System.out.print("New Teacher ID (" + existing.getTeacherId() + "): ");
		Integer teacherId = Integer.parseInt(sc.nextLine());

		Course updated = new Course(id, name, duration, fees, teacherId);

		courseService.updateCourse(id, updated);

		System.out.println("Course updated successfully");
	}

	private void deleteCourse() {

		System.out.print("Enter Course ID to delete: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		if (!studentService.getStudentsByCourse(courseId).isEmpty()) {
			throw new IllegalStateException("Cannot delete course. Students are enrolled in this course.");
		}

		courseService.deleteCourse(courseId);

		System.out.println("Course deleted successfully");
	}

	private void addPayment() {
		try {
			// 1️⃣ Get Student ID
			System.out.print("Student ID: ");
			Integer studentId = Integer.parseInt(sc.nextLine());

			Student student = studentService.getStudentById(studentId);
			if (student == null) {
				System.out.println("❌ Student not found");
				return;
			}

			Integer courseId = student.getCourseId();

			// 2️⃣ Get Payment Amount
			System.out.print("Payment Amount: ");
			BigDecimal amount = new BigDecimal(sc.nextLine());

			// 3️⃣ Get Payment Mode
			PaymentMode mode;
			while (true) {
				System.out.print("Payment Mode (CASH/UPI/CARD): ");
				String input = sc.nextLine().trim().toUpperCase();
				try {
					mode = PaymentMode.valueOf(input);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println("❌ Invalid mode! Enter CASH, UPI, or CARD.");
				}
			}

			// 4️⃣ Create Payment object
			Payment payment = new Payment(IdGenerator.generateId(), studentId, courseId, amount, mode,
					PaymentStatus.PENDING, LocalDateTime.now());

			// 5️⃣ Delegate to PaymentServiceImpl
			paymentService.addPayment(payment);

			System.out.println("✅ Payment processed successfully");

		} catch (NumberFormatException e) {
			System.out.println("❌ Invalid numeric input!");
		} catch (Exception e) {
			System.out.println("❌ Error processing payment: " + e.getMessage());
		}
	}

	private void deletePayment() {

		System.out.print("Enter Payment ID to delete: ");
		Integer id = Integer.parseInt(sc.nextLine());

		paymentService.deletePayment(id);

		System.out.println("Payment deleted successfully");
	}

	private void markAttendance() {

		Integer studentId = null;

		// 1️⃣ Validate Student ID
		while (true) {
			try {
				System.out.print("Student ID: ");
				studentId = Integer.parseInt(sc.nextLine());

				// Check if student exists
				studentService.getStudentById(studentId); // will throw exception if not found
				break; // valid ID, exit loop
			} catch (NumberFormatException e) {
				System.out.println("❌ Invalid input! Enter a numeric Student ID.");
			} catch (Exception e) {
				System.out.println("❌ Student not found with ID: " + studentId);
			}
		}

		// 2️⃣ Validate Present input
		boolean present;
		while (true) {
			System.out.print("Present? (true/false): ");
			String input = sc.nextLine().trim().toLowerCase();

			if (input.equals("true")) {
				present = true;
				break;
			} else if (input.equals("false")) {
				present = false;
				break;
			} else {
				System.out.println("❌ Invalid input! Please enter 'true' or 'false'.");
			}
		}

		// 3️⃣ Mark Attendance
		try {
			attendanceService.markAttendance(studentId, present);
			System.out.println("✅ Attendance marked successfully");
		} catch (Exception e) {
			System.out.println("❌ Error marking attendance: " + e.getMessage());
		}
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
				double threshold;

				// Validate threshold input
				while (true) {
					try {
						System.out.print("Enter attendance threshold (0-100): ");
						threshold = Double.parseDouble(sc.nextLine());

						if (threshold < 0 || threshold > 100) {
							System.out.println("❌ Invalid input! Enter a number between 0 and 100.");
							continue;
						}

						break; // valid threshold
					} catch (NumberFormatException e) {
						System.out.println("❌ Invalid input! Enter a numeric value between 0 and 100.");
					}
				}

				reportService.generateLowAttendanceReportAsync(threshold);
				System.out.println("✅ Low attendance report triggered for threshold: " + threshold + "%");
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