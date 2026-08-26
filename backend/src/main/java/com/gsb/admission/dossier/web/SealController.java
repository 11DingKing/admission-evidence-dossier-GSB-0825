package com.gsb.admission.dossier.web;

import com.gsb.admission.dossier.service.DossierService;
import com.gsb.admission.dossier.web.dto.SealListResponse;
import com.gsb.admission.dossier.web.dto.SealRequest;
import com.gsb.admission.dossier.web.dto.SealView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dossiers/{dossierId}/seals")
public class SealController {

    private final DossierService dossierService;

    public SealController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @GetMapping
    public SealListResponse listSeals(@PathVariable UUID dossierId) {
        return new SealListResponse(dossierService.listSeals(dossierId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SealView createSeal(@PathVariable UUID dossierId,
                               @Valid @RequestBody SealRequest request) {
        return dossierService.createSeal(dossierId, request.asOf());
    }

    @GetMapping("/{sealId}")
    public SealView getSeal(@PathVariable UUID dossierId,
                            @PathVariable UUID sealId) {
        return dossierService.getSeal(dossierId, sealId);
    }
}
