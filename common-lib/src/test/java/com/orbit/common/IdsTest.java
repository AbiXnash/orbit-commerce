package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IdsTest {

    @Test
    void normalisesIdentifiers() {
        assertEquals("AB-12", Ids.normalize(" ab_12 "));
        assertEquals("A-B", Ids.normalize("a--b"));
        assertNull(Ids.normalize(null));
    }

    @Test
    void validatesIdentifiers() {
        assertTrue(Ids.isValid("AB-12"));
        assertTrue(Ids.isValid("A1"));
        assertFalse(Ids.isValid("A"));
        assertFalse(Ids.isValid("-AB"));
        assertFalse(Ids.isValid("ab-12"));
        assertFalse(Ids.isValid(null));
    }

    @Test
    void requiresAValidIdentifier() {
        assertEquals("AB-12", Ids.requireValid(" ab_12 "));
        assertThrows(IllegalArgumentException.class, () -> Ids.requireValid("x"));
        assertThrows(IllegalArgumentException.class, () -> Ids.requireValid(null));
    }

    @Test
    void buildsTheNextIdentifier() {
        assertEquals("ORD-000042", Ids.next("ord", 42));
    }

    @Test
    void rejectsAnEmptyPrefixOrSequence() {
        assertThrows(IllegalArgumentException.class, () -> Ids.next(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> Ids.next("ORD", 0));
    }
}
