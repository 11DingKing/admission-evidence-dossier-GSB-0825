package com.gsb.admission.dossier.domain;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    void factKeyHashIsStableAndMatchesSha256OfCanonicalString() throws Exception {
        FactKey factKey = new FactKey("44", 2025, SubjectCategory.PHYSICS, "11113", "203");

        String hash = hashService.factKeyHash(factKey);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String expected = HexFormat.of().formatHex(
                digest.digest("44|2025|PHYSICS|11113|203".getBytes(StandardCharsets.UTF_8)));

        assertThat(hash).isEqualTo(expected);
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(hashService.factKeyHash(factKey)).isEqualTo(hash);
    }

    @Test
    void recordContentHashHasPrefixAndIsStable() {
        String hash1 = hashService.recordContentHash(
                "GD_EDU_EXAM", 2, 6090, ScoreScale.GAOKAO_750_TENTH,
                "粤教考函〔2025〕41号",
                LocalDate.of(2025, 7, 25), null,
                Instant.parse("2025-07-25T10:00:00Z"),
                "更正公告原文");
        String hash2 = hashService.recordContentHash(
                "GD_EDU_EXAM", 2, 6090, ScoreScale.GAOKAO_750_TENTH,
                "粤教考函〔2025〕41号",
                LocalDate.of(2025, 7, 25), null,
                Instant.parse("2025-07-25T10:00:00Z"),
                "更正公告原文");

        assertThat(hash1).startsWith("sha256:");
        assertThat(hash1.substring("sha256:".length())).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void differentInputProducesDifferentHash() {
        String hash1 = hashService.recordContentHash(
                "GD_EDU_EXAM", 1, 6100, ScoreScale.GAOKAO_750_TENTH,
                "doc-1", LocalDate.of(2025, 7, 19), null,
                Instant.parse("2025-07-19T09:00:00Z"), "raw-1");
        String hash2 = hashService.recordContentHash(
                "GD_EDU_EXAM", 2, 6090, ScoreScale.GAOKAO_750_TENTH,
                "doc-2", LocalDate.of(2025, 7, 25), null,
                Instant.parse("2025-07-25T10:00:00Z"), "raw-2");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void sealContentHashIsStable() {
        byte[] json = "{\"status\":\"CONFLICT\"}".getBytes(StandardCharsets.UTF_8);

        String hash1 = hashService.sealContentHash(json);
        String hash2 = hashService.sealContentHash(json);

        assertThat(hash1).startsWith("sha256:");
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1.substring("sha256:".length())).hasSize(64);
    }
}
