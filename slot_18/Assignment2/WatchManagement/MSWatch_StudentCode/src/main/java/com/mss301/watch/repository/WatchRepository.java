package com.mss301.watch.repository;

import com.mss301.watch.entity.Watch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// [TODO-6.16] The category is fetched with the watch because responses need CategoryName
public interface WatchRepository extends JpaRepository<Watch, Integer> {

    @Query("select w from Watch w join fetch w.category order by w.watchId desc")
    List<Watch> findAllNewestFirst();

    @Query("select w from Watch w join fetch w.category where w.watchId = :id")
    Optional<Watch> findWithCategory(@Param("id") Integer id);
}
