package model;

import java.math.BigDecimal;

public class Course {

    private final Integer courseId;
    private String courseName;
    private Integer duration;
    private BigDecimal fees;
    private Integer teacherId;

    public Course(Integer courseId, String courseName,
                  Integer duration, BigDecimal fees,
                  Integer teacherId) {

        this.courseId = courseId;
        this.courseName = courseName;
        this.duration = duration;
        this.fees = fees;
        this.teacherId = teacherId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "Course [courseId=" + courseId +
                ", courseName=" + courseName +
                ", teacherId=" + teacherId + "]";
    }
}