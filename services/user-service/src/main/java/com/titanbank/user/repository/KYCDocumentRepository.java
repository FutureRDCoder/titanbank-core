package com.titanbank.user.repository;

import com.titanbank.user.model.entity.KYCDocument;
import com.titanbank.user.model.enums.DocumentType;
import com.titanbank.user.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KYCDocumentRepository extends JpaRepository<KYCDocument, Long> {

    // Spring Data automatically generates:
    // SELECT * FROM kyc_documents WHERE user_id = ?
    List<KYCDocument> findByUserId(Long userId);

    // Spring Data automatically generates:
    // SELECT * FROM kyc_documents WHERE user_id = ? AND document_type = ?
    Optional<KYCDocument> findByUserIdAndDocumentType(Long userId, DocumentType documentType);

    // Spring Data automatically generates:
    // SELECT * FROM kyc_documents WHERE verification_status = ?
    List<KYCDocument> findByVerificationStatus(VerificationStatus status);

    // Spring Data automatically generates COUNT query:
    // SELECT COUNT(*) FROM kyc_documents WHERE user_id = ? AND verification_status = ?
    Long countByUserIdAndVerificationStatus(Long userId, VerificationStatus status);

    // Custom JPQL with JOIN FETCH for performance (loads user in same query)
    @Query("SELECT kd FROM KYCDocument kd JOIN FETCH kd.user WHERE kd.verificationStatus = :status")
    List<KYCDocument> findByVerificationStatusWithUser(@Param("status") VerificationStatus status);

    // Additional useful queries
    List<KYCDocument> findByUserIdOrderByUploadedAtDesc(Long userId);

    boolean existsByUserIdAndDocumentType(Long userId, DocumentType documentType);
}