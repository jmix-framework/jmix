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

package ai_exclusion;

import io.jmix.aitools.AiExclusionSupport;
import io.jmix.core.Metadata;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.impl.keyvalue.KeyValueMetaClassFactory;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiExclusionSupportTest {

    static MetaPropertyPath path(Metadata metadata, String entityName, String propertyPath) {
        MetaPropertyPath path = metadata.getClass(entityName).getPropertyPath(propertyPath);
        assertNotNull(path);
        return path;
    }

    static MetaProperty property(Metadata metadata, String entityName, String propertyName) {
        return metadata.getClass(entityName).getProperty(propertyName);
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    class DefaultConfiguration {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;
        @Autowired
        KeyValueMetaClassFactory keyValueFactory;

        @Test
        void isExcluded_annotatedEntity_true() {
            assertTrue(support.isExcluded(metadata.getClass("aitls_HiddenEntity")));
        }

        @Test
        void isExcluded_subclassOfAnnotatedEntity_true() {
            assertTrue(support.isExcluded(metadata.getClass("aitls_HiddenSubEntity")));
        }

        @Test
        void isExcluded_regularEntity_false() {
            assertFalse(support.isExcluded(metadata.getClass("aitls_Order")));
        }

        @Test
        void isExcluded_systemLevelEntity_true() {
            assertTrue(support.isExcluded(metadata.getClass("aitls_SystemLevelEntity")));
        }

        @Test
        void isExcluded_dtoEntity_false() {
            MetaClass dto = metadata.getClass("aitls_DtoEntity");

            assertFalse(support.isExcluded(dto));
            assertFalse(support.isExplicitlyIncluded(dto));
        }

        @Test
        void isHidden_secretAttribute_true() {
            assertTrue(support.isHidden(property(metadata, "aitls_Customer", "secretToken")));
        }

        @Test
        void isHidden_excludeFromAiAttribute_true() {
            assertTrue(support.isHidden(property(metadata, "aitls_Customer", "piiPhone")));
        }

        @Test
        void isHidden_systemLevelAttribute_false() {
            assertFalse(support.isHidden(property(metadata, "aitls_Customer", "systemNote")));
        }

        @Test
        void isHiddenSystemLevel_systemLevelAttributeWithFlagOn_true() {
            assertTrue(support.isHiddenSystemLevel(property(metadata, "aitls_Customer", "systemNote")));
            assertFalse(support.isHiddenSystemLevel(property(metadata, "aitls_Customer", "name")));
        }

        @Test
        void isExcluded_pathToPlainAttribute_false() {
            assertFalse(support.isExcluded(path(metadata, "aitls_Order", "number")));
            assertFalse(support.isExcluded(path(metadata, "aitls_Order", "customer.name")));
        }

        @Test
        void isExcluded_pathThroughReferenceToExcludedEntity_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Document", "licenseKey")));
            assertTrue(support.isExcluded(path(metadata, "aitls_Document", "licenseKey.name")));
        }

        @Test
        void isExcluded_pathThroughReferenceToSystemLevelEntity_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Document", "systemRecord")));
        }

        @Test
        void isExcluded_pathToSecretOrExcludedAttribute_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Customer", "secretToken")));
            assertTrue(support.isExcluded(path(metadata, "aitls_Order", "customer.secretToken")));
            assertTrue(support.isExcluded(path(metadata, "aitls_Order", "customer.piiPhone")));
        }

        @Test
        void isExcluded_pathToSystemLevelAttributeWithFlagOn_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Customer", "systemNote")));
            assertTrue(support.isExcluded(path(metadata, "aitls_Order", "customer.systemNote")));
        }

        @Test
        void isExcluded_pathOfExcludedRootEntity_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_HiddenEntity", "name")));
        }

        @Test
        void isExcluded_keyValuePathToPlainAttribute_false() {
            KeyValueMetaClass keyValue = keyValueFactory.builder()
                    .addProperty("title", String.class)
                    .addProperty("customer", metadata.getClass("aitls_Customer"))
                    .build();

            assertFalse(support.isExcluded(keyValue.getPropertyPath("title")));
            assertFalse(support.isExcluded(keyValue.getPropertyPath("customer.name")));
        }

        @Test
        void isExcluded_keyValuePathThroughReferenceToExcludedEntity_true() {
            KeyValueMetaClass keyValue = keyValueFactory.builder()
                    .addProperty("title", String.class)
                    .addProperty("hidden", metadata.getClass("aitls_HiddenEntity"))
                    .build();

            assertTrue(support.isExcluded(keyValue.getPropertyPath("hidden")));
            assertTrue(support.isExcluded(keyValue.getPropertyPath("hidden.name")));
        }

        @Test
        void isExcluded_keyValuePathThroughReferenceToSecretAttribute_true() {
            KeyValueMetaClass keyValue = keyValueFactory.builder()
                    .addProperty("customer", metadata.getClass("aitls_Customer"))
                    .build();

            assertTrue(support.isExcluded(keyValue.getPropertyPath("customer.secretToken")));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = "jmix.aitools.dataload.exclude-system-level-attributes=false")
    class SystemLevelAttributesAllowed {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isHiddenSystemLevel_systemLevelAttributeWithFlagOff_false() {
            assertFalse(support.isHiddenSystemLevel(property(metadata, "aitls_Customer", "systemNote")));
        }

        @Test
        void isExcluded_pathToSystemLevelAttributeWithFlagOff_false() {
            assertFalse(support.isExcluded(path(metadata, "aitls_Customer", "systemNote")));
        }

        @Test
        void isExcluded_pathToSecretSystemLevelAttributeWithFlagOff_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Customer", "secretSystemNote")));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = {
            "jmix.aitools.dataload.include-entities=aitls_SystemLevelEntity,aitls_Order",
            "jmix.aitools.dataload.exclude-entities=aitls_Order",
            "jmix.aitools.dataload.include-packages=test_support.entity.sales"
    })
    class ExplicitIncludeAndExclude {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isExcluded_includedSystemLevelEntity_false() {
            MetaClass systemLevel = metadata.getClass("aitls_SystemLevelEntity");

            assertTrue(support.isExplicitlyIncluded(systemLevel));
            assertFalse(support.isExcluded(systemLevel));
        }

        @Test
        void isExcluded_entityInBothExcludeAndIncludeLists_true() {
            MetaClass order = metadata.getClass("aitls_Order");

            assertTrue(support.isExplicitlyIncluded(order));
            assertTrue(support.isExcluded(order));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = "jmix.aitools.dataload.include-entities=aitls_HiddenEntity,aitls_HiddenSubEntity")
    class AnnotationBeatsInclude {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isExcluded_annotatedEntityListedInIncludeEntities_true() {
            assertTrue(support.isExcluded(metadata.getClass("aitls_HiddenEntity")));
            assertTrue(support.isExcluded(metadata.getClass("aitls_HiddenSubEntity")));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = "jmix.aitools.dataload.exclude-packages=test_support.entity.sales")
    class ExcludedPackage {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isExcluded_entityInExcludedPackage_true() {
            assertTrue(support.isExcluded(metadata.getClass("aitls_Order")));
            assertFalse(support.isExcluded(metadata.getClass("aitls_CompositeKeyEntity")));
        }

        @Test
        void isExcluded_pathOfEntityInExcludedPackage_true() {
            assertTrue(support.isExcluded(path(metadata, "aitls_Order", "number")));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = {
            "jmix.aitools.dataload.exclude-packages=test_support.entity.sales",
            "jmix.aitools.dataload.include-packages=test_support.entity.sales"
    })
    class IncludedPackageOverridesExcludedPackage {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isExcluded_entityInIncludedAndExcludedPackage_false() {
            MetaClass order = metadata.getClass("aitls_Order");

            assertTrue(support.isExplicitlyIncluded(order));
            assertFalse(support.isExcluded(order));
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AiToolsTestConfiguration.class)
    @TestPropertySource(properties = {
            "jmix.aitools.dataload.exclude-packages=test_support.entity.sales",
            "jmix.aitools.dataload.include-entities=aitls_Order"
    })
    class IncludedEntityOverridesExcludedPackage {

        @Autowired
        AiExclusionSupport support;
        @Autowired
        Metadata metadata;

        @Test
        void isExcluded_includedEntityInExcludedPackage_false() {
            MetaClass order = metadata.getClass("aitls_Order");

            assertTrue(support.isExplicitlyIncluded(order));
            assertFalse(support.isExcluded(order));
        }
    }
}
