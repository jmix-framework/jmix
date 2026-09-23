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

import io.jmix.aitools.dataload.introspection.JpaDomainModelIntrospector;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the {@code @ExcludeFromAi} trust boundary: an annotated entity or attribute is dropped from
 * the introspected index unconditionally, the exclusion propagates to entity subclasses, and no
 * configuration can bring it back.
 */
class DomainModelDiscoveryExcludeFromAiTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    class DefaultExclusion {

        @Autowired
        JpaDomainModelIntrospector introspector;
        @Autowired
        DomainModelDiscoveryTool tool;
        @Autowired
        SystemAuthenticator systemAuthenticator;

        @Test
        @DisplayName("Drops a @ExcludeFromAi entity from the index, making it unqueryable")
        void testEntityDroppedFromIndex() {
            assertNull(introspector.getEntityDescriptor("aitls_HiddenEntity"));
            // Left the index, so JPQL validation cannot resolve it either.
            assertFalse(introspector.containsEntity("aitls_HiddenEntity"));
        }

        @Test
        @DisplayName("Propagates the exclusion to a subclass of a @ExcludeFromAi entity")
        void testExclusionPropagatesToSubclass() {
            assertNull(introspector.getEntityDescriptor("aitls_HiddenBaseEntity"));
            assertNull(introspector.getEntityDescriptor("aitls_HiddenSubEntity"));
        }

        @Test
        @DisplayName("Drops a @ExcludeFromAi attribute from the index while the entity stays available")
        void testAttributeDroppedFromIndex() {
            assertNotNull(introspector.getEntityDescriptor("aitls_Customer"));
            assertTrue(introspector.containsProperty("aitls_Customer", "name"));
            assertFalse(introspector.containsProperty("aitls_Customer", "piiPhone"));
            // Left the index, so a generated query naming it is rejected as an unknown path.
            assertFalse(introspector.containsPropertyPath("aitls_Customer", "piiPhone"));
        }

        @Test
        @DisplayName("Hides a @ExcludeFromAi attribute from the detailed descriptor")
        void testAttributeHiddenFromDescriptor() {
            systemAuthenticator.begin();
            try {
                EntityDescriptor customer = tool.getDomainModelForEntities(
                                List.of("aitls_Customer"), new ToolContext(Map.of())).stream()
                        .findFirst()
                        .orElseThrow();
                assertTrue(hasProperty(customer, "name"));
                assertFalse(hasProperty(customer, "piiPhone"));
            } finally {
                systemAuthenticator.end();
            }
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties =
            "jmix.aitools.dataload.include-entities=aitls_HiddenEntity,aitls_HiddenBaseEntity,aitls_HiddenSubEntity")
    class ForceIncludeAttempt {

        @Autowired
        JpaDomainModelIntrospector introspector;

        @Test
        @DisplayName("A force-include cannot bring back a @ExcludeFromAi entity or its subclass")
        void testForceIncludeCannotOverrideAnnotation() {
            assertNull(introspector.getEntityDescriptor("aitls_HiddenEntity"));
            assertNull(introspector.getEntityDescriptor("aitls_HiddenBaseEntity"));
            assertNull(introspector.getEntityDescriptor("aitls_HiddenSubEntity"));
        }
    }

    static boolean hasProperty(EntityDescriptor descriptor, String propertyName) {
        return descriptor.getProperties().stream().anyMatch(property -> property.getName().equals(propertyName));
    }
}
