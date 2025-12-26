package com.shopperspoint.key;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewKey implements Serializable {

    private Long customerUserId;
    private Long productId;

}
