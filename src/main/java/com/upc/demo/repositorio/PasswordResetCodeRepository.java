package com.upc.demo.repositorio;

import com.upc.demo.entidad.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    Optional<PasswordResetCode> findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);

    List<PasswordResetCode> findAllByEmailAndUsedAtIsNull(String email);
}
