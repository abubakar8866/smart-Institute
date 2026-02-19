package model;

import java.time.LocalDate;

public class Attendance {

    private final Integer studentId;
    private LocalDate date;
    private boolean present;

    public Attendance(Integer studentId, LocalDate date, boolean present) {
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

    public boolean getPresent() {
        return present;
    }
    
    public void setPresent(boolean present) {
        this.present = present;
    }

    @Override
    public String toString() {
        return "Attendance [studentId=" + studentId +
               ", date=" + date + ", present=" + present + "]";
    }
}
