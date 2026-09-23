package com.upc.demo.repositorio;

import com.upc.demo.entidad.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findAllByOrderByTimestampDesc();

    List<AuditEvent> findByActionTypeOrderByTimestampDesc(String actionType);

    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:query IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.targetEntity) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.operatorName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY a.timestamp DESC")
    List<AuditEvent> searchLogs(@Param("actionType") String actionType, @Param("query") String query);
}
