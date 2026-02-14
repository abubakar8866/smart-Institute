package model;

import util.IdGenerator;

public class Student {

    private final Integer studentId;
    private String name;
    private String email;
    private Integer courseId;
    private Double feesPaid;
    private Double attendancePercentage;

    public Student(String name,
                   String email,
                   Integer courseId,
                   Double feesPaid,
                   Double attendancePercentage) {

        this.studentId = IdGenerator.generateId();
        this.name = name;
        this.email = email;
        this.courseId = courseId;
        this.feesPaid = feesPaid;
        this.attendancePercentage = attendancePercentage;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Double getFeesPaid() {
        return feesPaid;
    }

    public void setFeesPaid(Double feesPaid) {
        this.feesPaid = feesPaid;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    @Override
    public String toString() {
        return studentId + "," +
               name + "," +
               email + "," +
               courseId + "," +
               feesPaid + "," +
               attendancePercentage;
    }
}
