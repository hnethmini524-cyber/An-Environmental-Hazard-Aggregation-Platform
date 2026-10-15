package com.openhazard.app.enums;

public enum RiskLevel {
    LOW(1),
    MODERATE(2),
    HIGH(3),
    CRITICAL(4);

    private final int severityRank;

    RiskLevel(int severityRank) {
        this.severityRank = severityRank;
    }

    public int getSeverityRank() {
        return severityRank;
    }

    public static RiskLevel max(RiskLevel r1, RiskLevel r2) {
        if (r1 == null) return r2;
        if (r2 == null) return r1;
        return r1.getSeverityRank() >= r2.getSeverityRank() ? r1 : r2;
    }
}
