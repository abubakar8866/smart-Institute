package service.impl;

import model.Payment;
import model.PaymentMode;
import model.PaymentStatus;
import service.CourseService;
import service.PaymentService;
import util.FileUtil;
import util.IdGenerator;
import exception.PaymentNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PaymentServiceImpl implements PaymentService {

	private final Map<Integer, Payment> paymentMap = new ConcurrentHashMap<>();

	private final CourseService courseService;

	private static final String PAYMENT_FILE = "data/payments.csv";

	public PaymentServiceImpl(CourseService courseService) {
		this.courseService = courseService;
		loadPaymentsFromFile();
	}

	private void loadPaymentsFromFile() {

		File file = new File(PAYMENT_FILE);

		if (!file.exists() || file.length() == 0) {
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			String line;
			boolean isFirstLine = true;

			while ((line = reader.readLine()) != null) {

				if (isFirstLine) {
					isFirstLine = false;
					continue; // skip header
				}

				String[] parts = line.split(",");

				if (parts.length != 7)
					continue;

				Integer paymentId = Integer.parseInt(parts[0].trim());
				Integer studentId = Integer.parseInt(parts[1].trim());
				Integer courseId = Integer.parseInt(parts[2].trim());
				BigDecimal amount = new BigDecimal(parts[3].trim());
				PaymentMode mode = PaymentMode.valueOf(parts[4].trim());
				PaymentStatus status = PaymentStatus.valueOf(parts[5].trim());
				LocalDateTime date = LocalDateTime.parse(parts[6].trim());

				Payment payment = new Payment(paymentId, studentId, courseId, amount, mode, status, date);

				paymentMap.put(paymentId, payment);
			}

			if (!paymentMap.isEmpty()) {

				Integer maxId = paymentMap.values().stream().map(Payment::getPaymentId).max(Integer::compareTo)
						.orElse(1000);

				IdGenerator.initialize(maxId);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error loading payments file", e);
		}
	}

	private void rewritePaymentFile() {

		StringBuilder builder = new StringBuilder();

		builder.append("paymentId,studentId,courseId,amount,paymentMode,status,paymentDate")
				.append(System.lineSeparator());

		for (Payment payment : paymentMap.values()) {

			builder.append(payment.getPaymentId()).append(",").append(payment.getStudentId()).append(",")
					.append(payment.getCourseId()).append(",").append(payment.getAmount().toPlainString()).append(",")
					.append(payment.getPaymentMode()).append(",").append(payment.getStatus()).append(",")
					.append(payment.getPaymentDate()).append(System.lineSeparator());
		}

		FileUtil.overwriteFile(PAYMENT_FILE, builder.toString());
	}

	/* ---------------- CORE METHODS ---------------- */

	@Override
	public void addPayment(Payment payment) {
		validatePayment(payment);

		if (paymentMap.putIfAbsent(payment.getPaymentId(), payment) != null) {
			throw new IllegalArgumentException("Payment already exists with ID: " + payment.getPaymentId());
		}

		rewritePaymentFile();
	}

	@Override
	public Payment getPaymentById(Integer paymentId) {
		validatePaymentId(paymentId);

		Payment payment = paymentMap.get(paymentId);
		if (payment == null) {
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
		}
		return payment;
	}

	@Override
	public List<Payment> getPaymentsByStudent(Integer studentId) {
		if (studentId == null) {
			throw new IllegalArgumentException("StudentId cannot be null");
		}

		return paymentMap.values().stream().filter(p -> studentId.equals(p.getStudentId())).toList();
	}

	@Override
	public List<Payment> getAllPayments() {
		return new ArrayList<>(paymentMap.values());
	}

	@Override
	public void updatePaymentStatus(Integer paymentId, String status) {
		validatePaymentId(paymentId);

		Payment payment = paymentMap.get(paymentId);
		if (payment == null) {
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
		}

		try {
			payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid payment status");
		}

		payment.setPaymentDate(LocalDateTime.now());

		rewritePaymentFile();

	}

	@Override
	public void deletePayment(Integer paymentId) {
		validatePaymentId(paymentId);

		Payment removed = paymentMap.remove(paymentId);
		if (removed == null) {
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
		}

		rewritePaymentFile();

	}

	@Override
	public BigDecimal getTotalPaidByStudent(Integer studentId) {

		return paymentMap.values().stream().filter(p -> p.getStudentId().equals(studentId))
				.filter(p -> p.getStatus() == PaymentStatus.SUCCESS).map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/* ---------------- REQUIRED METHOD ---------------- */

	@Override
	public List<Integer> getStudentsWithPendingFees() {

		// Sum SUCCESS payments per (studentId + courseId)
		Map<String, BigDecimal> totalPaid = paymentMap.values().stream()
				.filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
				.collect(Collectors.groupingBy(p -> p.getStudentId() + "-" + p.getCourseId(),
						Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

		return paymentMap.values().stream().map(p -> {

			Integer studentId = p.getStudentId();
			Integer courseId = p.getCourseId();

			String key = studentId + "-" + courseId;

			BigDecimal paid = totalPaid.getOrDefault(key, BigDecimal.ZERO);

			BigDecimal courseFee = courseService.getCourseById(courseId).getFees();

			return paid.compareTo(courseFee) < 0 ? studentId : null;
		}).filter(Objects::nonNull).distinct().toList();
	}

	/* ---------------- VALIDATION ---------------- */

	private void validatePayment(Payment payment) {
		if (payment == null || payment.getPaymentId() == null || payment.getStudentId() == null
				|| payment.getCourseId() == null || payment.getAmount() == null
				|| payment.getAmount().compareTo(BigDecimal.ZERO) <= 0 || payment.getPaymentMode() == null
				|| payment.getStatus() == null || payment.getPaymentDate() == null) {

			throw new IllegalArgumentException("Invalid payment data");
		}
	}

	private void validatePaymentId(Integer paymentId) {
		if (paymentId == null) {
			throw new IllegalArgumentException("PaymentId cannot be null");
		}
	}

}
