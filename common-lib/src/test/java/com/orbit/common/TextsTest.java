package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void detectsBlankValues() {
        assertTrue(Texts.isBlank(null));
        assertTrue(Texts.isBlank("   "));
        assertEquals("", Texts.emptyIfNull(null));
        assertEquals("a", Texts.emptyIfNull("a"));
    }

    @Test
    void trimsToNull() {
        assertNull(Texts.trimToNull("  "));
        assertNull(Texts.trimToNull(null));
        assertEquals("a", Texts.trimToNull(" a "));
    }

    @Test
    void capitalisesTheFirstLetter() {
        assertEquals("Ada", Texts.capitalize("ada"));
        assertNull(Texts.capitalize(null));
        assertEquals("!", Texts.capitalize("!"));
    }

    @Test
    void truncatesToALength() {
        assertEquals("abc", Texts.truncate("abcdef", 3));
        assertEquals("abcdef", Texts.truncate("abcdef", 10));
        assertEquals("", Texts.truncate(null, 3));
        assertEquals("", Texts.truncate("abc", 0));
    }

    @Test
    void truncatesWithAnEllipsis() {
        assertEquals("ab...", Texts.truncateWithEllipsis("abcdef", 5));
        assertEquals("...", Texts.truncateWithEllipsis("abcdef", 3));
        assertEquals("abc", Texts.truncateWithEllipsis("abc", 5));
    }

    @Test
    void buildsSlugs() {
        assertEquals("hello-world", Texts.slugify("Hello, World!"));
        assertEquals("a-b", Texts.slugify("  --A  B-- "));
        assertEquals("", Texts.slugify(null));
    }

    @Test
    void buildsInitials() {
        assertEquals("AL", Texts.initials("Ada Lovelace"));
        assertEquals("ABC", Texts.initials("a b c d"));
        assertEquals("", Texts.initials(" "));
    }

    @Test
    void joinsOnlyNonBlankParts() {
        assertEquals("a,b", Texts.joinNonBlank(",", "a", null, " ", "b"));
        assertEquals("", Texts.joinNonBlank(",", " ", null));
    }

    @Test
    void masksAllButTheLastCharacters() {
        assertEquals("****5678", Texts.mask("12345678", 4));
        assertEquals("********", Texts.mask("12345678", 0));
        assertEquals("12", Texts.mask("12", 4));
        assertEquals("", Texts.mask(null, 4));
    }

    @Test
    void sortsNullsLast() {
        assertEquals(0, Texts.compareNullsLast(null, null));
        assertEquals(1, Texts.compareNullsLast(null, "a"));
        assertEquals(-1, Texts.compareNullsLast("a", null));
        assertEquals(0, Texts.compareNullsLast("A", "a"));
    }
}
