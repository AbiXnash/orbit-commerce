package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void computesThePageCount() {
        assertEquals(5, new PageResult<>(List.of(), 100, 0, 20).pageCount());
        assertEquals(5, new PageResult<>(List.of(), 81, 0, 20).pageCount());
        assertEquals(0, new PageResult<>(List.of(), 0, 0, 20).pageCount());
    }

    @Test
    void reportsItsPosition() {
        PageResult<String> first = new PageResult<>(List.of("a"), 45, 0, 20);
        PageResult<String> last = new PageResult<>(List.of("b"), 45, 2, 20);
        assertTrue(first.isFirst());
        assertTrue(first.hasNext());
        assertFalse(first.hasPrevious());
        assertTrue(last.isLast());
        assertFalse(last.hasNext());
        assertTrue(last.hasPrevious());
    }

    @Test
    void mapsItsItems() {
        PageResult<String> page = new PageResult<>(List.of("a", "b"), 2, 0, 20);
        PageResult<Integer> lengths = page.map(String::length);
        assertEquals(List.of(1, 1), lengths.items());
        assertEquals(2, lengths.total());
    }

    @Test
    void buildsFromARequest() {
        PageResult<String> page = PageResult.of(List.of("a"), 21, new PageRequest(1, 20));
        assertEquals(1, page.page());
        assertEquals(20, page.size());
    }

    @Test
    void buildsAnEmptyPage() {
        PageResult<String> page = PageResult.empty(PageRequest.first());
        assertTrue(page.isEmpty());
        assertEquals(0, page.pageCount());
    }

    @Test
    void copiesTheItems() {
        PageResult<String> page = new PageResult<>(List.of("a"), 1, 0, 20);
        assertThrows(UnsupportedOperationException.class, () -> page.items().add("b"));
    }

    @Test
    void rejectsInvalidMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), -1, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 0, 0));
    }
}
