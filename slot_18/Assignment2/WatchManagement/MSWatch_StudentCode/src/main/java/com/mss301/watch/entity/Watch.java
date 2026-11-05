package com.mss301.watch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// [TODO-6.16] Maps the Watches table. BrandID lives in another database, so it is only a reference
@Entity
@Table(name = "Watches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WatchID")
    private Integer watchId;

    @Column(name = "Name", length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", nullable = false)
    private WatchCategory category;

    @Column(name = "BrandID", nullable = false)
    private Integer brandId;

    @Column(name = "CaseMaterial", length = 100, nullable = false)
    private String caseMaterial;

    @Column(name = "StrapMaterial", length = 100, nullable = false)
    private String strapMaterial;

    @Column(name = "WaterResistance", length = 50, nullable = false)
    private String waterResistance;

    @Column(name = "Price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "ReleaseDate", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "Stock", nullable = false)
    private Integer stock;
}
