package com.dossier.admission.repository;

import com.dossier.admission.domain.Dossier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DossierRepository extends JpaRepository<Dossier, String> {

    @Query("""
            SELECT d FROM Dossier d
            WHERE d.provinceCode = :#{#factKey.provinceCode}
              AND d.admissionYear = :#{#factKey.admissionYear}
              AND d.subjectCategory = :#{#factKey.subjectCategory}
              AND d.schoolCode = :#{#factKey.schoolCode}
              AND d.majorGroupCode = :#{#factKey.majorGroupCode}
            ORDER BY d.createdAt DESC
            """)
    List<Dossier> findAllByFactKey(@Param("factKey") com.dossier.admission.domain.FactKey factKey);

    @Query("""
            SELECT d FROM Dossier d
            WHERE d.provinceCode = :#{#factKey.provinceCode}
              AND d.admissionYear = :#{#factKey.admissionYear}
              AND d.subjectCategory = :#{#factKey.subjectCategory}
              AND d.schoolCode = :#{#factKey.schoolCode}
              AND d.majorGroupCode = :#{#factKey.majorGroupCode}
              AND d.sealed = false
            """)
    Optional<Dossier> findOpenByFactKey(@Param("factKey") com.dossier.admission.domain.FactKey factKey);
}
