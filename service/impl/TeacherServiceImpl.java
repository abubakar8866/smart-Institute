package service.impl;

import model.Teacher;
import service.TeacherService;
import util.ValidationUtil;
import util.FileUtil;
import util.IdGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import exception.TeacherNotFoundException;

public class TeacherServiceImpl implements TeacherService {

	private static final Map<Integer, Teacher> teacherMap = new ConcurrentHashMap<>();

	static {
		loadTeachersFromFile();
	}

	private static final String TEACHER_FILE = "data/teachers.csv";
	private static final String TEACHER_LOG = "data/teacher-logs.txt";

	private static void loadTeachersFromFile() {

		try {

			File file = new File(TEACHER_FILE);
			if (!file.exists())
				return;

			String content = FileUtil.readFile(TEACHER_FILE);
			if (content.isBlank())
				return;

			String[] lines = content.split("\\R");

			for (int i = 1; i < lines.length; i++) {

				String[] parts = lines[i].split(",");

				if (parts.length != 4)
					continue;

				Integer id = Integer.parseInt(parts[0].trim());
				String name = parts[1].trim();
				String subject = parts[2].trim();
				BigDecimal salary = new BigDecimal(parts[3].trim());

				Teacher teacher = new Teacher(id, name, subject, salary);
				teacherMap.put(id, teacher);
			}

			if (!teacherMap.isEmpty()) {

				Integer maxId = teacherMap.keySet().stream().max(Integer::compareTo).orElse(1000);

				IdGenerator.initialize(maxId);
			}

		} catch (Exception e) {
			System.out.println("Error loading teachers: " + e.getMessage());
		}
	}

	private static void rewriteTeachersFile() {

		StringBuilder sb = new StringBuilder();

		sb.append("teacherId,name,subject,salary").append(System.lineSeparator());

		for (Teacher teacher : teacherMap.values()) {

			sb.append(teacher.getTeacherId()).append(",").append(teacher.getName()).append(",")
					.append(teacher.getSubject()).append(",").append(teacher.getSalary())
					.append(System.lineSeparator());
		}

		FileUtil.overwriteFile(TEACHER_FILE, sb.toString());
	}

	@Override
	public void addTeacher(Teacher teacher) {

		validateTeacher(teacher);

		if (teacherMap.putIfAbsent(teacher.getTeacherId(), teacher) != null) {

			throw new IllegalArgumentException("Teacher already exists with id: " + teacher.getTeacherId());
		}

		rewriteTeachersFile();

		FileUtil.writeToFile(TEACHER_LOG, "ADDED: " + teacher.getTeacherId());
	}

	@Override
	public Teacher getTeacherById(Integer teacherId) {

		validateId(teacherId);

		return Optional.ofNullable(teacherMap.get(teacherId))
				.orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + teacherId));

	}

	@Override
	public List<Teacher> getAllTeachers() {
		return new ArrayList<>(teacherMap.values());
	}

	@Override
	public void updateTeacher(Integer teacherId, Teacher updatedTeacher) {

		validateTeacher(updatedTeacher);

		teacherMap.compute(teacherId, (id, existing) -> {

			if (existing == null) {
				throw new TeacherNotFoundException("Teacher not found with id: " + teacherId);
			}

			existing.setName(updatedTeacher.getName());
			existing.setSubject(updatedTeacher.getSubject());
			existing.setSalary(updatedTeacher.getSalary());

			return existing;
		});

		rewriteTeachersFile();

		FileUtil.writeToFile(TEACHER_LOG, "UPDATED: " + teacherId);
	}

	@Override
	public void deleteTeacher(Integer teacherId) {

		validateId(teacherId);

		Teacher removed = teacherMap.remove(teacherId);

		if (removed == null) {
			throw new TeacherNotFoundException("Teacher not found with id: " + teacherId);
		}

		rewriteTeachersFile();

		FileUtil.writeToFile(TEACHER_LOG, "DELETED: " + teacherId);
	}

	@Override
	public Map<Integer, String> getTeacherCourseMapping() {

		return teacherMap.values().stream().collect(Collectors.toMap(Teacher::getTeacherId, Teacher::getSubject));
	}

	private void validateTeacher(Teacher teacher) {

		if (teacher == null || !ValidationUtil.isNotBlank(teacher.getName())
				|| !ValidationUtil.isNotBlank(teacher.getSubject()) || teacher.getSalary() == null
				|| teacher.getSalary() == null || teacher.getSalary().compareTo(BigDecimal.ZERO) <= 0) {

			throw new IllegalArgumentException("Invalid teacher data");
		}
	}

	private void validateId(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Teacher ID cannot be null");
		}
	}
}
