/*
 * Copyright 2026 Haulmont.
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

package repair;

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.repair.JpqlRepairRequest;
import io.jmix.aitools.dataload.repair.JpqlRepairSchemaComposer;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.core.security.SystemAuthenticator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;
import test_support.TestEntityAttributeViewConstraint;

import java.util.Arrays;
import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.ReservedWordAliasValidator.RESERVED_ALIAS_CODE;
import static io.jmix.aitools.dataload.validation.validator.SelectedValuesValidator.SELECTED_ENTITY_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedEntitiesValidator.USED_ENTITY_UNKNOWN_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedPropertyPathsValidator.PROPERTY_PATH_INVALID_CODE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class JpqlRepairSchemaComposerTest {

    @Autowired
    JpqlRepairSchemaComposer composer;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    TestEntityAttributeViewConstraint attributeViewConstraint;

    @AfterEach
    void tearDown() {
        attributeViewConstraint.clear();
    }

    @Test
    void compose_invalidPath_describesUsedEntityAndEntityReachedByValidPart() {
        String schema = compose("select o.number as num from aitls_Order o where o.customer.nam = 'Acme'",
                PROPERTY_PATH_INVALID_CODE);

        assertNotNull(schema);
        String orderLine = entityLine(schema, "aitls_Order");
        assertTrue(orderLine.contains("number String"));
        assertTrue(orderLine.contains("customer -> aitls_Customer (MANY_TO_ONE)"));
        assertTrue(entityLine(schema, "aitls_Customer").contains("name String"));
        // An entity the order refers to but the query does not reach is only named, not described.
        assertNull(findEntityLine(schema, "aitls_OrderLine"));
        assertFalse(schema.contains("All available entities"));
    }

    @Test
    void compose_invalidPathInJoinedEntity_describesJoinedEntity() {
        String schema = compose("select c.nam as name from aitls_Order o join o.customer c",
                PROPERTY_PATH_INVALID_CODE);

        assertNotNull(schema);
        assertNotNull(findEntityLine(schema, "aitls_Order"));
        assertTrue(entityLine(schema, "aitls_Customer").contains("name String"));
    }

    @Test
    void compose_invalidPathThroughEmbedded_describesEmbeddable() {
        String schema = compose("select o.address.cty as city from aitls_Order o", PROPERTY_PATH_INVALID_CODE);

        assertNotNull(schema);
        assertTrue(entityLine(schema, "aitls_Order").contains("address embedded aitls_Address"));
        assertTrue(entityLine(schema, "aitls_Address").contains("city String"));
    }

    @Test
    void compose_selectedEntity_describesSelectedEntity() {
        String schema = compose("select o.customer as customer from aitls_Order o", SELECTED_ENTITY_CODE);

        assertNotNull(schema);
        assertTrue(entityLine(schema, "aitls_Customer").contains("name String"));
    }

    @Test
    void compose_unknownEntity_listsAvailableEntityNames() {
        String schema = compose("select c.name as name from aitls_Custmer c", USED_ENTITY_UNKNOWN_CODE);

        assertNotNull(schema);
        String entityList = schema.lines()
                .filter(line -> line.startsWith("All available entities: "))
                .findFirst()
                .orElseThrow();
        List<String> entityNames = Arrays.asList(
                entityList.substring("All available entities: ".length()).split(", "));
        assertTrue(entityNames.contains("aitls_Customer"));
        assertTrue(entityNames.contains("aitls_Order"));
        assertFalse(entityNames.contains("aitls_HiddenEntity"));
    }

    @Test
    void compose_issueNotAboutNames_returnsNull() {
        assertNull(compose("select o.number as user from aitls_Order o", RESERVED_ALIAS_CODE));
    }

    @Test
    void compose_hiddenAttributes_notDescribed() {
        attributeViewConstraint.denyView("aitls_Customer", "name");

        String schema = compose("select c.nam as name from aitls_Customer c", PROPERTY_PATH_INVALID_CODE);

        assertNotNull(schema);
        String customerLine = entityLine(schema, "aitls_Customer");
        assertTrue(customerLine.contains("id Long"));
        // Denied by security.
        assertFalse(customerLine.contains("name String"));
        // @Secret, @SystemLevel and @ExcludeFromAi attributes, hidden from discovery.
        assertFalse(customerLine.contains("secretToken"));
        assertFalse(customerLine.contains("systemNote"));
        assertFalse(customerLine.contains("piiPhone"));
    }

    @Test
    void compose_pathThroughHiddenReference_doesNotDescribeTheReference() {
        attributeViewConstraint.denyView("aitls_Order", "customer");

        String schema = compose("select o.customer.nam as name from aitls_Order o", PROPERTY_PATH_INVALID_CODE);

        assertNotNull(schema);
        assertFalse(entityLine(schema, "aitls_Order").contains("customer"));
    }

    @Nullable
    String compose(String jpql, String issueCode) {
        JpqlExecutionRequest executionRequest = new JpqlExecutionRequest();
        executionRequest.setUserText("request");
        JpqlRepairRequest request = new JpqlRepairRequest(
                executionRequest,
                new GeneratedJpqlResult(jpql, List.of(), "", List.of()),
                new JpqlValidationResult(false, List.of(new JpqlValidationIssue(issueCode, "Issue"))),
                1);

        systemAuthenticator.begin();
        try {
            return composer.compose(request);
        } finally {
            systemAuthenticator.end();
        }
    }

    String entityLine(String schema, String entityName) {
        String line = findEntityLine(schema, entityName);
        assertNotNull(line, () -> "No line for " + entityName + " in schema:\n" + schema);
        return line;
    }

    @Nullable
    String findEntityLine(String schema, String entityName) {
        return schema.lines()
                .filter(line -> line.startsWith(entityName + ": "))
                .findFirst()
                .orElse(null);
    }
}
