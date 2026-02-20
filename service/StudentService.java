package service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Student;

public interface StudentService {

	void addStudent(Student student);

    Student getStudentById(Integer studentId);

    List<Student> getAllStudents();

    List<Student> getStudentsByCourse(Integer courseId);

    void updateStudent(Student student);

    void deleteStudent(Integer studentId);
    
    Map<Integer, List<Student>> getStudentsGroupedByCourse();
    
    Student getStudentByUserId(Integer userId);
    
    Set<Integer> getAllLinkedUserIds();
	
}
