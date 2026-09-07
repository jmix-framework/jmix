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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the domain-model discovery tools never expose non-persistent attributes of
 * {@code aitls_Order} (declares a {@code transientNote} {@code @Transient} {@code @JmixProperty}),
 * since such attributes cannot be used in the JPQL these tools prepare for.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class DomainModelDiscoveryNonPersistentAttributesTest {

    @Autowired
    DomainModelDiscoveryTool tool;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    @DisplayName("Hides a non-persistent attribute from the compact summary")
    void testHidesNonPersistentAttributeFromSummary() {
        systemAuthenticator.begin();
        try {
            EntitySummary order = orderSummary();
            assertTrue(order.getPropertyNames().contains("number"));
            assertFalse(order.getPropertyNames().contains("transientNote"));
        } finally {
            systemAuthenticator.end();
        }
    }

    @Test
    @DisplayName("Hides a non-persistent attribute from the detailed descriptor")
    void testHidesNonPersistentAttributeFromDescriptor() {
        systemAuthenticator.begin();
        try {
            EntityDescriptor order = orderDescriptor();
            assertTrue(hasProperty(order, "number"));
            assertFalse(hasProperty(order, "transientNote"));
        } finally {
            systemAuthenticator.end();
        }
    }

    EntitySummary orderSummary() {
        return tool.getAvailableEntities(new ToolContext(Map.of())).stream()
                .filter(summary -> summary.getEntityName().equals("aitls_Order"))
                .findFirst()
                .orElseThrow();
    }

    EntityDescriptor orderDescriptor() {
        return tool.getDomainModelForEntities(List.of("aitls_Order"), new ToolContext(Map.of())).stream()
                .findFirst()
                .orElseThrow();
    }

    static boolean hasProperty(EntityDescriptor descriptor, String propertyName) {
        return descriptor.getProperties().stream().anyMatch(property -> property.getName().equals(propertyName));
    }
}
