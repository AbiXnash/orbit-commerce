package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    void computesTheOffset() {
        assertEquals(40, new PageRequest(2, 20).offset());
        assertEquals(20, new PageRequest(2, 20).limit());
    }

    @Test
    void rejectsANegativePage() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(-1, 20));
    }

    @Test
    void rejectsAnUnusableSize() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 201));
    }

    @Test
    void startsAtTheFirstPage() {
        assertEquals(new PageRequest(0, 20), PageRequest.first());
        assertEquals(new PageRequest(0, 50), PageRequest.first(50));
    }

    @Test
    void movesForwardsAndBackwards() {
        assertEquals(new PageRequest(2, 20), new PageRequest(1, 20).next());
        assertEquals(new PageRequest(0, 20), new PageRequest(1, 20).previous());
        assertEquals(new PageRequest(0, 20), new PageRequest(0, 20).previous());
    }

    @Test
    void changesTheSizeOnTheSamePage() {
        assertEquals(new PageRequest(3, 50), new PageRequest(3, 20).withSize(50));
    }
}
