package com.mss301.watch.repository;

import com.mss301.watch.entity.SagaLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// [TODO-6.2]
public interface SagaLogRepository extends JpaRepository<SagaLog, Long> {

    List<SagaLog> findBySagaIdOrderByIdAsc(String sagaId);
}
