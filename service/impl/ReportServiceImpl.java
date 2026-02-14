package service.impl;

import model.Student;
import model.Teacher;
import model.Payment;
import service.*;
import util.FileUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    // ================= EXISTING REPORTS =================

    @Override
    public void generateStudentReportAsync() {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder(2048);

            report.append("===== STUDENT REPORT =====\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            for (Student student : studentService.getAllStudents()) {

                double attendance =
                        attendanceService.calculateAttendancePercentage(
                                student.getStudentId());

                BigDecimal totalPaid =
                        paymentService.getPaymentsByStudent(
                                        student.getStudentId())
                                .stream()
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                report.append("--------------------------------\n")
                      .append("ID           : ").append(student.getStudentId()).append("\n")
                      .append("Name         : ").append(student.getName()).append("\n")
                      .append("Email        : ").append(student.getEmail()).append("\n")
                      .append("Attendance % : ").append(attendance).append("\n")
                      .append("Fees Paid    : ").append(totalPaid).append("\n");
            }

            FileUtil.writeToFile(
                    "reports/student-report.txt",
                    report.toString()
            );
        });
    }

    @Override
    public void generateTeacherReportAsync() {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder(1024);

            report.append("===== TEACHER REPORT =====\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            for (Teacher teacher : teacherService.getAllTeachers()) {

                report.append("--------------------------------\n")
                      .append("ID      : ").append(teacher.getTeacherId()).append("\n")
                      .append("Name    : ").append(teacher.getName()).append("\n")
                      .append("Subject : ").append(teacher.getSubject()).append("\n")
                      .append("Salary  : ").append(teacher.getSalary()).append("\n");
            }

            FileUtil.writeToFile(
                    "reports/teacher-report.txt",
                    report.toString()
            );
        });
    }

    // ================= NEW REPORTS =================

    @Override
    public void generateStudentByCourseReportAsync() {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder();

            report.append("===== STUDENT BY COURSE REPORT =====\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            Map<Integer, List<Student>> grouped =
                    studentService.getStudentsGroupedByCourse();

            grouped.forEach((courseId, students) -> {
                report.append("Course ID: ").append(courseId).append("\n");
                students.forEach(s ->
                        report.append("  - ")
                              .append(s.getStudentId())
                              .append(" : ")
                              .append(s.getName())
                              .append("\n")
                );
                report.append("\n");
            });

            FileUtil.writeToFile(
                    "reports/student-by-course-report.txt",
                    report.toString()
            );
        });
    }

    @Override
    public void generatePendingFeesReportAsync() {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder();

            report.append("===== PENDING FEES REPORT =====\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            paymentService.getStudentsWithPendingFees()
                    .forEach(id ->
                            report.append("Student ID: ")
                                  .append(id)
                                  .append("\n")
                    );

            FileUtil.writeToFile(
                    "reports/pending-fees-report.txt",
                    report.toString()
            );
        });
    }

    @Override
    public void generateLowAttendanceReportAsync(double threshold) {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder();

            report.append("===== LOW ATTENDANCE REPORT =====\n")
                  .append("Threshold: ").append(threshold).append("%\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            attendanceService
                    .getStudentsBelowAttendance(threshold)
                    .forEach(id ->
                            report.append("Student ID: ")
                                  .append(id)
                                  .append("\n")
                    );

            FileUtil.writeToFile(
                    "reports/low-attendance-report.txt",
                    report.toString()
            );
        });
    }

    @Override
    public void generateTeacherCourseMappingReportAsync() {

        executor.submit(() -> {

            StringBuilder report = new StringBuilder();

            report.append("===== TEACHER COURSE MAPPING =====\n")
                  .append("Generated At: ")
                  .append(LocalDateTime.now())
                  .append("\n\n");

            teacherService.getTeacherCourseMapping()
                    .forEach((teacher, course) ->
                            report.append(teacher)
                                  .append(" -> ")
                                  .append(course)
                                  .append("\n")
                    );

            FileUtil.writeToFile(
                    "reports/teacher-course-mapping-report.txt",
                    report.toString()
            );
        });
    }

    // ================= ALL =================

    @Override
    public void generateAllReportsAsync() {
        generateStudentReportAsync();
        generateTeacherReportAsync();
        generateStudentByCourseReportAsync();
        generatePendingFeesReportAsync();
        generateLowAttendanceReportAsync(75);
        generateTeacherCourseMappingReportAsync();
    }

    @Override
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
