package model;

import java.math.BigDecimal;

public class StudentReport {

    private final Integer studentId;
    private final String name;
    private final String email;
    private final Integer courseId;
    private final BigDecimal totalPaid;
    private final double attendancePercentage;

    public StudentReport(Integer studentId,
                         String name,
                         String email,
                         Integer courseId,
                         BigDecimal totalPaid,
                         double attendancePercentage) {

        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.courseId = courseId;
        this.totalPaid = totalPaid;
        this.attendancePercentage = attendancePercentage;
    }

    @Override
    public String toString() {
        return "StudentReport{" +
                "id=" + studentId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", courseId=" + courseId +
                ", totalPaid=" + totalPaid +
                ", attendance=" + attendancePercentage +
                '}';
    }
}
