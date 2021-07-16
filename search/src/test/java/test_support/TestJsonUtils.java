/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test_support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TestJsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean areEqualIgnoringOrder(final JsonNode firstValue, final JsonNode secondValue) {
        if (!firstValue.getNodeType().equals(secondValue.getNodeType())) {
            return false;
        }
        switch (firstValue.getNodeType()) {
            case OBJECT:
                final ObjectNode firstObject = (ObjectNode) firstValue;
                final ObjectNode secondObject = (ObjectNode) secondValue;
                Set<String> firstKeySet = getKeyStream(firstObject).collect(Collectors.toSet());
                Set<String> secondKeySet = getKeyStream(secondObject).collect(Collectors.toSet());

                if (!firstKeySet.equals(secondKeySet)) {
                    return false;
                }

                return firstKeySet.stream()
                        .map(key -> areEqualIgnoringOrder(firstObject.get(key), secondObject.get(key)))
                        .reduce(true, (a, b) -> a && b);

            case ARRAY:
                final ArrayNode firstArray = (ArrayNode) firstValue;
                final ArrayNode secondArray = (ArrayNode) secondValue;
                if (firstArray.size() != secondArray.size()) {
                    return false;
                }
                if (firstArray.isEmpty()) {
                    return true;
                }
                for (final JsonNode itemOfFirstArray : firstArray) {
                    if (StreamSupport.stream(secondArray.spliterator(), false)
                            .noneMatch(itemOfSecondArray -> areEqualIgnoringOrder(itemOfFirstArray, itemOfSecondArray))) {
                        return false;
                    }
                }
                return true;
            default:
                return firstValue.equals(secondValue);
        }
    }

    public static Stream<String> getKeyStream(ObjectNode objectNode) {
        Iterable<String> iterable = objectNode::fieldNames;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static JsonNode readJsonFromFile(String path) {
        try {
            URL resource = ClassLoader.getSystemClassLoader().getResource(path);
            return objectMapper.readTree(resource);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read json from file '%s'", path), e);
        }
    }
}
