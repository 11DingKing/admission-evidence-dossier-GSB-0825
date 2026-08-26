package com.dossier.admission.seed;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.FactKey;
import com.dossier.admission.repository.DossierRepository;
import com.dossier.admission.repository.SourceRecordRepository;
import com.dossier.admission.service.SourceService;
import com.dossier.admission.web.dto.SourceRecordRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    static final String SZ_PROVINCE = "44";
    static final int SZ_YEAR = 2025;
    static final String SZ_SUBJECT = "PHYSICS";
    static final String SZ_SCHOOL = "11113";
    static final String SZ_GROUP = "001";
    static final String SZ_SCALE = "PHYSICS_750_T1";

    static final String WH_PROVINCE = "42";
    static final int WH_YEAR = 2025;
    static final String WH_SUBJECT = "ART";
    static final String WH_SCHOOL = "13796";
    static final String WH_GROUP = "018";
    static final String WH_SCALE = "ART_COMPREHENSIVE_T1";

    private final DossierRepository dossierRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final SourceService sourceService;

    public DataSeeder(DossierRepository dossierRepository,
                      SourceRecordRepository sourceRecordRepository,
                      SourceService sourceService) {
        this.dossierRepository = dossierRepository;
        this.sourceRecordRepository = sourceRecordRepository;
        this.sourceService = sourceService;
    }

    @Override
    public void run(String... args) {
        seedShenzhen();
        seedWuhan();
    }

    private void seedShenzhen() {
        FactKey szKey = new FactKey(SZ_PROVINCE, SZ_YEAR, SZ_SUBJECT, SZ_SCHOOL, SZ_GROUP);
        if (sourceRecordRepository.findAllByFactKey(szKey).isEmpty()) {
            Instant base = Instant.parse("2025-07-15T10:00:00Z");
            Instant eff = Instant.parse("2025-07-10T00:00:00Z");

            sourceService.submitBatch(java.util.List.of(
                    new SourceRecordRequest(
                            SZ_PROVINCE, SZ_YEAR, SZ_SUBJECT, SZ_SCHOOL, SZ_GROUP,
                            "SOURCE_A", 1, 6100L, SZ_SCALE, "610.0",
                            eff, null, base, "粤招办[2025]12号",
                            "深圳职业技术大学 普通物理类 投档最高分 610.0"),
                    new SourceRecordRequest(
                            SZ_PROVINCE, SZ_YEAR, SZ_SUBJECT, SZ_SCHOOL, SZ_GROUP,
                            "SOURCE_B", 1, 6090L, SZ_SCALE, "609.0",
                            eff, null, base.plus(2, ChronoUnit.HOURS), "粤招办[2025]13号",
                            "深圳职业技术大学 普通物理类 投档最高分 609.0")
            ), null);

            sourceService.submitBatch(java.util.List.of(
                    new SourceRecordRequest(
                            SZ_PROVINCE, SZ_YEAR, SZ_SUBJECT, SZ_SCHOOL, SZ_GROUP,
                            "SOURCE_A", 2, 6090L, SZ_SCALE, "609.0",
                            eff, null, base.plus(1, ChronoUnit.DAYS), "粤招办[2025]12号-勘误",
                            "深圳职业技术大学 普通物理类 投档最高分 更正为 609.0")
            ), null);
        }

        if (dossierRepository.findOpenByFactKey(szKey).isEmpty()) {
            Dossier d = new Dossier(szKey, SZ_SCALE);
            dossierRepository.save(d);
        }
    }

    private void seedWuhan() {
        FactKey whKey = new FactKey(WH_PROVINCE, WH_YEAR, WH_SUBJECT, WH_SCHOOL, WH_GROUP);
        if (sourceRecordRepository.findAllByFactKey(whKey).isEmpty()) {
            Instant base = Instant.parse("2025-07-16T10:00:00Z");
            Instant eff = Instant.parse("2025-07-11T00:00:00Z");

            sourceService.submitBatch(java.util.List.of(
                    new SourceRecordRequest(
                            WH_PROVINCE, WH_YEAR, WH_SUBJECT, WH_SCHOOL, WH_GROUP,
                            "SOURCE_C", 1, 6044L, WH_SCALE, "604.4",
                            eff, null, base, "鄂招办[2025]21号",
                            "武汉职业技术大学 数字媒体艺术 艺术综合分 604.4")
            ), null);
        }

        if (dossierRepository.findOpenByFactKey(whKey).isEmpty()) {
            Dossier d = new Dossier(whKey, WH_SCALE);
            dossierRepository.save(d);
        }
    }
}
