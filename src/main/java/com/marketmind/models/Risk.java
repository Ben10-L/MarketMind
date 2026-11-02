package com.marketmind.models;

import java.io.Serializable;

public record Risk(String title, String mitigation) implements Serializable { }
