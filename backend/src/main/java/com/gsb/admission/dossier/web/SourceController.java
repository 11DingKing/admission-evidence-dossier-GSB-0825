package com.gsb.admission.dossier.web;

import com.gsb.admission.dossier.service.DossierService;
import com.gsb.admission.dossier.web.dto.FetchReportView;
import com.gsb.admission.dossier.web.dto.FetchRequest;
import com.gsb.admission.dossier.web.dto.ManualSourceRecordRequest;
import com.gsb.admission.dossier.web.dto.RevisionChainView;
import com.gsb.admission.dossier.web.dto.SourceRecordView;
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
@RequestMapping("/api/v1/dossiers/{dossierId}")
public class SourceController {

    private final DossierService dossierService;

    public SourceController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @PostMapping("/sources/fetch")
    public FetchReportView fetchSources(@PathVariable UUID dossierId,
                                        @RequestBody(required = false) FetchRequest request) {
        return dossierService.fetchSources(dossierId, request == null ? null : request.asOf());
    }

    @PostMapping("/sources")
    @ResponseStatus(HttpStatus.CREATED)
    public SourceRecordView addSourceRecord(@PathVariable UUID dossierId,
                                            @Valid @RequestBody ManualSourceRecordRequest request) {
        return dossierService.addSourceRecord(dossierId, request);
    }

    @GetMapping("/sources/{sourceId}/revisions")
    public RevisionChainView getSourceRevisions(@PathVariable UUID dossierId,
                                                @PathVariable String sourceId) {
        return dossierService.getRevisions(dossierId, sourceId);
    }
}
