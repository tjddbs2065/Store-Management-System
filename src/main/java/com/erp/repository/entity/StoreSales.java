package com.erp.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Setter
@Getter
@ToString
@Table(name = "store_sales")
public class StoreSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeSalesNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_no")
    private Store store;
    private LocalDate salesDate;
    private Integer salesPrice;

}
