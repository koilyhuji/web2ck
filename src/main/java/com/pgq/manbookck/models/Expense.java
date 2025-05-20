package com.pgq.manbookck.models;

import jakarta.persistence.*; // Or javax.persistence.*
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String description;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    
    @Column(name = "expense_date", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY is good for performance
    @JoinColumn(name = "category_id") // FK column in the 'expenses' table
    private ExpenseCategory category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}