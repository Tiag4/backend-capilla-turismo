package com.upc.demo.repositorio;

import com.upc.demo.entidad.MerchantApplication;
import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantApplicationRepository extends JpaRepository<MerchantApplication, UUID> {

    List<MerchantApplication> findAllByOrderByCreatedAtDesc();

    List<MerchantApplication> findByStatusOrderByCreatedAtDesc(MerchantApplicationStatus status);

    boolean existsByLicenseNumber(String licenseNumber);
}
