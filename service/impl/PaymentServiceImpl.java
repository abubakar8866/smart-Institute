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
		if (!file.exists() || file.length() == 0)
			return;

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean isFirstLine = true;
			while ((line = reader.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					continue;
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

		for (Payment p : paymentMap.values()) {
			builder.append(p.getPaymentId()).append(",").append(p.getStudentId()).append(",").append(p.getCourseId())
					.append(",").append(p.getAmount().toPlainString()).append(",").append(p.getPaymentMode())
					.append(",").append(p.getStatus()).append(",").append(p.getPaymentDate())
					.append(System.lineSeparator());
		}

		FileUtil.overwriteFile(PAYMENT_FILE, builder.toString());
	}

	/* ---------------- CORE METHODS ---------------- */

	@Override
	public void addPayment(Payment payment) {
		validatePayment(payment);

		Optional<Payment> pendingOpt = paymentMap.values().stream()
				.filter(p -> p.getStudentId().equals(payment.getStudentId()))
				.filter(p -> p.getCourseId().equals(payment.getCourseId()))
				.filter(p -> p.getStatus() == PaymentStatus.PENDING).findFirst();

		LocalDateTime now = LocalDateTime.now();

		if (pendingOpt.isPresent()) {
			Payment pending = pendingOpt.get();
			BigDecimal remaining = pending.getAmount().subtract(payment.getAmount());

			if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
				// ✅ Fully paid
				pending.setAmount(BigDecimal.ZERO);
				pending.setStatus(PaymentStatus.SUCCESS);
				pending.setPaymentMode(payment.getPaymentMode());
				pending.setPaymentDate(now);
			} else {
				// ✅ Partial payment
				pending.setAmount(remaining); // Keep remaining as pending
				pending.setPaymentDate(now); // Update timestamp
				// Do NOT mark status SUCCESS yet
			}

			// Record the actual payment as SUCCESS
			Payment actualPayment = new Payment(IdGenerator.generateId(), payment.getStudentId(), payment.getCourseId(),
					payment.getAmount(), payment.getPaymentMode(), PaymentStatus.SUCCESS, now);
			paymentMap.put(actualPayment.getPaymentId(), actualPayment);

		} else {
			// No pending → treat as new payment
			paymentMap.put(payment.getPaymentId(), payment);
		}

		rewritePaymentFile();
	}

	@Override
	public void createPendingPayment(Integer studentId, Integer courseId, BigDecimal courseFee) {
		LocalDateTime now = LocalDateTime.now();

		Payment pending = new Payment(IdGenerator.generateId(), studentId, courseId, courseFee, PaymentMode.CASH, // default
																													// placeholder
				PaymentStatus.PENDING, now);
		paymentMap.put(pending.getPaymentId(), pending);

		rewritePaymentFile();
	}

	@Override
	public Payment getPaymentById(Integer paymentId) {
		validatePaymentId(paymentId);
		Payment payment = paymentMap.get(paymentId);
		if (payment == null)
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
		return payment;
	}

	@Override
	public List<Payment> getPaymentsByStudent(Integer studentId) {
		if (studentId == null)
			throw new IllegalArgumentException("StudentId cannot be null");
		return paymentMap.values().stream().filter(p -> p.getStudentId().equals(studentId)).toList();
	}

	@Override
	public List<Payment> getAllPayments() {
		return new ArrayList<>(paymentMap.values());
	}

	@Override
	public void updatePaymentStatus(Integer paymentId, String status) {
		validatePaymentId(paymentId);
		Payment payment = paymentMap.get(paymentId);
		if (payment == null)
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);

		payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
		payment.setPaymentDate(LocalDateTime.now());
		rewritePaymentFile();
	}

	@Override
	public void deletePayment(Integer paymentId) {
		validatePaymentId(paymentId);
		if (paymentMap.remove(paymentId) == null)
			throw new PaymentNotFoundException("Payment not found with id: " + paymentId);
		rewritePaymentFile();
	}

	@Override
	public BigDecimal getTotalPaidByStudent(Integer studentId) {
		return paymentMap.values().stream().filter(p -> p.getStudentId().equals(studentId))
				.filter(p -> p.getStatus() == PaymentStatus.SUCCESS).map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public Map<Integer, BigDecimal> getStudentsWithPendingFees() {
		return paymentMap.values().stream().filter(p -> p.getStatus() == PaymentStatus.PENDING)
				.collect(Collectors.groupingBy(Payment::getStudentId,
						Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
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
		if (paymentId == null)
			throw new IllegalArgumentException("PaymentId cannot be null");
	}
}