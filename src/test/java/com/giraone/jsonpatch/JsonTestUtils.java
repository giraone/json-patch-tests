package com.giraone.jsonpatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

class JsonTestUtils {

    static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Hide
    private JsonTestUtils() {
    }

    public static ObjectNode buildTestNode(Map<String, Object> map) {
        return MAPPER.valueToTree(map);
    }

    public static ObjectNode buildJsonPatchNode(String op, String path, Object value) {

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