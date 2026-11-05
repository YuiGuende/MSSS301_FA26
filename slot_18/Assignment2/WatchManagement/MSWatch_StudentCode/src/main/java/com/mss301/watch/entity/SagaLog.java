package com.mss301.watch.entity;

import com.mss301.watch.service.saga.SagaPhase;
import com.mss301.watch.service.saga.SagaStepResult;
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

// [TODO-6.2] One row per EXECUTE, COMPENSATE or FAULT event of a Saga
@Entity
@Table(name = "SagaLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "SagaId", length = 36, nullable = false)
    private String sagaId;

    @Column(name = "StepNo", nullable = false)
    private Integer stepNo;

    @Column(name = "StepName", length = 50, nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "Phase", length = 20, nullable = false)
    private SagaPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(name = "Result", length = 20, nullable = false)
    private SagaStepResult result;

    @Column(name = "Message", length = 1000)
    private String message;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
