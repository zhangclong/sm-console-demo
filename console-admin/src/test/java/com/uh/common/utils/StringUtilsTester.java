package com.uh.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTester {

    @Test
    public void toReadableSize() {
        assertEquals(StringUtils.toReadableSize(3300), "3.2KB");
        assertEquals(StringUtils.toReadableSize(2048), "2.0KB");
        assertEquals(StringUtils.toReadableSize(1024*1024), "1.0MB");
        assertEquals(StringUtils.toReadableSize(1024L*1024L*1024L*1020L), "1020GB");
        assertEquals(StringUtils.toReadableSize(1099511627776L), "1.0TB");
        assertEquals(StringUtils.toReadableSize(24L*1024L*1024L*1024L*1024L), "24.0TB");
        assertEquals(StringUtils.toReadableSize(2L*1024L*1024L*1024L*1024L*1024L*1024L), "2048PB");
    }

    @Test
    public void testPatternMatch() {
        assertTrue(StringUtils.matchesGlobPattern("hello", "h?llo"));
        assertTrue(StringUtils.matchesGlobPattern("hallo", "h?llo"));
        assertTrue(StringUtils.matchesGlobPattern("hhllo", "h?llo"));
        assertTrue(StringUtils.matchesGlobPattern("hllo", "h*llo"));
        assertTrue(StringUtils.matchesGlobPattern("heeeello", "h*llo"));
        assertTrue(StringUtils.matchesGlobPattern("my-key-0", "*-key-*"));
        assertTrue(StringUtils.matchesGlobPattern("hello", "h[ae]llo"));
        assertTrue(StringUtils.matchesGlobPattern("hallo", "h[ae]llo"));
        assertFalse(StringUtils.matchesGlobPattern("hillo", "h[ae]llo"));
        assertFalse(StringUtils.matchesGlobPattern("hello", "h\\?llo"));
        assertTrue(StringUtils.matchesGlobPattern("h?llo", "h\\?llo"));
    }

}
