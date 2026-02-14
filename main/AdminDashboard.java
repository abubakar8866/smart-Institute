package main;

import model.Course;
import model.Student;
import model.Teacher;
import service.*;
import service.impl.*;

import java.math.BigDecimal;
import java.util.Scanner;

public class AdminDashboard {

	private final Scanner sc = new Scanner(System.in);

	private final StudentService studentService = new StudentServiceImpl();
	private final TeacherService teacherService = new TeacherServiceImpl();
	private final CourseService courseService = new CourseServiceImpl();
	private final AttendanceService attendanceService = new AttendanceServiceImpl();
	PaymentService paymentService = new PaymentServiceImpl(courseService);

	private final ReportService reportService = new ReportServiceImpl(studentService, teacherService, attendanceService,paymentService);

	public void start() {

		boolean running = true;

		while (running) {
			System.out.println("""
					--- ADMIN DASHBOARD ---
					1. Add Student
					2. Add Teacher
					3. Add Course
					4. Generate Reports
					0. Logout
					""");

			System.out.print("Choose: ");
			int choice = Integer.parseInt(sc.nextLine());

			switch (choice) {
			case 1 -> addStudent();
			case 2 -> addTeacher();
			case 3 -> addCourse();
			case 4 -> generateReports();
			case 0 -> running = false;
			default -> System.out.println("Invalid choice");
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

		Student student = new Student(name, email, courseId, 0.0, 0.0);

		studentService.addStudent(student);
		System.out.println("✅ Student added successfully");
	}

	private void addTeacher() {

		System.out.print("Name: ");
		String name = sc.nextLine();

		System.out.print("Subject: ");
		String subject = sc.nextLine();

		System.out.print("Salary: ");
		Double salary = Double.parseDouble(sc.nextLine());

		Teacher teacher = new Teacher(name, subject, salary);

		teacherService.addTeacher(teacher);
		System.out.println("✅ Teacher added successfully");
	}

	private void addCourse() {

		System.out.print("Course ID: ");
		Integer courseId = Integer.parseInt(sc.nextLine());

		System.out.print("Course name: ");
		String name = sc.nextLine();

		System.out.print("Duration (months): ");
		Integer duration = Integer.parseInt(sc.nextLine());

		System.out.print("Fees: ");
		BigDecimal fees = new BigDecimal(sc.nextLine());

		Course course = new Course(courseId, name, duration, fees);

		courseService.addCourse(course);
		System.out.println("✅ Course added successfully");
	}

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
	                double threshold =
	                        Double.parseDouble(sc.nextLine());
	                reportService.generateLowAttendanceReportAsync(threshold);
	            }

	            case 6 -> reportService.generateTeacherCourseMappingReportAsync();

	            case 7 -> reportService.generateAllReportsAsync();

	            case 0 -> running = false;

	            default -> System.out.println("❌ Invalid choice");
	        }

	        if (choice != 0) {
	            System.out.println("📊 Selected report(s) are generating in background...");
	        }
	    }
	}


}
