package com.giraone.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.giraone.jsonpatch.JsonTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTest {

    private static final EnumSet<DiffFlags> flags_flipkart = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone();

    @ParameterizedTest
    @CsvSource({
        "false, 10000, true",
        "true, 10000, true",
        "false, 100000, false",
        "true, 100000, false"
    })
    void loopTest_for_JsonDiff_asJson_smallSource(boolean useFge, int loops, boolean warmup) {

        // Run
        long totalNanos = 0L;
        for (int i = 0; i < loops; i++) {
            totalNanos += oneRunJsonDiffAsJson_smallSource(i, useFge);
        }

        final long msPerCall = totalNanos / loops / 1000;
        System.out.println("Average time for " + (useFge ? "FgeJsonPatch" : "FlipkartZJsonPatch") +
            " JsonDiff.asJson() = " + msPerCall + " ms");
        if (!warmup) {
            assertThat(msPerCall).isLessThan(50L);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, 10000, 100, 10, true",
        "true, 10000, 100, 10, true",
        "false, 100000, 100, 10, false",
        "true, 100000, 100, 10, false",
        "false, 100000, 200, 20, false",
        "true, 100000, 200, 20, false"
    })
    void loopTest_for_JsonDiff_asJson_largeSource(boolean useFge, int loops, int nrOfFields, int nrOfChanges, boolean warmup) {

        // Run
        long totalNanos = 0L;
        for (int i = 0; i < loops; i++) {
            totalNanos += oneRunJsonDiffAsJson_largeSource(useFge, nrOfFields, nrOfChanges);
        }

        final long msPerCall = totalNanos / loops / 1000;
        System.out.println("Average time for " + (useFge ? "FgeJsonPatch" : "FlipkartZJsonPatch") +
            " JsonDiff.asJson() = " + msPerCall + " ms");
        if (!warmup) {
            assertThat(msPerCall).isLessThan(50L);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, 10000, true",
        "false, true, 10000, true",
        "true, false, 10000, true",
        "false, false, 100000, false",
        "false, true, 100000, false",
        "true, false, 100000, false"
    })
    void loopTest_for_JsonPatch_apply(boolean useFge, boolean inPlace, int loops, boolean warmup) throws JsonPatchException, IOException {

        // Run
        long totalNanos = 0L;
        for (int i = 0; i < loops; i++) {
            totalNanos += oneRunJsonPatchApply(i, useFge, inPlace);
        }

        final long msPerCall = totalNanos / loops / 1000;
        System.out.println("Average time for " + (useFge ? "FgeJsonPatch" : "FlipkartZJsonPatch") +
            " JsonDiff.asJson() = " + msPerCall + " ms");
        if (!warmup) {
            assertThat(msPerCall).isLessThan(50L);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    long oneRunJsonDiffAsJson_smallSource(int i, boolean useFge) {

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

        final long start, end;
        JsonNode patch;
        if (useFge) {
            start = System.nanoTime();
            patch = com.github.fge.jsonpatch.diff.JsonDiff.asJson(before, after);
            end = System.nanoTime();
        } else {
            start = System.nanoTime();
            patch = com.flipkart.zjsonpatch.JsonDiff.asJson(before, after, flags_flipkart);
            end = System.nanoTime();
        }
        assertThat(patch.size()).isEqualTo(5);
        return end - start;
    }

    long oneRunJsonDiffAsJson_largeSource(boolean useFge, int nrOfFields, int nrOfChanges) {

        ObjectNode before = JsonTestUtils.buildLargeTestNode(nrOfFields);
        ObjectNode after = JsonTestUtils.modifyTestNode(before, nrOfChanges);

        final long start, end;
        JsonNode patch;
        if (useFge) {
            start = System.nanoTime();
            patch = com.github.fge.jsonpatch.diff.JsonDiff.asJson(before, after);
            end = System.nanoTime();
        } else {
            start = System.nanoTime();
            patch = com.flipkart.zjsonpatch.JsonDiff.asJson(before, after, flags_flipkart);
            end = System.nanoTime();
        }
        assertThat(patch.size()).isEqualTo(nrOfChanges);
        return end - start;
    }

    long oneRunJsonPatchApply(int i, boolean useFge, boolean inPlace) throws JsonPatchException, IOException {

        // arrange
        ObjectNode before = buildTestNode(Map.of(
            "intValue1", 1 + i,
            "stringValue2", "animal",
            "objectValue3", Map.of(
                "intSubValue1", 10,
                "stringSubValue2", "dog"
            )
        ));

        JsonNode jsonPatchAsJsonNode = buildJsonPatchNodes(List.of(
            buildJsonPatchNode("replace", "/intValue1", 2 + i),
            buildJsonPatchNode("remove", "/objectValue3/intSubValue1", null),
            buildJsonPatchNode("replace", "/objectValue3/stringSubValue2", "cat"),
            buildJsonPatchNode("add", "/objectValue3/stringSubValue3", "eats mouse"),
            buildJsonPatchNode("add", "/objectValue4", buildTestNode(Map.of(
                "intSubValue4", 9,
                "stringSubValue5", "lives"
                ))
            )));

        // act
        JsonNode result;
        final long start, end;
        if (useFge) {
            start = System.nanoTime();
            final com.github.fge.jsonpatch.JsonPatch jsonPatch =
                com.github.fge.jsonpatch.JsonPatch.fromJson(jsonPatchAsJsonNode);
            result = jsonPatch.apply(before);
            end = System.nanoTime();
        } else {
            if (inPlace) {
                start = System.nanoTime();
                com.flipkart.zjsonpatch.JsonPatch.applyInPlace(jsonPatchAsJsonNode, before);
                result = before;
                end = System.nanoTime();
            } else {
                start = System.nanoTime();
                result = com.flipkart.zjsonpatch.JsonPatch.apply(jsonPatchAsJsonNode, before);
                end = System.nanoTime();
            }
        }

        // assert
        assertThat(result.size()).isEqualTo(4);
        return end - start;
    }
}
