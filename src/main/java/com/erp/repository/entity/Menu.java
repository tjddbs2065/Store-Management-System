package com.erp.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Setter
@Getter
@Table(name = "menu")
public class Menu {
    @Id
    @Column(name = "menu_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long menuNo;
    @Column(nullable = false)
    private String menuName;
    @Column(nullable = false)
    private String menuCode;
    @Column(nullable = false)
    private String menuCategory;
    private String menuExplain;
    @Column(nullable = false)
    private String size;
    private String menuImage;
    private Integer menuPrice;
    @Column(nullable = false)
    private String releaseStatus;
    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp inDate;
    private Timestamp editDate;
    private Timestamp delDate;
}
