package com.marketmind.services;

import com.marketmind.models.MarketRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MarketRequestTest {
    @Test void requestIsDynamicAndValidatesInputs() {
        var book = new MarketRequest("Bookstore", "ma", null, "MAD");
        var hotel = new MarketRequest("Hotel", "fr", BigDecimal.TEN, "EUR");
        assertNotEquals(book.getId(), hotel.getId());
        assertEquals("MA", book.getCountryCode());
        assertEquals("Hotel", hotel.getDescription());
        assertThrows(IllegalArgumentException.class, () -> new MarketRequest("  ", "MA", null, "MAD"));
        assertThrows(IllegalArgumentException.class, () -> new MarketRequest("Startup", "MA", new BigDecimal("-3"), "MAD"));
    }
}
