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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies how the domain-model discovery tools expose {@code @Secret} and {@code @SystemLevel}
 * attributes of {@code aitls_Customer} (declares a {@code secretToken} and a {@code systemNote}).
 */
class DomainModelDiscoverySensitiveAttributesTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    class DefaultHiding {

        @Autowired
        DomainModelDiscoveryTool tool;
        @Autowired
        SystemAuthenticator systemAuthenticator;

        @Test
        @DisplayName("Hides @Secret and @SystemLevel attributes from the compact summary by default")
        void testHidesSensitiveAttributesFromSummary() {
            systemAuthenticator.begin();
            try {
                EntitySummary customer = customerSummary(tool);
                assertTrue(customer.getPropertyNames().contains("name"));
                assertFalse(customer.getPropertyNames().contains("secretToken"));
                assertFalse(customer.getPropertyNames().contains("systemNote"));
            } finally {
                systemAuthenticator.end();
            }
        }

        @Test
        @DisplayName("Hides @Secret and @SystemLevel attributes from the detailed descriptor by default")
        void testHidesSensitiveAttributesFromDescriptor() {
            systemAuthenticator.begin();
            try {
                EntityDescriptor customer = customerDescriptor(tool);
                assertTrue(hasProperty(customer, "name"));
                assertFalse(hasProperty(customer, "secretToken"));
                assertFalse(hasProperty(customer, "systemNote"));
            } finally {
                systemAuthenticator.end();
            }
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = "jmix.aitools.dataload.exclude-system-level-attributes=false")
    class SystemLevelHidingDisabled {

        @Autowired
        DomainModelDiscoveryTool tool;
        @Autowired
        SystemAuthenticator systemAuthenticator;

        @Test
        @DisplayName("Exposes @SystemLevel attribute when hiding is disabled, but never @Secret")
        void testExposesSystemLevelButNeverSecret() {
            systemAuthenticator.begin();
            try {
                EntityDescriptor customer = customerDescriptor(tool);
                assertTrue(hasProperty(customer, "systemNote"));
                // @Secret is unconditional: the flag does not bring it back.
                assertFalse(hasProperty(customer, "secretToken"));
            } finally {
                systemAuthenticator.end();
            }
        }
    }

    static EntitySummary customerSummary(DomainModelDiscoveryTool tool) {
        return tool.getAvailableEntities(new ToolContext(Map.of())).stream()
                .filter(summary -> summary.getEntityName().equals("aitls_Customer"))
                .findFirst()
                .orElseThrow();
    }

    static EntityDescriptor customerDescriptor(DomainModelDiscoveryTool tool) {
        return tool.getDomainModelForEntities(List.of("aitls_Customer"), new ToolContext(Map.of())).stream()
                .findFirst()
                .orElseThrow();
    }

    static boolean hasProperty(EntityDescriptor descriptor, String propertyName) {
        return descriptor.getProperties().stream().anyMatch(property -> property.getName().equals(propertyName));
    }
}
