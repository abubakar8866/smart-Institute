package service;

import java.util.List;
import model.Course;

public interface CourseService {

    void addCourse(Course course);

    Course getCourseById(Integer courseId);

    List<Course> getAllCourses();

    void updateCourse(Integer courseId, Course updatedCourse);

    void deleteCourse(Integer courseId);
}
