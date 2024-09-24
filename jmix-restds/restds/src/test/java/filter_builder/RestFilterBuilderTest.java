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
import io.jmix.restds.impl.RestFilterBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class})
class RestFilterBuilderTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RestFilterBuilder builder;

    private void assertThatJsonEquals(String actual, String expected) throws JsonProcessingException {
        assertThat(objectMapper.readTree(actual)).isEqualTo(objectMapper.readTree(expected));
    }

    @Test
    void testCondition() throws JsonProcessingException {
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
    void testConditionSkipNull() throws JsonProcessingException {
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
    void testQuery() throws JsonProcessingException {
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
    void testQueryAndCondition() throws JsonProcessingException {
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

    @Test
    void testQueryWithParameters() throws JsonProcessingException {
        String query = """
                {
                  "property": "field1",
                  "operator": "=",
                  "parameterName": "param1"
                }""";

        String result = builder.build(query, null, Map.of("param1", "value1"));

        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "field1",
                      "operator": "=",
                      "value": "value1"
                    }
                  ]
                }""");
    }

    @Test
    void testQueryWithEntityParameter() throws JsonProcessingException {
        String query = """
                {
                  "property": "field1",
                  "operator": "=",
                  "parameterName": "param1"
                }""";

        Customer customer = new Customer();
        customer.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        String result = builder.build(query, null, Map.of("param1", customer));

        assertThatJsonEquals(result, """
                {
                  "conditions": [
                    {
                      "property": "field1",
                      "operator": "=",
                      "value": {
                        "_entityName": "Customer",
                        "id": "00000000-0000-0000-0000-000000000001",
                        "firstName": "John",
                        "lastName": "Doe"
                      }
                    }
                  ]
                }""");
    }
}