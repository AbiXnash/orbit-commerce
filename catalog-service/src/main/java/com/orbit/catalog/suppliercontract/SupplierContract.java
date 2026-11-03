package com.orbit.catalog.suppliercontract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** A SupplierContract as stored by the catalog service. */
@Entity
@Table(name = "supplier_contract")
public class SupplierContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private int priority;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 120)
    private String contractRef;

    private int renewalNoticeDays;

    private boolean signed;

    private Instant signedAt;

    @Version
    private long version;

    private Instant createdAt;

    private Instant updatedAt;

    protected SupplierContract() {
        // for JPA
    }

    public SupplierContract(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public void archive() {
        status = "ARCHIVED";
    }

    public void activate() {
        status = "ACTIVE";
    }

    public void deactivate() {
        status = "INACTIVE";
    }

    public void rename(String name) {
        this.name = name;
    }

    public void addAmount(BigDecimal delta) {
        if (delta != null) {
            amount = amount.add(delta);
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getContractRef() {
        return contractRef;
    }

    public void setContractRef(String contractRef) {
        this.contractRef = contractRef;
    }

    public int getRenewalNoticeDays() {
        return renewalNoticeDays;
    }

    public void setRenewalNoticeDays(int renewalNoticeDays) {
        this.renewalNoticeDays = renewalNoticeDays;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SupplierContract that)) {
            return false;
        }
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "SupplierContract[" + code + ", " + status + "]";
    }
}
