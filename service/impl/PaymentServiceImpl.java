package service.impl;

import model.Payment;
import model.PaymentStatus;
import service.CourseService;
import service.PaymentService;
import util.FileUtil;
import exception.PaymentNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PaymentServiceImpl implements PaymentService {

    private final Map<Integer, Payment> paymentMap =
            new ConcurrentHashMap<>();

    private final CourseService courseService;

    private static final String PAYMENT_FILE = "data/payments.txt";

    public PaymentServiceImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    /* ---------------- CORE METHODS ---------------- */

    @Override
    public void addPayment(Payment payment) {
        validatePayment(payment);

        if (paymentMap.putIfAbsent(payment.getPaymentId(), payment) != null) {
            throw new IllegalArgumentException(
                    "Payment already exists with ID: " + payment.getPaymentId());
        }

        FileUtil.writeToFile(PAYMENT_FILE, formatPayment(payment));
    }

    @Override
    public Payment getPaymentById(Integer paymentId) {
        validatePaymentId(paymentId);

        Payment payment = paymentMap.get(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(
                    "Payment not found with id: " + paymentId);
        }
        return payment;
    }

    @Override
    public List<Payment> getPaymentsByStudent(Integer studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("StudentId cannot be null");
        }

        return paymentMap.values()
                .stream()
                .filter(p -> studentId.equals(p.getStudentId()))
                .toList();
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
            throw new PaymentNotFoundException(
                    "Payment not found with id: " + paymentId);
        }

        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        payment.setPaymentDate(LocalDateTime.now());

        FileUtil.writeToFile(PAYMENT_FILE,
                "UPDATED: " + formatPayment(payment));
    }

    @Override
    public void deletePayment(Integer paymentId) {
        validatePaymentId(paymentId);

        Payment removed = paymentMap.remove(paymentId);
        if (removed == null) {
            throw new PaymentNotFoundException(
                    "Payment not found with id: " + paymentId);
        }

        FileUtil.writeToFile(PAYMENT_FILE,
                "DELETED: " + formatPayment(removed));
    }

    /* ---------------- REQUIRED METHOD ---------------- */

    @Override
    public List<Integer> getStudentsWithPendingFees() {

        // Sum SUCCESS payments per student
        Map<Integer, BigDecimal> totalPaidByStudent =
                paymentMap.values()
                        .stream()
                        .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                        .collect(Collectors.groupingBy(
                                Payment::getStudentId,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Payment::getAmount,
                                        BigDecimal::add
                                )
                        ));

        return paymentMap.values()
                .stream()
                .map(p -> {
                    Integer studentId = p.getStudentId();

                    BigDecimal courseFee =
                            courseService
                                    .getCourseById(p.getCourseId())
                                    .getFees();

                    BigDecimal paid =
                            totalPaidByStudent.getOrDefault(
                                    studentId,
                                    BigDecimal.ZERO
                            );

                    return paid.compareTo(courseFee) < 0 ? studentId : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /* ---------------- VALIDATION ---------------- */

    private void validatePayment(Payment payment) {
        if (payment == null ||
                payment.getPaymentId() == null ||
                payment.getStudentId() == null ||
                payment.getCourseId() == null ||
                payment.getAmount() == null ||
                payment.getAmount().compareTo(BigDecimal.ZERO) <= 0 ||
                payment.getPaymentMode() == null ||
                payment.getStatus() == null ||
                payment.getPaymentDate() == null) {

            throw new IllegalArgumentException("Invalid payment data");
        }
    }

    private void validatePaymentId(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("PaymentId cannot be null");
        }
    }

    private String formatPayment(Payment payment) {
        return payment.getPaymentId() + "," +
                payment.getStudentId() + "," +
                payment.getCourseId() + "," +
                payment.getAmount() + "," +
                payment.getPaymentMode() + "," +
                payment.getStatus() + "," +
                payment.getPaymentDate();
    }
}
