package com.giraone.jsonpatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
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

    public static ArrayNode buildJsonPatchNodes(List<ObjectNode> patchNodes) {

        final ArrayNode node = new ArrayNode(FACTORY);
        node.addAll(patchNodes);
        return node;
    }

    public static ObjectNode buildLargeTestNode(int nrOfTopLevelFields) {
        final ObjectNode node = new ObjectNode(FACTORY);
        for (int i = 0; i < nrOfTopLevelFields; i++) {
            node.put(String.format("field%08d", i), i);
        }
        return node;
    }

    public static ObjectNode modifyTestNode(ObjectNode before, int fieldChanges) {

        final ObjectNode node = before.deepCopy();
        for (int i = 0; i < fieldChanges; i++) {
            int changedIndex = before.size() - 1 - i;
            node.put(String.format("field%08d", changedIndex), changedIndex + 4711);
        }
        return node;
    }
}
