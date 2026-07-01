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

package metadata;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.impl.metadata.MetadataSessionCloneSupport;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.instance_name_inheritance.ExtEntity;
import test_support.app.entity.jmix_entities.AnnotatedNonJpaEntity;
import test_support.app.entity.jmix_entities.EntityWithJmix;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class MetadataPropertyOrderTest {

    @Autowired
    Metadata metadata;
    @Autowired
    ExtendedEntities extendedEntities;
    @Autowired
    MetadataSessionCloneSupport metadataSessionCloneSupport;

    @Test
    void ownPropertiesFollowFieldDeclarationOrder() {
        MetaClass metaClass = metadata.getClass(EntityWithJmix.class);

        assertEquals(List.of(
                        "uuid",
                        "name",
                        "mappedWithJmix",
                        "embeddableWithJmix",
                        "calculatedId",
                        "consideredTransientField"
                ),
                propertyNames(metaClass.getOwnProperties()));
    }

    @Test
    void methodBasedPropertiesAreAppendedAfterFieldBasedProperties() {
        MetaClass metaClass = metadata.getClass(AnnotatedNonJpaEntity.class);

        List<String> propertyNames = propertyNames(metaClass.getOwnProperties());
        assertEquals("id", propertyNames.get(0));
        assertEquals("allowedProperty", propertyNames.get(1));
        assertTrue(propertyNames.indexOf("methodAnnotatedProperty") == 2 || propertyNames.indexOf("methodAnnotatedProperty") == 3);
        assertTrue(propertyNames.indexOf("methodOnlyProperty") == 2 || propertyNames.indexOf("methodOnlyProperty") == 3);
    }

    @Test
    void inheritedPropertiesFollowBaseToLeafOrder() {
        MetaClass metaClass = metadata.getClass(ExtEntity.class);

        assertEquals(List.of(
                        "id",
                        "code",
                        "name",
                        "description",
                        "version",
                        "createdBy",
                        "createdDate",
                        "lastModifiedBy",
                        "lastModifiedDate",
                        "deletedBy",
                        "deletedDate",
                        "fullCode"
                ),
                propertyNames(metaClass.getProperties()));
    }

    @Test
    void clonedSessionPreservesPropertyOrder() {
        MetadataSessionCloneSupport.SessionCloneResult cloneResult = metadataSessionCloneSupport.cloneSession(
                metadata.getSession(),
                extendedEntities.getCurrentStateSnapshot()
        );

        MetaClass metaClass = cloneResult.getSession().getClass(AnnotatedNonJpaEntity.class);

        List<String> propertyNames = propertyNames(metaClass.getOwnProperties());
        assertEquals("id", propertyNames.get(0));
        assertEquals("allowedProperty", propertyNames.get(1));
        assertTrue(propertyNames.indexOf("methodAnnotatedProperty") == 2 || propertyNames.indexOf("methodAnnotatedProperty") == 3);
        assertTrue(propertyNames.indexOf("methodOnlyProperty") == 2 || propertyNames.indexOf("methodOnlyProperty") == 3);

        propertyNames = propertyNames(metaClass.getOwnProperties());
        assertEquals("id", propertyNames.get(0));
        assertEquals("allowedProperty", propertyNames.get(1));
        assertTrue(propertyNames.indexOf("methodAnnotatedProperty") == 2 || propertyNames.indexOf("methodAnnotatedProperty") == 3);
        assertTrue(propertyNames.indexOf("methodOnlyProperty") == 2 || propertyNames.indexOf("methodOnlyProperty") == 3);
    }

    private List<String> propertyNames(Iterable<MetaProperty> properties) {
        return toList(properties).stream()
                .map(MetaProperty::getName)
                .toList();
    }

    private List<MetaProperty> toList(Iterable<MetaProperty> properties) {
        if (properties instanceof List<MetaProperty> propertyList) {
            return propertyList;
        }

        List<MetaProperty> propertyList = new ArrayList<>();
        for (MetaProperty property : properties) {
            propertyList.add(property);
        }
        return propertyList;
    }
}
