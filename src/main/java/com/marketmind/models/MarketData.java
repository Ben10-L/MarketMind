package com.marketmind.models;

import java.io.Serializable;

public record MarketData(String indicator, String year, double value, String unit, String sourceUrl)
        implements Serializable { }
