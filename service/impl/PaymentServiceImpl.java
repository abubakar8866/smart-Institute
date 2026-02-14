package service.impl;

import model.Payment;
import model.PaymentStatus;
import service.PaymentService;
import util.FileUtil;
import exception.PaymentNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PaymentServiceImpl implements PaymentService {

    private final Map<Integer, Payment> paymentMap =
            new ConcurrentHashMap<>();

    private static final String PAYMENT_FILE = "data/payments.txt";

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
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> getAllPayments() {
        return new ArrayList<>(paymentMap.values());
    }

    @Override
    public void updatePaymentStatus(Integer paymentId, String status) {

        validatePaymentId(paymentId);

        PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());

        Payment payment = paymentMap.get(paymentId);

        if (payment == null) {
            throw new PaymentNotFoundException(
                    "Payment not found with id: " + paymentId);
        }

        payment.setStatus(newStatus);
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

    private void validatePayment(Payment payment) {

        if (payment == null ||
                payment.getPaymentId() == null ||
                payment.getStudentId() == null ||
                payment.getCourseId() == null ||
                payment.getAmount() == null ||
                payment.getAmount().doubleValue() <= 0 ||
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
