package com.hemovault.repository;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {
    List<PredictionHistory> findByBloodGroupOrderByPredictionDateDesc(BloodGroup bloodGroup);
    List<PredictionHistory> findAllByOrderByPredictionDateDesc();
}
