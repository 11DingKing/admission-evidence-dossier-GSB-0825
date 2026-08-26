package com.gsb.admission.dossier.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;

public final class HashService {

    public static final String SHA256_PREFIX = "sha256:";

    public String factKeyHash(FactKey factKey) {
        return sha256Hex(factKey.canonical());
    }

    public String recordContentHash(
            String sourceId,
            int sourceRevision,
            long scoreTenths,
            ScoreScale scoreScale,
            String sourceDocNo,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Instant recordedAt,
            String rawText) {
        String canonical = String.join("|",
                sourceId,
                Integer.toString(sourceRevision),
                Long.toString(scoreTenths),
                scoreScale.name(),
                sourceDocNo,
                effectiveFrom.toString(),
                effectiveTo == null ? "" : effectiveTo.toString(),
                recordedAt.toString(),
                rawText);
        return SHA256_PREFIX + sha256Hex(canonical);
    }

    public String sealContentHash(byte[] canonicalJson) {
        return SHA256_PREFIX + sha256Hex(new String(canonicalJson, StandardCharsets.UTF_8));
    }

    public String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
