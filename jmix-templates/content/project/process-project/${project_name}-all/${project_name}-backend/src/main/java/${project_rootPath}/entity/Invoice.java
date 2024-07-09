package ${project_rootPackage}.entity;

import io.jmee.client.entity.ProcessFormEntity;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "${prefixTable}INVOICE", indexes = {
        @Index(name = "IDX_${prefixTable}INVOICE_BANK_ACCOUNT", columnList = "BANK_ACCOUNT_ID")
})
@Entity(name = "${normalizedPrefix_underscore}Invoice")
public class Invoice implements ProcessFormEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @Column(name = "NUMBER_", nullable = false)
    private Long number;

    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "PAYMENT_STATUS")
    private String paymentStatus;

    @JoinColumn(name = "BANK_ACCOUNT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private BankAccount bankAccount;

    @Column(name = "VENDOR")
    private String vendor;

    @NotNull
    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "PO_NUMBER", nullable = false)
    private UUID poNumber;

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public UUID getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(UUID poNumber) {
        this.poNumber = poNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus == null ? null : PaymentStatus.fromId(paymentStatus);
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus == null ? null : paymentStatus.getId();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}