package service.impl;

import model.Course;
import service.CourseService;
import util.ValidationUtil;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import exception.CourseNotFoundException;

public class CourseServiceImpl implements CourseService {

    private final Map<Integer, Course> courseMap = new ConcurrentHashMap<>();

    private static final String COURSE_FILE = "data/courses.csv";

    public CourseServiceImpl() {
        loadCoursesFromFile();
    }
    
    private void loadCoursesFromFile() {

        File file = new File(COURSE_FILE);

        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }

                String[] parts = line.split(",");

                Integer id = Integer.parseInt(parts[0].trim());
                String name = parts[1];
                Integer duration = Integer.parseInt(parts[2]);
                BigDecimal fees = new BigDecimal(parts[3]);

                Course course = new Course(id, name, duration, fees);

                courseMap.put(id, course);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading courses file", e);
        }
    }

    private void rewriteCourseFile() {

        StringBuilder builder = new StringBuilder();

        builder.append("courseId,courseName,duration,fees")
               .append(System.lineSeparator());

        for (Course course : courseMap.values()) {

            builder.append(course.getCourseId())
                   .append(",")
                   .append(course.getCourseName())
                   .append(",")
                   .append(course.getDuration())
                   .append(",")
                   .append(course.getFees().toPlainString())
                   .append(System.lineSeparator());
        }

        FileUtil.overwriteFile(COURSE_FILE, builder.toString());
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
    
    @Override
    public void addCourse(Course course) {

        validateCourse(course);

        if (courseMap.putIfAbsent(course.getCourseId(), course) != null) {
            throw new IllegalArgumentException(
                    "Course already exists with ID: " + course.getCourseId());
        }

        rewriteCourseFile();

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
        
        rewriteCourseFile();

        FileUtil.writeToFile("data/course-logs.txt",
                "UPDATED: " + updatedCourse.getCourseName());
    }

    @Override
    public void deleteCourse(Integer courseId) {

        validateCourseId(courseId);

        Course removed = courseMap.remove(courseId);
        
        rewriteCourseFile();

        if (removed == null) {
            throw new CourseNotFoundException(
                    "Course not found with ID: " + courseId);
        }

        FileUtil.writeToFile("data/course-logs.txt",
                "DELETED: " + removed.getCourseName());
    }

}
