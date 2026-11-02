package com.marketmind.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MarketRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String description;
    private final String countryCode;
    private final BigDecimal budget;
    private final String currency;

    public MarketRequest(String description, String countryCode, BigDecimal budget, String currency) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description cannot be empty");
        if (description.length() > 4000) throw new IllegalArgumentException("Description exceeds 4000 characters");
        if (budget != null && budget.signum() < 0) throw new IllegalArgumentException("Budget must not be negative");
        this.id = UUID.randomUUID().toString();
        this.description = description.trim();
        this.countryCode = Objects.requireNonNull(countryCode).trim().toUpperCase();
        if (!this.countryCode.matches("[A-Z]{2,3}")) throw new IllegalArgumentException("Choose a valid country code");
        this.budget = budget;
        this.currency = (currency == null || currency.isBlank()) ? "USD" : currency.trim().toUpperCase();
    }
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getCountryCode() { return countryCode; }
    public BigDecimal getBudget() { return budget; }
    public String getCurrency() { return currency; }
}
