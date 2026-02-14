package service;

import java.util.List;

import model.Teacher;

public interface TeacherService {

	void addTeacher(Teacher teacher);

    Teacher getTeacherById(Integer teacherId);
    List<Teacher> getAllTeachers();

    void updateTeacher(Integer teacherId, Teacher updatedTeacher);

    void deleteTeacher(Integer teacherId);
	
}
