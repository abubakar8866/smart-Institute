package service.impl;

import model.Attendance;
import service.AttendanceService;
import util.FileUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

import exception.NoRecordAttendanceException;

public class AttendanceServiceImpl implements AttendanceService {

    private final Map<Integer, List<Attendance>> attendanceMap =
            new ConcurrentHashMap<>();

    private static final String ATTENDANCE_FILE = "data/attendance.txt";

    @Override
    public void markAttendance(Integer studentId, boolean present) {

        validateStudentId(studentId);

        attendanceMap.putIfAbsent(studentId,
                new CopyOnWriteArrayList<>());

        List<Attendance> records = attendanceMap.get(studentId);

        LocalDate today = LocalDate.now();

        boolean alreadyMarked = records.stream()
                .anyMatch(a -> a.getDate().equals(today));

        if (alreadyMarked) {
            throw new IllegalArgumentException(
                    "Attendance already marked for today for student: "
                            + studentId);
        }

        Attendance attendance =
                new Attendance(studentId, today, present);

        records.add(attendance);

        // Save record to file
        FileUtil.writeToFile(ATTENDANCE_FILE,
                formatAttendance(attendance));

        // Log action
        FileUtil.writeToFile("data/attendance-logs.txt",
                "MARKED: Student " + studentId +
                        " | Date: " + today +
                        " | Present: " + present);
    }

    @Override
    public double calculateAttendancePercentage(Integer studentId) {

        validateStudentId(studentId);

        List<Attendance> records =
                attendanceMap.get(studentId);

        if (records == null || records.isEmpty()) {
            throw new NoRecordAttendanceException(
                    "No attendance record found for student ID: "
                            + studentId);
        }

        long presentCount = records.stream()
                .filter(Attendance::getPresent)
                .count();

        double percentage =
                (presentCount * 100.0) / records.size();

        // Generate report entry
        FileUtil.writeToFile("data/attendance-report.txt",
                "Student: " + studentId +
                        " | Attendance: " +
                        String.format("%.2f", percentage) + "%");

        return percentage;
    }

    private void validateStudentId(Integer studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException(
                    "StudentId cannot be null");
        }
    }

    private String formatAttendance(Attendance attendance) {
        return attendance.getStudentId() + "," +
                attendance.getDate() + "," +
                attendance.getPresent();
    }
}
