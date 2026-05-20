package org.sid.pfespring.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.util.*;

public final class ExcelTheme {

    private ExcelTheme() {}

    // ══════════════════════════════════════════════════════════════
    //  COULEURS STRUCTURE
    // ══════════════════════════════════════════════════════════════
    public static final String HEADER_BG  = "1F4E79";
    public static final String HEADER_FG  = "FFFFFF";
    public static final String ROW_PAIR   = "FAFAFA";
    public static final String ROW_IMPAIR = "FFFFFF";

    // ══════════════════════════════════════════════════════════════
    //  PALETTES
    // ══════════════════════════════════════════════════════════════
    public static final String[] PROF_PALETTE = {
            "FFF2CC","FFE0B2","F8BBD0","E1BEE7","C5CAE9",
            "B2EBF2","DCEDC8","FFE0E0","F0F4C3","E8F5E9",
            "FCE4EC","E3F2FD","FFF3E0","F3E5F5","E0F7FA",
            "EFEBE9","F9FBE7","E8EAF6","E0F2F1","FBE9E7",
            "F1F8E9","EDE7F6","FFFDE7","FFF8E1","EEF2FF",
            "E0F7FA","FFF0F3","F3E5F5","E8F5E9","FCE4EC"
    };

    public static final String[] DATE_PALETTE = {
            "EBF5FB","E9F7EF","FEF9E7","F5EEF8"
    };

    // ══════════════════════════════════════════════════════════════
    //  MAPS SÉMANTIQUES
    // ══════════════════════════════════════════════════════════════
    public static final Map<String, String> CRENEAU_COLORS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("09:00", "D6E4F0");
        m.put("11:00", "D5F5E3");
        m.put("14:00", "FAD7A0");
        m.put("16:00", "F9EBEA");
        CRENEAU_COLORS = Collections.unmodifiableMap(m);
    }

    public static final Map<String, String> FILIERE_COLORS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("ID",   "D6EAF8");
        m.put("GI",   "D5F5E3");
        m.put("TDIA", "FDEBD0");
        FILIERE_COLORS = Collections.unmodifiableMap(m);
    }

    // Anomalies
    public static final String ANOMALIE_OK_BG  = "C6EFCE";
    public static final String ANOMALIE_OK_FG  = "276221";
    public static final String ANOMALIE_ERR_BG = "FFC7CE";
    public static final String ANOMALIE_ERR_FG = "9C0006";

    // ══════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════════════
    public static byte[] hexToBytes(String hex) {
        return new byte[]{
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
    }
}