package com.shopperspoint.entity;

import com.shopperspoint.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    private LocalDateTime dateCreated;
    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String customerAddressCity;
    private String customerAddressState;
    private String customerAddressCountry;
    private String customerAddressAddressLine;
    private String customerAddressZipCode;
    private String customerAddressLabel;            // ex : work, office,home


    @ManyToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;
}
