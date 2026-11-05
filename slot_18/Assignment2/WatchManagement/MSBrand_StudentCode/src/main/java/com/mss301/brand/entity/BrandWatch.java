package com.mss301.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// [TODO-5.1] The brand-watch link every Saga keeps consistent; a watch belongs to one brand
@Entity
@Table(name = "BrandWatches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "WatchID", nullable = false, unique = true)
    private Integer watchId;

    @Column(name = "BrandID", nullable = false)
    private Integer brandId;
}
