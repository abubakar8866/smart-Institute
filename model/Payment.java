package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private final Integer paymentId;
    private final Integer studentId;
    private final Integer courseId;

    private BigDecimal amount;
    private PaymentMode paymentMode;
    private PaymentStatus status;
    private LocalDateTime paymentDate;

    public Payment(Integer paymentId,
                   Integer studentId,
                   Integer courseId,
                   BigDecimal amount,
                   PaymentMode paymentMode,
                   PaymentStatus status,
                   LocalDateTime paymentDate) {

        this.paymentId = paymentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Payment [paymentId=" + paymentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", amount=" + amount +
                ", mode=" + paymentMode +
                ", status=" + status + "]";
    }
}
