package com.dossier.admission.repository;

import com.dossier.admission.domain.FactKey;
import com.dossier.admission.domain.SourceRecord;
import com.dossier.admission.domain.SourceRecordId;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceRecordRepository extends JpaRepository<SourceRecord, SourceRecordId> {

    @Query("SELECT s FROM SourceRecord s WHERE s.id.factKey = :factKey ORDER BY s.id.sourceId, s.id.sourceRevision DESC")
    List<SourceRecord> findAllByFactKey(@Param("factKey") FactKey factKey);

    @Query("""
            SELECT s FROM SourceRecord s
            WHERE s.id.factKey = :factKey
              AND s.recordedAt <= :asOf
              AND s.id.sourceRevision = (
                  SELECT MAX(s2.id.sourceRevision) FROM SourceRecord s2
                  WHERE s2.id.factKey = :factKey
                    AND s2.id.sourceId = s.id.sourceId
                    AND s2.recordedAt <= :asOf
              )
            ORDER BY s.id.sourceId
            """)
    List<SourceRecord> findActiveByFactKeyAsOf(@Param("factKey") FactKey factKey,
                                                @Param("asOf") Instant asOf);

    @Query("""
            SELECT s FROM SourceRecord s
            WHERE s.id.factKey = :factKey
              AND s.id.sourceRevision = (
                  SELECT MAX(s2.id.sourceRevision) FROM SourceRecord s2
                  WHERE s2.id.factKey = :factKey
                    AND s2.id.sourceId = s.id.sourceId
              )
            ORDER BY s.id.sourceId
            """)
    List<SourceRecord> findActiveByFactKey(@Param("factKey") FactKey factKey);

    @Query("SELECT MAX(s.id.sourceRevision) FROM SourceRecord s WHERE s.id.factKey = :factKey AND s.id.sourceId = :sourceId")
    Integer findMaxRevision(@Param("factKey") FactKey factKey, @Param("sourceId") String sourceId);

    @Query("SELECT COUNT(s) FROM SourceRecord s WHERE s.id.factKey = :factKey AND s.id.sourceId = :sourceId AND s.id.sourceRevision = :revision")
    long countByFactKeyAndSourceIdAndRevision(@Param("factKey") FactKey factKey,
                                              @Param("sourceId") String sourceId,
                                              @Param("revision") Integer revision);
}
