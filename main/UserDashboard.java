package main;

import model.Course;
import model.Payment;
import model.PaymentStatus;
import model.Student;
import model.User;
import service.AttendanceService;
import service.CourseService;
import service.PaymentService;
import service.StudentService;
import service.impl.AttendanceServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.PaymentServiceImpl;
import service.impl.StudentServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class UserDashboard {

	private final User user;
	private final Scanner sc = new Scanner(System.in);

	// REAL services
	private final AttendanceService attendanceService = new AttendanceServiceImpl();
	private final CourseService courseService = new CourseServiceImpl();
	private final PaymentService paymentService = new PaymentServiceImpl(courseService);

	private final StudentService studentService = new StudentServiceImpl(paymentService, attendanceService);

	public UserDashboard(User user) {
		this.user = user;
	}

	public void start() {

		boolean running = true;

		while (running) {
			System.out.println("""
					--- USER DASHBOARD ---
					1. View Attendance %
					2. View Pending Fees
					0. Logout
					""");

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> viewAttendance();
			case 2 -> viewPendingFees();
			case 0 -> running = false;
			default -> System.out.println("Invalid choice");
			}
		}
	}

	private void viewAttendance() {

		Student student = studentService.getStudentById(user.getUserId());

		if (student == null) {
			System.out.println("Student not found");
			return;
		}

		double percentage = attendanceService.calculateAttendancePercentage(student.getStudentId());

		System.out.println("Attendance: " + percentage + "%");
	}

	private void viewPendingFees() {

		// 1️⃣ Get student
		Student student = studentService.getStudentById(user.getUserId());

		if (student == null) {
			System.out.println("Student not found");
			return;
		}

		// 2️⃣ Get course
		Course course = courseService.getCourseById(student.getCourseId());

		if (course == null) {
			System.out.println("Course not found");
			return;
		}

		BigDecimal courseFee = course.getFees();

		// 3️⃣ Sum successful payments
		List<Payment> payments = paymentService.getPaymentsByStudent(student.getStudentId());

		BigDecimal totalPaid = payments.stream().filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
				.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		// 4️⃣ Pending = Course Fee - Paid
		BigDecimal pending = courseFee.subtract(totalPaid);

		if (pending.compareTo(BigDecimal.ZERO) < 0) {
			pending = BigDecimal.ZERO;
		}

		System.out.println("Pending Fees: ₹" + pending);
	}
}
