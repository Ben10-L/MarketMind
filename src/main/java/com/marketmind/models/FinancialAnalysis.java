package com.marketmind.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public record FinancialAnalysis(Map<String, BigDecimal> illustrativeAllocation, String note)
        implements Serializable { }
