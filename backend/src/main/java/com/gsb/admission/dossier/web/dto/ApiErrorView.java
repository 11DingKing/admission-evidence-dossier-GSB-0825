package com.gsb.admission.dossier.web.dto;

import java.util.Map;

public record ApiErrorView(String errorCode, String message, Map<String, Object> details) {
}
