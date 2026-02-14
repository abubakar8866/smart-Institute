package service.impl;

import model.Course;
import service.CourseService;
import util.ValidationUtil;
import util.FileUtil;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import exception.CourseNotFoundException;

public class CourseServiceImpl implements CourseService {

    private final Map<Integer, Course> courseMap = new ConcurrentHashMap<>();

    private static final String COURSE_FILE = "data/courses.txt";

    @Override
    public void addCourse(Course course) {

        validateCourse(course);

        if (courseMap.putIfAbsent(course.getCourseId(), course) != null) {
            throw new IllegalArgumentException(
                    "Course already exists with ID: " + course.getCourseId());
        }

        // Save to file
        FileUtil.writeToFile(COURSE_FILE, formatCourse(course));

        FileUtil.writeToFile("data/course-logs.txt",
                "ADDED: " + course.getCourseName());
    }

    @Override
    public Course getCourseById(Integer courseId) {

        validateCourseId(courseId);

        Course course = courseMap.get(courseId);

        if (course == null) {
            throw new CourseNotFoundException(
                    "Course not found with ID: " + courseId);
        }

        return course;
    }

    @Override
    public Map<Integer, Course> getAllCourses() {
        return Collections.unmodifiableMap(courseMap);
    }

    @Override
    public void updateCourse(Integer courseId, Course updatedCourse) {

        validateCourse(updatedCourse);

        if (!courseId.equals(updatedCourse.getCourseId())) {
            throw new IllegalArgumentException("Course ID mismatch");
        }

        if (!courseMap.containsKey(courseId)) {
            throw new CourseNotFoundException(
                    "Course not found with ID: " + courseId);
        }

        courseMap.put(courseId, updatedCourse);

        FileUtil.writeToFile("data/course-logs.txt",
                "UPDATED: " + updatedCourse.getCourseName());
    }

    @Override
    public void deleteCourse(Integer courseId) {

        validateCourseId(courseId);

        Course removed = courseMap.remove(courseId);

        if (removed == null) {
            throw new CourseNotFoundException(
                    "Course not found with ID: " + courseId);
        }

        FileUtil.writeToFile("data/course-logs.txt",
                "DELETED: " + removed.getCourseName());
    }

    private void validateCourse(Course course) {

        if (course == null ||
                course.getCourseId() == null ||
                !ValidationUtil.isNotBlank(course.getCourseName()) ||
                course.getDuration() == null ||
                course.getFees() == null) {

            throw new IllegalArgumentException("Invalid course data");
        }
    }

    private void validateCourseId(Integer courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("CourseId cannot be null");
        }
    }

    private String formatCourse(Course course) {
        return course.getCourseId() + "," +
                course.getCourseName() + "," +
                course.getDuration() + "," +
                course.getFees();
    }
}
