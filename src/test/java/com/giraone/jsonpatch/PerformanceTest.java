package com.giraone.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static com.giraone.jsonpatch.JsonTestUtils.buildTestNode;
import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTest {

    @ParameterizedTest
    @CsvSource({
        "false, 1000, true",
        "true, 1000, true",
        "false, 10000, false",
        "true, 10000, false",
        "false, 100000, false",
        "true, 100000, false"
    })
    void simpleLoopTest_for_JsonDiff_asJson(boolean useFge, int loops, boolean warmup) {

        // Run
        long totalNanos = 0L;
        for (int i = 0; i < loops; i++) {
            totalNanos += oneRun(i, useFge);
        }

        final long msPerCall = totalNanos / loops / 1000;
        System.out.println("Average time for " + (useFge ? "FgeJsonPatch" : "FlipkartZJsonPatch") +
            " JsonDiff.asJson() = " + msPerCall + " ms");
        if (!warmup) {
            assertThat(msPerCall).isLessThan(50L);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    long oneRun(int i, boolean useFge) {

        ObjectNode before = JsonTestUtils.buildTestNode(Map.of(
            "intValue1", 1 + i,
            "stringValue2", "animal",
            "objectValue3", Map.of(
                "intSubValue1", 10 * i,
                "stringSubValue2", "dog"
            )
        ));
        ObjectNode after = buildTestNode(Map.of(
            "intValue1", 2 + i, // replace
            "stringValue2", "animal",
            "objectValue3", Map.of(
                // remove
                "stringSubValue2", "cat", // replace
                "stringSubValue3", "eats mouse"
            ),
            "objectValue4", Map.of( // add
                "intSubValue4", 9,
                "stringSubValue5", "lives"
            )
        ));

        final long start = System.nanoTime();
        JsonNode patch = useFge
            ? com.github.fge.jsonpatch.diff.JsonDiff.asJson(before, after)
            : com.flipkart.zjsonpatch.JsonDiff.asJson(before, after);
        final long end = System.nanoTime();
        assertThat(patch.size()).isEqualTo(5);
        return end - start;
    }
}
