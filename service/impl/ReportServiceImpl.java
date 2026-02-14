package service.impl;

import model.Student;
import model.Teacher;
import model.Payment;
import service.*;
import util.FileUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

public class ReportServiceImpl implements ReportService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );

    public ReportServiceImpl(StudentService studentService,
                             TeacherService teacherService,
                             AttendanceService attendanceService,
                             PaymentService paymentService) {

        this.studentService = studentService;
        this.teacherService = teacherService;
        this.attendanceService = attendanceService;
        this.paymentService = paymentService;
    }

    @Override
    public void generateStudentReportAsync() {

        System.out.println("Student report generation started...");

        executor.submit(() -> {

            StringBuilder report = new StringBuilder(2048);

            report.append("===== STUDENT REPORT =====")
                    .append(System.lineSeparator())
                    .append("Generated At: ")
                    .append(LocalDateTime.now())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());

            List<Student> students = studentService.getAllStudents();

            for (Student student : students) {

                BigDecimal attendancePercentage;

                try {
                    double percentage =
                            attendanceService.calculateAttendancePercentage(
                                    student.getStudentId());

                    attendancePercentage =
                            BigDecimal.valueOf(percentage);

                } catch (Exception e) {

                    attendancePercentage = BigDecimal.ZERO;

                    FileUtil.writeToFile(
                            "logs/error.txt",
                            "Attendance error for studentId: "
                                    + student.getStudentId()
                    );
                }

                List<Payment> payments =
                        paymentService.getPaymentsByStudent(
                                student.getStudentId());

                BigDecimal totalPaid = payments.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                report.append("--------------------------------")
                        .append(System.lineSeparator())
                        .append("ID              : ")
                        .append(student.getStudentId())
                        .append(System.lineSeparator())
                        .append("Name            : ")
                        .append(student.getName())
                        .append(System.lineSeparator())
                        .append("Email           : ")
                        .append(student.getEmail())
                        .append(System.lineSeparator())
                        .append("Attendance %    : ")
                        .append(attendancePercentage)
                        .append(System.lineSeparator())
                        .append("Total Fees Paid : ")
                        .append(totalPaid)
                        .append(System.lineSeparator());
            }

            report.append("===== STUDENT REPORT END =====")
                    .append(System.lineSeparator());

            FileUtil.writeToFile(
                    "reports/student-report.txt",
                    report.toString()
            );

            System.out.println("Student report generation completed.");
        });
    }

    @Override
    public void generateTeacherReportAsync() {

        System.out.println("Teacher report generation started...");

        executor.submit(() -> {

            StringBuilder report = new StringBuilder(1024);

            report.append("===== TEACHER REPORT =====")
                    .append(System.lineSeparator())
                    .append("Generated At: ")
                    .append(LocalDateTime.now())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());

            List<Teacher> teachers = teacherService.getAllTeachers();

            for (Teacher teacher : teachers) {

                report.append("--------------------------------")
                        .append(System.lineSeparator())
                        .append("ID      : ")
                        .append(teacher.getTeacherId())
                        .append(System.lineSeparator())
                        .append("Name    : ")
                        .append(teacher.getName())
                        .append(System.lineSeparator())
                        .append("Subject : ")
                        .append(teacher.getSubject())
                        .append(System.lineSeparator())
                        .append("Salary  : ")
                        .append(teacher.getSalary())
                        .append(System.lineSeparator());
            }

            report.append("===== TEACHER REPORT END =====")
                    .append(System.lineSeparator());

            FileUtil.writeToFile(
                    "reports/teacher-report.txt",
                    report.toString()
            );

            System.out.println("Teacher report generation completed.");
        });
    }

    public void shutdown() {

        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
