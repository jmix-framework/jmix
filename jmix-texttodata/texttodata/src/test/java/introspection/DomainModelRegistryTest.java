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

import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TextToDataTestConfiguration.class)
class DomainModelRegistryTest {

    @Autowired
    DomainModelRegistry domainModelRegistry;

    @Test
    @DisplayName("Indexes entity descriptors by name")
    void testIndexesEntitiesByName() {
        assertTrue(domainModelRegistry.containsEntity("textdt_Order"));
        assertTrue(domainModelRegistry.containsEntity("textdt_Customer"));
        assertFalse(domainModelRegistry.containsEntity("textdt_Unknown"));
        assertNotNull(domainModelRegistry.getEntityDescriptor("textdt_Order"));
    }

    @Test
    @DisplayName("Indexes property descriptors by entity and property name")
    void testIndexesPropertiesByEntityAndName() {
        assertTrue(domainModelRegistry.containsProperty("textdt_Order", "number"));
        assertTrue(domainModelRegistry.containsProperty("textdt_Order", "customer"));
        assertFalse(domainModelRegistry.containsProperty("textdt_Order", "unknown"));
        assertFalse(domainModelRegistry.containsProperty("textdt_Unknown", "number"));

        EntityPropertyDescriptor propertyDescriptor = domainModelRegistry.getPropertyDescriptor("textdt_Order", "number");
        assertNotNull(propertyDescriptor);
        assertEquals("number", propertyDescriptor.getName());
    }

    @Test
    @DisplayName("Returns relation property descriptors")
    void testReturnsRelationPropertyDescriptors() {
        RelationPropertyDescriptor customer = domainModelRegistry.getRelationPropertyDescriptor("textdt_Order", "customer");
        assertNotNull(customer);
        assertEquals("textdt_Customer", customer.getTargetEntityName());

        assertNull(domainModelRegistry.getRelationPropertyDescriptor("textdt_Order", "number"));
    }

    @Test
    @DisplayName("Resolves valid property paths through relations")
    void testResolvesValidPropertyPaths() {
        List<EntityPropertyDescriptor> path = domainModelRegistry.resolvePropertyPath("textdt_Order", "customer.name");
        assertNotNull(path);
        assertEquals(2, path.size());
        assertEquals("customer", path.get(0).getName());
        assertEquals("name", path.get(1).getName());

        List<EntityPropertyDescriptor> nestedPath = domainModelRegistry.resolvePropertyPath("textdt_Tag", "customers.orders.number");
        assertNotNull(nestedPath);
        assertEquals(3, nestedPath.size());
        assertEquals("customers", nestedPath.get(0).getName());
        assertEquals("orders", nestedPath.get(1).getName());
        assertEquals("number", nestedPath.get(2).getName());
    }

    @Test
    @DisplayName("Rejects invalid property paths")
    void testRejectsInvalidPropertyPaths() {
        assertNull(domainModelRegistry.resolvePropertyPath("textdt_Order", "number.value"));
        assertNull(domainModelRegistry.resolvePropertyPath("textdt_Order", "customer.unknown"));
        assertNull(domainModelRegistry.resolvePropertyPath("textdt_Unknown", "customer.name"));
        assertNull(domainModelRegistry.resolvePropertyPath("textdt_Order", ""));

        assertFalse(domainModelRegistry.containsPropertyPath("textdt_Order", "number.value"));
        assertTrue(domainModelRegistry.containsPropertyPath("textdt_Order", "customer.name"));
    }
}
