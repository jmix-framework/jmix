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

package introspection;

import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntitySummary;
import io.jmix.aitools.dataload.tool.DomainModelDiscoveryTool;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;
import test_support.TestEntityAttributeViewConstraint;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class DomainModelDiscoveryAttributeSecurityTest {

    @Autowired
    DomainModelDiscoveryTool domainModelDiscoveryTool;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    TestEntityAttributeViewConstraint attributeViewConstraint;

    @AfterEach
    void tearDown() {
        attributeViewConstraint.clear();
    }

    @Test
    @DisplayName("Excludes attributes denied by security from entity summaries")
    void testExcludesDeniedAttributesFromEntitySummaries() {
        assertTrue(getOrderSummary().getPropertyNames().contains("status"));

        attributeViewConstraint.denyView("aitls_Order", "status");

        EntitySummary orderSummary = getOrderSummary();
        assertFalse(orderSummary.getPropertyNames().contains("status"));
        assertFalse(orderSummary.getPropertyLocalizedNames().contains("Status"));
        assertTrue(orderSummary.getPropertyNames().contains("number"));
    }

    @Test
    @DisplayName("Excludes attributes denied by security from detailed entity descriptors")
    void testExcludesDeniedAttributesFromEntityDescriptors() {
        assertTrue(getOrderDescriptor().getProperties().stream()
                .anyMatch(property -> property.getName().equals("status")));

        attributeViewConstraint.denyView("aitls_Order", "status");

        EntityDescriptor orderDescriptor = getOrderDescriptor();
        assertTrue(orderDescriptor.getProperties().stream()
                .noneMatch(property -> property.getName().equals("status")));
        assertTrue(orderDescriptor.getProperties().stream()
                .anyMatch(property -> property.getName().equals("number")));
    }

    EntitySummary getOrderSummary() {
        systemAuthenticator.begin();
        try {
            return domainModelDiscoveryTool.getAvailableEntities(new ToolContext(Map.of())).stream()
                    .filter(summary -> summary.getEntityName().equals("aitls_Order"))
                    .findFirst()
                    .orElseThrow();
        } finally {
            systemAuthenticator.end();
        }
    }

    EntityDescriptor getOrderDescriptor() {
        systemAuthenticator.begin();
        try {
            return domainModelDiscoveryTool.getDomainModelForEntities(List.of("aitls_Order"), new ToolContext(Map.of()))
                    .stream()
                    .findFirst()
                    .orElseThrow();
        } finally {
            systemAuthenticator.end();
        }
    }
}
