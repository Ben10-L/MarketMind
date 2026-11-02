package com.marketmind.services;

import com.marketmind.models.MarketRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class FinancialServiceTest {
    @Test void budgetTemplateAddsUpToInput() {
        MarketRequest request = new MarketRequest("Open a bookstore", "MA", new BigDecimal("1000.00"), "MAD");
        var allocation = new FinancialService().allocate(request).illustrativeAllocation();
        assertEquals(5, allocation.size());
        assertEquals(0, allocation.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(new BigDecimal("1000.00")));
    }
    @Test void noBudgetMeansNoForecast() {
        MarketRequest request = new MarketRequest("Launch a service", "US", null, "USD");
        assertTrue(new FinancialService().allocate(request).illustrativeAllocation().isEmpty());
    }
}
