package model;

import java.time.LocalDate;

public class Attendance {

    private final Integer studentId;
    private LocalDate date;
    private Boolean present;

    public Attendance(Integer studentId, LocalDate date, Boolean present) {
        this.studentId = studentId;
        this.date = date;
        this.present = present;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getPresent() {
        return present;
    }
    
    public void setPresent(Boolean present) {
        this.present = present;
    }

    @Override
    public String toString() {
        return "Attendance [studentId=" + studentId +
               ", date=" + date + ", present=" + present + "]";
    }
}
