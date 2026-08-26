package com.gsb.admission.dossier.web;

import com.gsb.admission.dossier.service.DossierService;
import com.gsb.admission.dossier.web.dto.CreateDossierRequest;
import com.gsb.admission.dossier.web.dto.DossierDetailView;
import com.gsb.admission.dossier.web.dto.DossierListResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dossiers")
public class DossierController {

    private final DossierService dossierService;

    public DossierController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @GetMapping
    public DossierListResponse listDossiers() {
        return new DossierListResponse(dossierService.listDossiers());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DossierDetailView createDossier(@Valid @RequestBody CreateDossierRequest request) {
        return dossierService.createDossier(request);
    }

    @GetMapping("/{dossierId}")
    public DossierDetailView getDossier(@PathVariable UUID dossierId,
                                        @RequestParam(name = "as_of", required = false) Instant asOf) {
        return dossierService.getDossier(dossierId, asOf);
    }
}
