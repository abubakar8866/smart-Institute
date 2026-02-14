package service;

import model.Payment;

import java.util.List;

public interface PaymentService {

    void addPayment(Payment payment);

    Payment getPaymentById(Integer paymentId);

    List<Payment> getPaymentsByStudent(Integer studentId);

    List<Payment> getAllPayments();

    void updatePaymentStatus(Integer paymentId, String status);

    void deletePayment(Integer paymentId);
}
