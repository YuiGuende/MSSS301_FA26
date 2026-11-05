package com.mss301.watch.repository;

import com.mss301.watch.entity.SagaState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// [TODO-6.2]
public interface SagaStateRepository extends JpaRepository<SagaState, Long> {

    Optional<SagaState> findBySagaId(String sagaId);
}
