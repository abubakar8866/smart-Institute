package service;

import java.util.List;

import model.Student;

public interface StudentService {

	void addStudent(Student student);

    Student getStudentById(Integer studentId);

    List<Student> getAllStudents();

    List<Student> getStudentsByCourse(Integer courseId);

    void updateStudent(Student student);

    void deleteStudent(Integer studentId);
	
}
