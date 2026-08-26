package com.dossier.admission.web;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.FactKey;
import com.dossier.admission.domain.Freshness;
import com.dossier.admission.domain.SourceRecord;
import com.dossier.admission.repository.DossierRepository;
import com.dossier.admission.service.ConsistencyEvaluator;
import com.dossier.admission.service.SourceService;
import com.dossier.admission.web.dto.BatchSourceRequest;
import com.dossier.admission.web.dto.BatchSourceResponse;
import com.dossier.admission.web.dto.SourceRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Tag(name = "Sources", description = "来源记录提交与查询")
public class SourceController {

    private final SourceService sourceService;
    private final DossierRepository dossierRepository;
    private final ConsistencyEvaluator consistencyEvaluator;

    public SourceController(SourceService sourceService,
                            DossierRepository dossierRepository,
                            ConsistencyEvaluator consistencyEvaluator) {
        this.sourceService = sourceService;
        this.dossierRepository = dossierRepository;
        this.consistencyEvaluator = consistencyEvaluator;
    }

    @PostMapping("/dossiers/{dossierId}/sources/batch")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    @Operation(summary = "向指定卷宗批量提交来源（最多 5 条），逐项返回成功或错误")
    public BatchSourceResponse submitToDossier(@PathVariable String dossierId,
                                               @Valid @RequestBody BatchSourceRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "卷宗不存在: " + dossierId));
        if (dossier.isSealed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "卷宗已封存，不可追加来源: " + dossierId);
        }
        return sourceService.submitBatch(request.sources(), dossier.factKey());
    }

    @PostMapping("/sources/batch")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    @Operation(summary = "批量提交来源（最多 5 条），不校验卷宗归属")
    public BatchSourceResponse submitBatch(@Valid @RequestBody BatchSourceRequest request) {
        return sourceService.submitBatch(request.sources(), null);
    }

    @GetMapping("/sources")
    @Operation(summary = "按 fact_key 查询当前有效来源")
    public List<SourceRecordResponse> listSources(
            @RequestParam String provinceCode,
            @RequestParam Integer admissionYear,
            @RequestParam String subjectCategory,
            @RequestParam String schoolCode,
            @RequestParam String majorGroupCode) {
        FactKey factKey = new FactKey(provinceCode, admissionYear, subjectCategory,
                schoolCode, majorGroupCode);
        Instant now = Instant.now();
        List<SourceRecord> active = sourceService.getActiveSources(factKey);
        return active.stream()
                .map(s -> SourceRecordResponse.from(s, consistencyEvaluator.freshness(s, now), true))
                .toList();
    }

    @GetMapping("/sources/history")
    @Operation(summary = "按 fact_key 查询全部来源修订历史（含旧 revision）")
    public List<SourceRecordResponse> listSourceHistory(
            @RequestParam String provinceCode,
            @RequestParam Integer admissionYear,
            @RequestParam String subjectCategory,
            @RequestParam String schoolCode,
            @RequestParam String majorGroupCode) {
        FactKey factKey = new FactKey(provinceCode, admissionYear, subjectCategory,
                schoolCode, majorGroupCode);
        Instant now = Instant.now();
        List<SourceRecord> all = sourceService.getAllSources(factKey);
        java.util.Map<String, Integer> maxRev = new java.util.HashMap<>();
        for (SourceRecord s : all) {
            maxRev.merge(s.getSourceId(), s.getSourceRevision(), Math::max);
        }
        return all.stream()
                .map(s -> SourceRecordResponse.from(s, consistencyEvaluator.freshness(s, now),
                        s.getSourceRevision() == maxRev.get(s.getSourceId())))
                .toList();
    }
}
