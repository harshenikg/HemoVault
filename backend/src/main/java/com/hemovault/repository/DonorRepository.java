package com.hemovault.repository;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findByBloodGroup(BloodGroup bloodGroup);

    List<Donor> findByCityContainingIgnoreCase(String city);

    @Query("SELECT d FROM Donor d WHERE d.bloodGroup IN :groups AND d.isEligible = true")
    List<Donor> findEligibleByBloodGroups(@Param("groups") List<BloodGroup> groups);

    @Query("SELECT d FROM Donor d WHERE d.bloodGroup IN :groups AND d.isEligible = true " +
           "AND LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Donor> findEligibleByBloodGroupsAndCity(
            @Param("groups") List<BloodGroup> groups,
            @Param("city") String city);
}
