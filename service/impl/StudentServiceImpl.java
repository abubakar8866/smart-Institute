package service.impl;

import model.Student;
import model.StudentReport;
import service.StudentService;
import service.PaymentService;
import service.AttendanceService;
import util.ValidationUtil;
import util.FileUtil;
import util.IdGenerator;
import exception.StudentNotFoundException;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StudentServiceImpl implements StudentService {

	private static final Map<Integer, Student> studentMap = new ConcurrentHashMap<>();

	private final PaymentService paymentService;
	private final AttendanceService attendanceService;

	private static final String STUDENT_FILE = "data/students.csv";
	private static final String STUDENT_LOG = "data/student-logs.txt";

	public StudentServiceImpl(PaymentService paymentService, AttendanceService attendanceService) {

		this.paymentService = paymentService;
		this.attendanceService = attendanceService;

		loadStudentsFromFile();
	}

	private void loadStudentsFromFile() {

		File file = new File(STUDENT_FILE);
		if (!file.exists())
			return;

		String content = FileUtil.readFile(STUDENT_FILE);
		if (content.isBlank())
			return;

		String[] lines = content.split("\\R");

		for (int i = 1; i < lines.length; i++) { // skip header

			String[] parts = lines[i].split(",");
			if (parts.length != 4)
				continue;

			Integer id = Integer.parseInt(parts[0].trim());
			String name = parts[1].trim();
			String email = parts[2].trim();
			Integer courseId = Integer.parseInt(parts[3].trim());

			studentMap.put(id, new Student(id, name, email, courseId));
		}

		if (!studentMap.isEmpty()) {

			Integer maxId = studentMap.values().stream().map(Student::getStudentId).max(Integer::compareTo)
					.orElse(1000);

			IdGenerator.initialize(maxId);
		}
	}

	private void rewriteStudentsFile() {

		StringBuilder sb = new StringBuilder();

		sb.append("studentId,name,email,courseId").append(System.lineSeparator());

		for (Student student : studentMap.values()) {
			sb.append(student).append(System.lineSeparator());
		}

		FileUtil.overwriteFile(STUDENT_FILE, sb.toString());
	}

	@Override
	public void addStudent(Student student) {

		validateStudent(student);

		if (studentMap.putIfAbsent(student.getStudentId(), student) != null) {

			throw new IllegalArgumentException("Student already exists with id: " + student.getStudentId());
		}

		rewriteStudentsFile();

		FileUtil.writeToFile(STUDENT_LOG, "ADDED: " + student.getStudentId());
	}

	@Override
	public Student getStudentById(Integer studentId) {

		validateId(studentId);

		Student student = studentMap.get(studentId);

		if (student == null) {
			throw new StudentNotFoundException("Student not found with id: " + studentId);
		}

		return student;
	}

	@Override
	public List<Student> getAllStudents() {
		return new ArrayList<>(studentMap.values());
	}

	@Override
	public void deleteStudent(Integer studentId) {

		validateId(studentId);

		Student removed = studentMap.remove(studentId);

		if (removed == null) {
			throw new StudentNotFoundException("Student not found with id: " + studentId);
		}

		rewriteStudentsFile();

		FileUtil.writeToFile(STUDENT_LOG, "DELETED: " + studentId);
	}

	/* 🔥 NEW PROFESSIONAL METHOD */

	public StudentReport getStudentReport(Integer studentId) {

		Student student = getStudentById(studentId);

		BigDecimal totalPaid = paymentService.getTotalPaidByStudent(studentId);

		double attendance = attendanceService.calculateAttendancePercentage(studentId);

		return new StudentReport(student.getStudentId(), student.getName(), student.getEmail(), student.getCourseId(),
				totalPaid, attendance);
	}

	private void validateStudent(Student student) {

		if (student == null || !ValidationUtil.isNotBlank(student.getName())
				|| !ValidationUtil.isValidEmail(student.getEmail()) || student.getCourseId() == null) {

			throw new IllegalArgumentException("Invalid student data");
		}
	}

	private void validateId(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Student ID cannot be null");
		}
	}

	@Override
	public List<Student> getStudentsByCourse(Integer courseId) {

		return studentMap.values().stream().filter(student -> student.getCourseId().equals(courseId)).toList();
	}

	@Override
	public void updateStudent(Student student) {

		if (!studentMap.containsKey(student.getStudentId())) {
			throw new RuntimeException("Student not found with ID: " + student.getStudentId());
		}

		studentMap.put(student.getStudentId(), student);

		rewriteStudentsFile();
	}

	@Override
	public Map<Integer, List<Student>> getStudentsGroupedByCourse() {

		return studentMap.values().stream().collect(Collectors.groupingBy(Student::getCourseId));
	}

}
