package service;

import model.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    void addPayment(Payment payment);

    Payment getPaymentById(Integer paymentId);

    List<Payment> getPaymentsByStudent(Integer studentId);

    List<Payment> getAllPayments();

    void updatePaymentStatus(Integer paymentId, String status);

    void deletePayment(Integer paymentId);
    
    Map<Integer, BigDecimal> getStudentsWithPendingFees();
    
    BigDecimal getTotalPaidByStudent(Integer studentId);

	void createPendingPayment(Integer studentId, Integer courseId, BigDecimal courseFee);

}
