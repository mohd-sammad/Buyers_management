package com.buyer.management.repository;

import com.buyer.management.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Map;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    
    boolean existsByEmailAndMobileAndUploadedByEmail(String email, String mobile, String uploadedByEmail);

    @Query("SELECT b FROM Buyer b WHERE b.uploadedBy.email = :userEmail AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "b.mobile LIKE CONCAT('%', :search, '%'))")
    Page<Buyer> findByUploadedByEmailAndSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    @Query("SELECT " +
           "COUNT(b) as totalBuyers, " +
           "COALESCE(SUM(b.totalInvoiceAmount), 0) as totalInvoice, " +
           "COALESCE(SUM(b.totalAmountPaid), 0) as totalPaid, " +
           "COALESCE(SUM(b.totalAmountDue), 0) as totalDue " +
           "FROM Buyer b WHERE b.uploadedBy.email = :userEmail")
    Map<String, Object> getStatisticsByEmail(@Param("userEmail") String userEmail);
}