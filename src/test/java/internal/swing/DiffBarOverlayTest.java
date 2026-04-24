package internal.swing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffBarOverlayTest {

    @Test
    void identity() {
        // No changes — every item maps to its own index
        assertMapping(list("A", "B", "C"), list("A", "B", "C"), 0, 1, 2);
    }

    @Test
    void emptyBoth() {
        // Empty original and empty current — nothing to map
        assertMapping(list(), list());
    }

    @Test
    void fullDelete() {
        // All items removed — current is empty, mapping is empty
        assertMapping(list("A", "B", "C"), list());
    }

    @Test
    void fullAdd() {
        // Original was empty — all current items are new
        assertMapping(list(), list("A", "B", "C"), -1, -1, -1);
    }

    @Test
    void deleteFirst() {
        // A deleted: B and C shift up but must stay unmarked
        assertMapping(list("A", "B", "C"), list("B", "C"), 1, 2);
    }

    @Test
    void deleteMiddle() {
        // B deleted: A and C stay at their logical positions
        assertMapping(list("A", "B", "C"), list("A", "C"), 0, 2);
    }

    @Test
    void deleteLast() {
        // C deleted: A and B unchanged
        assertMapping(list("A", "B", "C"), list("A", "B"), 0, 1);
    }

    @Test
    void addFirst() {
        // X prepended: B and C shift right but stay matched
        assertMapping(list("B", "C"), list("X", "B", "C"), -1, 0, 1);
    }

    @Test
    void addMiddle() {
        // X inserted between A and B
        assertMapping(list("A", "B"), list("A", "X", "B"), 0, -1, 1);
    }

    @Test
    void addLast() {
        // Z appended: A and B unchanged
        assertMapping(list("A", "B"), list("A", "B", "Z"), 0, 1, -1);
    }

    @Test
    void permutation() {
        // [A,B] → [B,A]: LCS is length 1; the algorithm matches B to original[1]
        assertMapping(list("A", "B"), list("B", "A"), 1, -1);
    }

    @Test
    void singleElement_unchanged() {
        assertMapping(list("A"), list("A"), 0);
    }

    @Test
    void singleElement_deleted() {
        assertMapping(list("A"), list());
    }

    @Test
    void singleElement_added() {
        assertMapping(list(), list("A"), -1);
    }

    // --- helpers ---

    private static void assertMapping(List<?> original, List<?> current, int... expected) {
        assertThat(DiffBarOverlay.computeMapping(original, current)).isEqualTo(expected);
    }

    private static List<String> list(String... items) {
        return Arrays.asList(items);
    }
}

