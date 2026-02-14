package service.impl;

import model.Teacher;
import service.TeacherService;
import util.ValidationUtil;
import util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import exception.TeacherNotFoundException;

public class TeacherServiceImpl implements TeacherService {

    private final Map<Integer, Teacher> teacherMap =
            new ConcurrentHashMap<>();

    private static final String TEACHER_FILE = "data/teachers.txt";
    private static final String TEACHER_LOG = "data/teacher-logs.txt";

    @Override
    public void addTeacher(Teacher teacher) {

        validateTeacher(teacher);

        if (teacherMap.putIfAbsent(
                teacher.getTeacherId(), teacher) != null) {

            throw new IllegalArgumentException(
                    "Teacher already exists with id: "
                            + teacher.getTeacherId());
        }

        FileUtil.writeToFile(TEACHER_FILE, teacher.toString());

        FileUtil.writeToFile(TEACHER_LOG,
                "ADDED: " + teacher.getTeacherId());
    }

    @Override
    public Teacher getTeacherById(Integer teacherId) {

        validateId(teacherId);

        Teacher teacher = teacherMap.get(teacherId);

        if (teacher == null) {
            throw new TeacherNotFoundException(
                    "Teacher not found with id: " + teacherId);
        }

        return teacher;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return new ArrayList<>(teacherMap.values());
    }

    @Override
    public void updateTeacher(Integer teacherId,
                              Teacher updatedTeacher) {

        validateTeacher(updatedTeacher);

        teacherMap.compute(teacherId, (id, existing) -> {

            if (existing == null) {
                throw new TeacherNotFoundException(
                        "Teacher not found with id: " + teacherId);
            }

            existing.setName(updatedTeacher.getName());
            existing.setSubject(updatedTeacher.getSubject());
            existing.setSalary(updatedTeacher.getSalary());

            return existing;
        });

        FileUtil.writeToFile(TEACHER_LOG,
                "UPDATED: " + teacherId);
    }

    @Override
    public void deleteTeacher(Integer teacherId) {

        validateId(teacherId);

        Teacher removed = teacherMap.remove(teacherId);

        if (removed == null) {
            throw new TeacherNotFoundException(
                    "Teacher not found with id: " + teacherId);
        }

        FileUtil.writeToFile(TEACHER_LOG,
                "DELETED: " + teacherId);
    }  
    
    @Override
    public Map<Integer, String> getTeacherCourseMapping() {

        return teacherMap.values()
                .stream()
                .collect(Collectors.toMap(
                        Teacher::getTeacherId,
                        Teacher::getSubject
                ));
    }

    private void validateTeacher(Teacher teacher) {

        if (teacher == null ||
            !ValidationUtil.isNotBlank(teacher.getName()) ||
            !ValidationUtil.isNotBlank(teacher.getSubject()) ||
            teacher.getSalary() == null ||
            teacher.getSalary() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid teacher data");
        }
    }

    private void validateId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Teacher ID cannot be null");
        }
    }
}
