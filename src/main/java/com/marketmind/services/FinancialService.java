package com.marketmind.services;

import com.marketmind.models.FinancialAnalysis;
import com.marketmind.models.MarketRequest;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class FinancialService {
    public FinancialAnalysis allocate(MarketRequest request) {
        BigDecimal budget = request.getBudget();
        if (budget == null || budget.signum() == 0)
            return new FinancialAnalysis(Map.of(), "No budget supplied; financial projections cannot be calculated.");
        Map<String, BigDecimal> shares = new LinkedHashMap<>();
        shares.put("Research and validation (10%)", new BigDecimal("0.10"));
        shares.put("Setup and operations (30%)", new BigDecimal("0.30"));
        shares.put("Marketing tests (20%)", new BigDecimal("0.20"));
        shares.put("Working capital (25%)", new BigDecimal("0.25"));
        shares.put("Contingency reserve (15%)", new BigDecimal("0.15"));
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        shares.forEach((label, ratio) -> allocation.put(label, budget.multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP)));
        return new FinancialAnalysis(allocation, "ILLUSTRATIVE planning allocation only, not a forecast or recommendation. "
                + "Profitability requires validated local costs, prices, demand and unit economics.");
    }
}
