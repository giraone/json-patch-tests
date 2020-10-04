# Using JSON Patch in Java

This projects includes example code and tests for using JSON Patch and JSON Diff in Java.
 
The used libraries are:
```xml
<dependency>
    <groupId>com.flipkart.zjsonpatch</groupId>
    <artifactId>zjsonpatch</artifactId>
	<version>0.4.11</version>
</dependency>
```

and

```xml
<dependency>
    <groupId>com.github.java-json-tools</groupId>
    <artifactId>json-patch</artifactId>
    <version>1.13</version>
</dependency>
```

Both libraries share the same method signatures and are compared with `@ParameterizedTest`.
	
## Functional tests

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

## Performance tests

| Method                                  | `com.flipkart.zjsonpatch:zjsonpatch` | `com.github.java-json-tools:json-patch` |
|-----------------------------------------|--------------------------------------|-----------------------------------------|
| patch = JsonDiff.asJson(before, after)  | 5 - 50 ms                            | 10 - 150 ms                             |
| after = JsonPatch.apply(patch, before)  | 3 ms                                 | 5 ms                                    |
| JsonPatch.applyInPlace(patch, before)   | 2 - 3 ms                             | n/a                                     |

The tests are simple Unit tests (no usage of [JMH](http://tutorials.jenkov.com/java-performance/jmh.html)),
but they use a warm-up phase and a configurable numbers of loops.

Test environment for figures above:
- OS: `Windows 10.0.18362`
- CPU: `i7-4910MQ 2.9 GHz`
- Java:
```
openjdk 11.0.8 2020-07-14
OpenJDK Runtime Environment JBR-11.0.8.10-944.31-jcef (build 11.0.8+10-b944.31)
OpenJDK 64-Bit Server VM JBR-11.0.8.10-944.31-jcef (build 11.0.8+10-b944.31, mixed mode)
```

Result: `com.flipkart.zjsonpatch:zjsonpatch` shows a better performance.

## Hints

- When using *com.flipkart.zjsonpatch* and `DiffFlags.dontNormalizeOpIntoMoveAndCopy()``the remove
  operations contains also the removed value. If this flags is not used and if *com.github.java-json-tools*,
  the value of a remove op is empty.