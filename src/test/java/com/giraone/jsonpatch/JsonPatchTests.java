package com.giraone.jsonpatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonDiff;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPatchTests {

    static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    void assertThat_JsonDiff_asJson_works() throws JsonProcessingException {

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
        JsonNode patch = JsonDiff.asJson(before, after);
        System.out.println(MAPPER.writeValueAsString(patch));

        // assert
        assertThat(patch).containsExactlyInAnyOrder(
            buildJsonPatchNode("replace", "/intValue1", 2),
            buildJsonPatchNode("remove", "/objectValue3/intSubValue1", null),
            buildJsonPatchNode("replace", "/objectValue3/stringSubValue2", "cat"),
            buildJsonPatchNode("add", "/objectValue3/stringSubValue3", "eats mouse"),
            buildJsonPatchNode("add", "/objectValue4", buildTestNode(Map.of(
                "intSubValue4", 9,
                "stringSubValue5", "lives"
            )))
        );
    }

    //------------------------------------------------------------------------------------------------------------------

    private ObjectNode buildTestNode(Map<String, Object> map) {
        return MAPPER.valueToTree(map);
    }

    private ObjectNode buildJsonPatchNode(String op, String path, Object value) {

        final ObjectNode node = new ObjectNode(FACTORY);
        node.put("op", op);
        node.put("path", path);
        if (value == null) {
            return node;
        } else if (value instanceof Integer) {
            node.put("value", (Integer) value);
        } else if (value instanceof String) {
            node.put("value", (String) value);
        } else if (value instanceof ObjectNode) {
            node.set("value", (ObjectNode) value);
        } else {
            throw new IllegalArgumentException("Tests supports only String, Integer and ObjectNode!");
        }
        return node;
    }
}
