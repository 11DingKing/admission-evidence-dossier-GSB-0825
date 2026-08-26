package com.dossier.admission.web;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.FactKey;
import com.dossier.admission.service.DossierService;
import com.dossier.admission.web.dto.CreateDossierRequest;
import com.dossier.admission.web.dto.DossierResponse;
import com.dossier.admission.web.dto.SealDossierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dossiers")
@Tag(name = "Dossiers", description = "卷宗创建、封存与查询")
public class DossierController {

    private final DossierService dossierService;

    public DossierController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "为 fact_key 创建卷宗")
    public DossierResponse createDossier(@Valid @RequestBody CreateDossierRequest request) {
        FactKey factKey = new FactKey(
                request.provinceCode(), request.admissionYear(), request.subjectCategory(),
                request.schoolCode(), request.majorGroupCode()
        );
        Dossier dossier = dossierService.createDossier(factKey, request.expectedScale());
        return dossierService.getDossier(dossier.getDossierId());
    }

    @PostMapping("/{dossierId}/seal")
    @Operation(summary = "按 as_of 封存不可变卷宗")
    public DossierResponse sealDossier(@PathVariable String dossierId,
                                       @Valid @RequestBody SealDossierRequest request) {
        dossierService.sealDossier(dossierId, request.asOf());
        return dossierService.getDossier(dossierId);
    }

    @GetMapping("/{dossierId}")
    @Operation(summary = "获取卷宗详情及来源证据")
    public DossierResponse getDossier(@PathVariable String dossierId) {
        return dossierService.getDossier(dossierId);
    }

    @GetMapping
    @Operation(summary = "按 fact_key 查询卷宗列表")
    public List<DossierResponse> listDossiers(
            @RequestParam String provinceCode,
            @RequestParam Integer admissionYear,
            @RequestParam String subjectCategory,
            @RequestParam String schoolCode,
            @RequestParam String majorGroupCode) {
        FactKey factKey = new FactKey(provinceCode, admissionYear, subjectCategory,
                schoolCode, majorGroupCode);
        return dossierService.listDossiers(factKey).stream()
                .map(d -> dossierService.getDossier(d.getDossierId()))
                .toList();
    }
}
