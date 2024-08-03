/*
 * Copyright 2024 Haulmont.
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

package filter_builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.restds.impl.GenericRestFilterBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GenericRestFilterBuilderTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private void assertThatJsonEquals(String actual, String expected) throws JsonProcessingException {
        assertThat(objectMapper.readTree(actual)).isEqualTo(objectMapper.readTree(expected));
    }

    @Test
    void test() throws JsonProcessingException {
        GenericRestFilterBuilder builder = new GenericRestFilterBuilder();

        LogicalCondition condition = LogicalCondition.and(
                PropertyCondition.equal("name", "alpha").skipNullOrEmpty(),
                PropertyCondition.startsWith("email", "beta").skipNullOrEmpty()
        );

        String result = builder.build(condition);

        assertThatJsonEquals(result, """
                {
                  "group": "and",
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    },
                    {
                      "property": "email",
                      "operator": "startsWith",
                      "value": "beta"
                    }
                  ]
                }""");
    }

    @Test
    void testSkipNull() throws JsonProcessingException {
        GenericRestFilterBuilder builder = new GenericRestFilterBuilder();

        PropertyCondition firstCondition = PropertyCondition.equal("name", "alpha")
                .skipNullOrEmpty();

        PropertyCondition secondCondition = PropertyCondition.createWithParameterName("email", PropertyCondition.Operation.CONTAINS, ":email_123")
                .skipNullOrEmpty();
        secondCondition.setParameterValue(null);

        LogicalCondition condition = LogicalCondition.and(
                firstCondition,
                secondCondition
        );

        String result = builder.build(condition);

        assertThatJsonEquals(result, """
                {
                  "group": "and",
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    }
                  ]
                }""");

    }

    @Test
    void testString() throws JsonProcessingException {
        GenericRestFilterBuilder builder = new GenericRestFilterBuilder();

        String result = builder.build("""
                {
                  "property": "name",
                  "operator": "=",
                  "value": "alpha"
                }""");
        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    }
                  ]
                }""");

        result = builder.build("""
                [{
                  "property": "name",
                  "operator": "=",
                  "value": "alpha"
                }]""");
        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    }
                  ]
                }""");

        result = builder.build("""
                [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    },
                    {
                      "property": "email",
                      "operator": "=",
                      "value": "beta"
                    }
                ]""");
        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    },
                    {
                      "property": "email",
                      "operator": "=",
                      "value": "beta"
                    }
                  ]
                }""");

        result = builder.build("""
                {
                    "conditions": [
                        {
                          "property": "name",
                          "operator": "=",
                          "value": "alpha"
                        }
                    ]
                }""");
        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "name",
                      "operator": "=",
                      "value": "alpha"
                    }
                  ]
                }""");

    }

    @Test
    void testStringAndCondition() throws JsonProcessingException {
        GenericRestFilterBuilder builder = new GenericRestFilterBuilder();

        String jsonConditions = """
                {
                  "property": "field1",
                  "operator": "=",
                  "value": "value1"
                }""";

        LogicalCondition condition = LogicalCondition.and(
                PropertyCondition.equal("name", "alpha").skipNullOrEmpty(),
                PropertyCondition.startsWith("email", "beta").skipNullOrEmpty()
        );

        String result = builder.build(jsonConditions, condition, Map.of());

        assertThatJsonEquals(result, """
                {
                   "group": "and",
                   "conditions": [
                     {
                       "property": "field1",
                       "operator": "=",
                       "value": "value1"
                     },
                     {
                       "property": "name",
                       "operator": "=",
                       "value": "alpha"
                     },
                     {
                       "property": "email",
                       "operator": "startsWith",
                       "value": "beta"
                     }
                   ]
                 }""");

    }

}