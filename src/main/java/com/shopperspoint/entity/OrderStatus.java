package com.shopperspoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Enumerated(EnumType.STRING)
    private com.shopperspoint.enums.OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private com.shopperspoint.enums.OrderStatus toStatus;


    private LocalDateTime transitionDate;
    private String transitionNotesComments;

    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;
}
