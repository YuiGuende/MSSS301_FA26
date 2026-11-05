package com.mss301.watch.entity;

import com.mss301.watch.service.saga.SagaStatus;
import com.mss301.watch.service.saga.SagaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// [TODO-6.2] One row per Saga; Payload keeps the before/after snapshots for later recovery
@Entity
@Table(name = "SagaStates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "SagaId", length = 36, nullable = false, unique = true)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SagaType", length = 20, nullable = false)
    private SagaType sagaType;

    @Column(name = "WatchID")
    private Integer watchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private SagaStatus status;

    @Column(name = "CurrentStep", length = 50)
    private String currentStep;

    @Column(name = "FailureReason", length = 1000)
    private String failureReason;

    @Column(name = "Payload", columnDefinition = "NVARCHAR(MAX)")
    private String payload;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
