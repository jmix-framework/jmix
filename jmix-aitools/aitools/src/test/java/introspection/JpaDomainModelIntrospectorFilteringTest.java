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

import io.jmix.aitools.introspection.introspector.JpaDomainModelIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JpaDomainModelIntrospectorFilteringTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    class DefaultFiltering {

        @Autowired
        JpaDomainModelIntrospector introspector;

        @Test
        @DisplayName("Excludes system-level, DTO, and 'io.jmix' package entities by default")
        void testDefaultFiltering() {
            assertNull(introspector.getEntityDescriptor("aitols_SystemLevelEntity"));
            assertNull(introspector.getEntityDescriptor("aitols_DtoEntity"));
            assertNull(introspector.getEntityDescriptor("aitols_PackageEntity"));
            assertNotNull(introspector.getEntityDescriptor("aitols_Order"));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = {
            "aitools.include-entities=aitols_SystemLevelEntity,aitols_Order",
            "aitools.exclude-entities=aitols_Order"
    })
    class ExplicitFiltering {

        @Autowired
        JpaDomainModelIntrospector introspector;

        @Test
        @DisplayName("Allows explicit includes and applies explicit excludes last")
        void testExplicitFiltering() {
            assertNotNull(introspector.getEntityDescriptor("aitols_SystemLevelEntity"));
            assertNull(introspector.getEntityDescriptor("aitols_Order"));
        }
    }
}
