package com.jordi.kakebot.user.model;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
        name = "user_category_limits",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_category_limits_user_category", columnNames = {"user_id", "category"})
)
public class UserCategoryLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(name = "monthly_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "last_alert_sent_year")
    private Integer lastAlertSentYear;

    @Column(name = "last_alert_sent_month")
    private Integer lastAlertSentMonth;

    protected UserCategoryLimit() {
    }

    public UserCategoryLimit(User user, ExpenseCategory category, BigDecimal monthlyLimit) {
        this.user = user;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public Integer getLastAlertSentYear() {
        return lastAlertSentYear;
    }

    public void setLastAlertSentYear(Integer lastAlertSentYear) {
        this.lastAlertSentYear = lastAlertSentYear;
    }

    public Integer getLastAlertSentMonth() {
        return lastAlertSentMonth;
    }

    public void setLastAlertSentMonth(Integer lastAlertSentMonth) {
        this.lastAlertSentMonth = lastAlertSentMonth;
    }
}
