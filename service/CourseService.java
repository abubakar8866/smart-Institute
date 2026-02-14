package service;

import java.util.Map;
import model.Course;

public interface CourseService {

    void addCourse(Course course);

    Course getCourseById(Integer courseId);

    Map<Integer, Course> getAllCourses();

    void updateCourse(Integer courseId, Course updatedCourse);

    void deleteCourse(Integer courseId);
}
