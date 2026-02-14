package model;

import util.IdGenerator;

public class Teacher {

    private final Integer teacherId;
    private String name;
    private String subject;
    private Double salary;

    // ID generated inside constructor
    public Teacher(String name,
                   String subject,
                   Double salary) {

        this.teacherId = IdGenerator.generateId();
        this.name = name;
        this.subject = subject;
        this.salary = salary;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return teacherId + "," +
               name + "," +
               subject + "," +
               salary;
    }
}
