package main;

import model.Course;
import model.Payment;
import model.PaymentStatus;
import model.Student;
import service.AttendanceService;
import service.CourseService;
import service.PaymentService;
import service.StudentService;
import service.TeacherService;
import service.impl.AttendanceServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.PaymentServiceImpl;
import service.impl.StudentServiceImpl;
import service.impl.TeacherServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import exception.StudentNotFoundException;

public class UserDashboard {

	private final Scanner sc = new Scanner(System.in);

	// Services
	private final AttendanceService attendanceService = new AttendanceServiceImpl();
	private final TeacherService teacherService = new TeacherServiceImpl();
	private final CourseService courseService = new CourseServiceImpl(teacherService);
	private final PaymentService paymentService = new PaymentServiceImpl(courseService);
	private final StudentService studentService = new StudentServiceImpl(paymentService, attendanceService);

	private Student currentStudent;

	public void start() {

		// 1️⃣ Show all students
		List<Student> allStudents = studentService.getAllStudents();
		if (allStudents.isEmpty()) {
			System.out.println("❌ No students found. Please add students first.");
			return;
		}

		System.out.println("Available Students:");
		allStudents.forEach(s -> System.out
				.println("ID: " + s.getStudentId() + " | Name: " + s.getName() + " | Course ID: " + s.getCourseId()));

		// 2️⃣ Ask student to login via Student ID
		System.out.print("Enter your Student ID to login: ");
		Integer studentId = Integer.parseInt(sc.nextLine());

		try {
			currentStudent = studentService.getStudentById(studentId);
		} catch (StudentNotFoundException e) {
			System.out.println("❌ Student not found. Exiting...");
			return;
		}

		boolean running = true;

		while (running) {
			System.out.println("""
					--- USER DASHBOARD ---
					1. View Attendance %
					2. View Pending Fees
					3. View Profile
					4. View Payment History
					5. View Course Details
					0. Logout
					""");

			System.out.print("Choose: ");
			int choice;
			try {
				choice = Integer.parseInt(sc.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Invalid input! Enter number.");
				continue;
			}

			switch (choice) {
			case 1 -> viewAttendance();
			case 2 -> viewPendingFees();
			case 3 -> viewProfile();
			case 4 -> viewPaymentHistory();
			case 5 -> viewCourseDetails();
			case 0 -> {
				running = false;
				System.out.println("Logging out...");
			}
			default -> System.out.println("Invalid choice!");
			}
		}
	}

	private void viewAttendance() {
		if (!attendanceService.hasAttendance(currentStudent.getStudentId())) {
			System.out.println("No attendance records found for this student.");
			return;
		}

		double percentage = attendanceService.calculateAttendancePercentage(currentStudent.getStudentId());
		System.out.printf("Attendance: %.2f%%%n", percentage);
	}

	private void viewPendingFees() {
		Course course = courseService.getCourseById(currentStudent.getCourseId());

		if (course == null) {
			System.out.println("Course not found.");
			return;
		}

		BigDecimal courseFee = course.getFees();

		List<Payment> payments = paymentService.getPaymentsByStudent(currentStudent.getStudentId());

		BigDecimal totalPaid = payments.stream().filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
				.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal pending = courseFee.subtract(totalPaid);
		if (pending.compareTo(BigDecimal.ZERO) < 0)
			pending = BigDecimal.ZERO;

		System.out.println("Pending Fees: ₹" + pending);
	}

	private void viewProfile() {
		System.out.println(currentStudent);
	}

	private void viewPaymentHistory() {
		List<Payment> payments = paymentService.getPaymentsByStudent(currentStudent.getStudentId());
		if (payments.isEmpty()) {
			System.out.println("No payment history found.");
		} else {
			payments.forEach(System.out::println);
		}
	}

	private void viewCourseDetails() {
		Course course = courseService.getCourseById(currentStudent.getCourseId());
		if (course == null) {
			System.out.println("Course not found.");
		} else {
			System.out.println(course);
		}
	}
}