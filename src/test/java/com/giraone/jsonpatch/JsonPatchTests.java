package com.giraone.jsonpatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.giraone.jsonpatch.JsonTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class JsonPatchTests {

    private static EnumSet<DiffFlags> flags_flipkart = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void assertThat_JsonDiff_asJson_works(boolean useFge) throws JsonProcessingException {

        // arrange
        ObjectNode before = buildTestNode(Map.of(
            "intValue1", 1,
            "stringValue2", "animal",
            "objectValue3", Map.of(
                "intSubValue1", 10,
                "stringSubValue2", "dog"
            )
        ));
        ObjectNode after = buildTestNode(Map.of(
            "intValue1", 2, // replace
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

        // act
        System.out.println(MAPPER.writeValueAsString(before));
        System.out.println(MAPPER.writeValueAsString(after));
        JsonNode patch;
        if (useFge) {
            patch = com.github.fge.jsonpatch.diff.JsonDiff.asJson(before, after);

        } else {
            patch = com.flipkart.zjsonpatch.JsonDiff.asJson(before, after, flags_flipkart);
        }
        System.out.println(MAPPER.writeValueAsString(patch));

        // assert
        assertThat(patch).containsExactlyInAnyOrder(
            buildJsonPatchNode("replace", "/intValue1", 2),
            useFge
                ? buildJsonPatchNode("remove", "/objectValue3/intSubValue1", null)
                : buildJsonPatchNode("remove", "/objectValue3/intSubValue1", 10),
            buildJsonPatchNode("replace", "/objectValue3/stringSubValue2", "cat"),
            buildJsonPatchNode("add", "/objectValue3/stringSubValue3", "eats mouse"),
            buildJsonPatchNode("add", "/objectValue4", buildTestNode(Map.of(
                "intSubValue4", 9,
                "stringSubValue5", "lives"
            )))
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void assertThat_JsonPatch_apply_works(boolean useFge) throws IOException, JsonPatchException {

        // arrange
        ObjectNode before = buildTestNode(Map.of(
            "intValue1", 1,
            "stringValue2", "animal",
            "objectValue3", Map.of(
                "intSubValue1", 10,
                "stringSubValue2", "dog"
            )
        ));

        JsonNode jsonPatchAsJsonNode = buildJsonPatchNodes(List.of(
            buildJsonPatchNode("replace", "/intValue1", 2),
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
        if (useFge) {
            final com.github.fge.jsonpatch.JsonPatch jsonPatch =
                com.github.fge.jsonpatch.JsonPatch.fromJson(jsonPatchAsJsonNode);
            result = jsonPatch.apply(before);
        } else {
            result = com.flipkart.zjsonpatch.JsonPatch.apply(jsonPatchAsJsonNode, before);
        }
        System.out.println(MAPPER.writeValueAsString(result));

        // assert
        ObjectNode expectedAfter = buildTestNode(Map.of(
            "intValue1", 2, // replace
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
        assertThat(result).isEqualTo(expectedAfter);
    }
}
