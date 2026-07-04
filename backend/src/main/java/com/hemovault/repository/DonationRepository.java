package com.hemovault.repository;

import com.hemovault.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorId(Long donorId);

    List<Donation> findByDonationDateBetween(LocalDate start, LocalDate end);

    List<Donation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT d FROM Donation d WHERE d.createdAt >= :since ORDER BY d.createdAt DESC")
    List<Donation> findRecentDonations(@Param("since") LocalDateTime since);
}
