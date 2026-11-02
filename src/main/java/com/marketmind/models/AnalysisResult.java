package com.marketmind.models;

import java.io.Serializable;

public class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String requestId;
    private final String report;
    private final String reportFile;
    public AnalysisResult(String requestId, String report, String reportFile) {
        this.requestId = requestId;
        this.report = report;
        this.reportFile = reportFile;
    }
    public String getRequestId() { return requestId; }
    public String getReport() { return report; }
    public String getReportFile() { return reportFile; }
}
