# Using JSON Patch in Java

This projects includes example code and tests for using JSON Patch in Java based on the *zjsonpath* library:
```xml
<dependency>
    <groupId>com.flipkart.zjsonpatch</groupId>
    <artifactId>zjsonpatch</artifactId>
</dependency>
```

Test example:
```java
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
JsonNode patch = JsonDiff.asJson(before, after);

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
```

