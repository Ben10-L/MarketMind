package com.marketmind.models;

import java.io.Serializable;

public record Competitor(String name, String description, String url) implements Serializable { }
