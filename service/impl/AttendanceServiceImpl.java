package service.impl;

import model.Attendance;
import service.AttendanceService;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

import exception.NoRecordAttendanceException;

public class AttendanceServiceImpl implements AttendanceService {

	private final Map<Integer, List<Attendance>> attendanceMap = new ConcurrentHashMap<>();

	private static final String ATTENDANCE_FILE = "data/attendance.csv";

	private void loadAttendanceFromFile() {

		File file = new File(ATTENDANCE_FILE);

		if (!file.exists() || file.length() == 0) {
			return; // nothing to load
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			String line;
			boolean isFirstLine = true;

			while ((line = reader.readLine()) != null) {

				// Skip header
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}

				String[] parts = line.trim().split(",");

				if (parts.length != 3)
					continue;

				Integer studentId = Integer.parseInt(parts[0]);
				LocalDate date = LocalDate.parse(parts[1]);
				Boolean present = Boolean.parseBoolean(parts[2]);

				Attendance attendance = new Attendance(studentId, date, present);

				attendanceMap.computeIfAbsent(studentId, k -> new CopyOnWriteArrayList<>()).add(attendance);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error loading attendance file", e);
		}
	}

	public AttendanceServiceImpl() {
		loadAttendanceFromFile();
	}

	private void validateStudentId(Integer studentId) {
		if (studentId == null) {
			throw new IllegalArgumentException("StudentId cannot be null");
		}
	}

	private void rewriteAttendanceFile() {

		StringBuilder builder = new StringBuilder();

		// Add header
		builder.append("studentId,date,present").append(System.lineSeparator());

		for (Map.Entry<Integer, List<Attendance>> entry : attendanceMap.entrySet()) {

			for (Attendance attendance : entry.getValue()) {

				builder.append(attendance.getStudentId()).append(",").append(attendance.getDate()).append(",")
						.append(attendance.getPresent()).append(System.lineSeparator());
			}
		}

		FileUtil.overwriteFile(ATTENDANCE_FILE, builder.toString());
	}

	@Override
	public void markAttendance(Integer studentId, boolean present) {

		validateStudentId(studentId);

		attendanceMap.putIfAbsent(studentId, new CopyOnWriteArrayList<>());

		List<Attendance> records = attendanceMap.get(studentId);

		LocalDate today = LocalDate.now();

		boolean alreadyMarked = records.stream().anyMatch(a -> a.getDate().equals(today));

		if (alreadyMarked) {
			throw new IllegalArgumentException("Attendance already marked for today for student: " + studentId);
		}

		Attendance attendance = new Attendance(studentId, today, present);

		records.add(attendance);

		rewriteAttendanceFile();

		// Log action
		FileUtil.writeToFile("data/attendance-logs.txt",
				"MARKED: Student " + studentId + " | Date: " + today + " | Present: " + present);
	}

	@Override
	public double calculateAttendancePercentage(Integer studentId) {

		validateStudentId(studentId);

		List<Attendance> records = attendanceMap.get(studentId);

		if (records == null || records.isEmpty()) {
			throw new NoRecordAttendanceException("No attendance record found for student ID: " + studentId);
		}

		long presentCount = records.stream().filter(Attendance::getPresent).count();

		return (presentCount * 100.0) / records.size();
	}

	@Override
	public List<Integer> getStudentsBelowAttendance(double threshold) {

		return attendanceMap.entrySet().stream().filter(e -> {
			long present = e.getValue().stream().filter(Attendance::getPresent).count();

			int total = e.getValue().size();
			if (total == 0)
				return false;
			double percent = (present * 100.0) / total;

			return percent < threshold;
		}).map(Map.Entry::getKey).toList();
	}
	
	@Override
	public boolean hasAttendance(Integer studentId) {
	    return attendanceMap.containsKey(studentId);
	}

}
