package service.impl;

import model.Student;
import service.StudentService;
import util.ValidationUtil;
import util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import exception.StudentNotFoundException;

public class StudentServiceImpl implements StudentService {

    private final Map<Integer, Student> studentMap =
            new ConcurrentHashMap<>();

    private static final String STUDENT_FILE = "data/students.txt";
    private static final String STUDENT_LOG = "data/student-logs.txt";

    @Override
    public void addStudent(Student student) {

        validateStudent(student);

        if (studentMap.putIfAbsent(
                student.getStudentId(), student) != null) {

            throw new IllegalArgumentException(
                    "Student already exists with id: "
                            + student.getStudentId());
        }

        FileUtil.writeToFile(STUDENT_FILE, student.toString());

        FileUtil.writeToFile(STUDENT_LOG,
                "ADDED: " + student.getStudentId());
    }

    @Override
    public Student getStudentById(Integer studentId) {

        validateId(studentId);

        Student student = studentMap.get(studentId);

        if (student == null) {
            throw new StudentNotFoundException(
                    "Student not found with id: " + studentId);
        }

        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    @Override
    public List<Student> getStudentsByCourse(Integer courseId) {

        if (courseId == null) {
            throw new IllegalArgumentException(
                    "Course ID cannot be null");
        }

        return studentMap.values()
                .stream()
                .filter(s -> courseId.equals(s.getCourseId()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateStudent(Student updatedStudent) {

        validateStudent(updatedStudent);

        studentMap.compute(
                updatedStudent.getStudentId(),
                (id, existing) -> {

                    if (existing == null) {
                        throw new StudentNotFoundException(
                                "Student not found with id: " + id);
                    }

                    existing.setName(updatedStudent.getName());
                    existing.setEmail(updatedStudent.getEmail());
                    existing.setCourseId(updatedStudent.getCourseId());
                    existing.setFeesPaid(updatedStudent.getFeesPaid());
                    existing.setAttendancePercentage(
                            updatedStudent.getAttendancePercentage());

                    return existing;
                }
        );

        FileUtil.writeToFile(STUDENT_LOG,
                "UPDATED: " + updatedStudent.getStudentId());
    }

    @Override
    public void deleteStudent(Integer studentId) {

        validateId(studentId);

        Student removed = studentMap.remove(studentId);

        if (removed == null) {
            throw new StudentNotFoundException(
                    "Student not found with id: " + studentId);
        }

        FileUtil.writeToFile(STUDENT_LOG,
                "DELETED: " + studentId);
    }

    @Override
    public Map<Integer, List<Student>> getStudentsGroupedByCourse() {

        return studentMap.values()
                .stream()
                .collect(Collectors.groupingBy(Student::getCourseId));
    }
    
    private void validateStudent(Student student) {

        if (student == null ||
            !ValidationUtil.isNotBlank(student.getName()) ||
            !ValidationUtil.isValidEmail(student.getEmail()) ||
            student.getCourseId() == null) {

            throw new IllegalArgumentException(
                    "Invalid student data");
        }
    }

    private void validateId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Student ID cannot be null");
        }
    } 
    
    
}
