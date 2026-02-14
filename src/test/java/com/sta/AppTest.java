package com.sta;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for the Static Code Analyzer.
 */
class AppTest {

    @Test
    void testTrue() {
        assertTrue(true);
    }

    @Test
    void testMainClassExists() {
        assertNotNull(Main.class);
    }
}
