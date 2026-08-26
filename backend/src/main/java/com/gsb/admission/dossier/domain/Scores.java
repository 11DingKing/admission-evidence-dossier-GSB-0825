package com.gsb.admission.dossier.domain;

import java.util.Locale;

public final class Scores {

    private Scores() {
    }

    public static String display(long scoreTenths) {
        return String.format(Locale.ROOT, "%.1f", scoreTenths / 10.0);
    }
}
