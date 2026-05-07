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

import io.jmix.core.Metadata;
import io.jmix.texttodata.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.texttodata.introspection.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;
import test_support.entity.sales.Order;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TextToDataTestConfiguration.class)
class JpaDomainModelIntrospectorTest {

    @Autowired
    Metadata metadata;

    @Autowired
    JpaDomainModelIntrospector introspector;

    @Test
    @DisplayName("Introspects entity descriptor and localized entity names")
    void testIntrospectsEntityAndLocalizedNames() {
        EntityDescriptor order = introspector.getEntityDescriptor("textdt_Order");

        assertNotNull(order);
        assertEquals("textdt_Order", order.getName());
        assertTrue(order.getLocalizedNames().contains("Test order"), order.getLocalizedNames().toString());
        assertSame(order, introspector.getEntityDescriptor(metadata.getClass(Order.class)));
    }

    @Test
    @DisplayName("Introspects datatype properties and identifier property")
    void testIntrospectsDatatypePropertiesAndIdProperty() {
        Map<String, EntityPropertyDescriptor> properties = properties("textdt_Order");

        DatatypePropertyDescriptor id = datatype(properties, "id");
        assertEquals("UUID", id.getJavaType());
        assertEquals("datatype", id.getPropertyType());
        assertEquals(Boolean.TRUE, id.getIdentifier());

        DatatypePropertyDescriptor number = datatype(properties, "number");
        assertEquals("String", number.getJavaType());
        assertEquals("datatype", number.getPropertyType());
        assertNull(number.getIdentifier());
        assertEquals("Business order number", number.getComment());
        assertTrue(number.getLocalizedNames().contains("Number"), number.getLocalizedNames().toString());

        assertEquals("BigDecimal", datatype(properties, "amount").getJavaType());
        assertEquals("LocalDate", datatype(properties, "orderDate").getJavaType());
        assertEquals("Boolean", datatype(properties, "active").getJavaType());
        assertEquals("Integer", datatype(properties, "version").getJavaType());
    }

    @Test
    @DisplayName("Introspects Jmix enum and plain Java enum properties")
    void testIntrospectsJmixEnumAndPlainJavaEnumProperties() {
        Map<String, EntityPropertyDescriptor> properties = properties("textdt_Order");

        EnumPropertyDescriptor status = enumProperty(properties, "status");
        assertEquals("Status", status.getJavaType());
        assertEquals("enum", status.getPropertyType());
        assertEquals("Status", status.getEnumType().getName());
        assertEquals("O", status.getEnumType().getConstants().get("OPEN").getId());
        assertTrue(status.getEnumType().getConstants().get("OPEN").getLocalizedName().contains("Open"),
                status.getEnumType().getConstants().get("OPEN").getLocalizedName().toString());
        assertEquals("C", status.getEnumType().getConstants().get("CLOSED").getId());

        EnumPropertyDescriptor plainStatus = enumProperty(properties, "plainStatus");
        assertEquals("TestPlainStatus", plainStatus.getJavaType());
        assertEquals(0, plainStatus.getEnumType().getConstants().get("NEW").getId());
        assertEquals(1, plainStatus.getEnumType().getConstants().get("DONE").getId());
        assertTrue(plainStatus.getEnumType().getConstants().get("NEW").getLocalizedName().isEmpty());
    }

    @Test
    @DisplayName("Introspects embedded property and embedded identifier property")
    void testIntrospectsEmbeddedPropertyAndEmbeddedIdProperty() {
        EmbeddedPropertyDescriptor address = embedded(properties("textdt_Order"), "address");
        assertEquals("textdt_Address", address.getJavaType());
        assertEquals("embedded", address.getPropertyType());
        assertTrue(address.getEmbedded());
        assertNull(address.getIdentifier());

        EmbeddedPropertyDescriptor embeddedId = embedded(properties("textdt_CompositeKeyEntity"), "id");
        assertEquals("textdt_CompositeKey", embeddedId.getJavaType());
        assertEquals("embedded", embeddedId.getPropertyType());
        assertEquals(Boolean.TRUE, embeddedId.getIdentifier());

        DatatypePropertyDescriptor description = datatype(properties("textdt_CompositeKeyEntity"), "description");
        assertEquals("String", description.getJavaType());
        assertNull(description.getIdentifier());
    }

    @Test
    @DisplayName("Introspects entity associations and relation metadata")
    void testIntrospectsAssociations() {
        Map<String, EntityPropertyDescriptor> orderProperties = properties("textdt_Order");
        Map<String, EntityPropertyDescriptor> customerProperties = properties("textdt_Customer");
        Map<String, EntityPropertyDescriptor> tagProperties = properties("textdt_Tag");

        RelationPropertyDescriptor customer = relation(orderProperties, "customer");
        assertEquals("association", customer.getPropertyType());
        assertEquals("textdt_Customer", customer.getJavaType());
        assertEquals("textdt_Customer", customer.getTargetEntityName());
        assertEquals(Boolean.FALSE, customer.getOptionalRelation());
        assertEquals("MANY_TO_ONE", customer.getCardinality());
        assertNull(customer.getMappedBy());

        RelationPropertyDescriptor approval = relation(orderProperties, "approval");
        assertEquals("ONE_TO_ONE", approval.getCardinality());
        assertEquals(Boolean.FALSE, approval.getOptionalRelation());

        RelationPropertyDescriptor shipment = relation(orderProperties, "shipment");
        assertEquals("ONE_TO_ONE", shipment.getCardinality());
        assertNull(shipment.getOptionalRelation());
        assertEquals("order", shipment.getMappedBy());

        RelationPropertyDescriptor orders = relation(customerProperties, "orders");
        assertEquals("ONE_TO_MANY", orders.getCardinality());
        assertEquals("customer", orders.getMappedBy());
        assertNull(orders.getOptionalRelation());

        RelationPropertyDescriptor tags = relation(orderProperties, "tags");
        assertEquals("MANY_TO_MANY", tags.getCardinality());
        assertNull(tags.getMappedBy());

        RelationPropertyDescriptor inverseTags = relation(tagProperties, "orders");
        assertEquals("MANY_TO_MANY", inverseTags.getCardinality());
        assertEquals("tags", inverseTags.getMappedBy());
    }

    @Test
    @DisplayName("Introspects composition relations and their targets")
    void testIntrospectsCompositions() {
        Map<String, EntityPropertyDescriptor> properties = properties("textdt_Order");

        RelationPropertyDescriptor lines = relation(properties, "lines");
        assertEquals("composition", lines.getPropertyType());
        assertEquals("ONE_TO_MANY", lines.getCardinality());
        assertEquals("order", lines.getMappedBy());
        assertEquals("textdt_OrderLine", lines.getTargetEntityName());

        RelationPropertyDescriptor detail = relation(properties, "detail");
        assertEquals("composition", detail.getPropertyType());
        assertEquals("ONE_TO_ONE", detail.getCardinality());
        assertEquals("order", detail.getMappedBy());
        assertEquals("textdt_OrderDetail", detail.getTargetEntityName());
    }

    @Test
    @DisplayName("Collects all metadata classes on initialization")
    void testInitCollectsAllMetadataClasses() {
        assertNotNull(introspector.getEntityDescriptor("textdt_Order"));
        assertNotNull(introspector.getEntityDescriptor("textdt_Customer"));
        assertNotNull(introspector.getEntityDescriptor("textdt_CompositeKeyEntity"));

        long testEntityCount = introspector.getEntityDescriptors().stream()
                .map(EntityDescriptor::getName)
                .filter(name -> name.startsWith("textdt_"))
                .count();
        assertEquals(10, testEntityCount);
    }

    @Test
    @DisplayName("Does not store entity caption fallbacks as localized names")
    void testDoesNotStoreCaptionFallbacksAsLocalizedNames() {
        EntityDescriptor approval = introspector.getEntityDescriptor("textdt_OrderApproval");
        assertNotNull(approval);
        assertTrue(approval.getLocalizedNames().isEmpty());

        DatatypePropertyDescriptor approvedBy = datatype(properties("textdt_OrderApproval"), "approvedBy");
        assertTrue(approvedBy.getLocalizedNames().isEmpty());
    }

    @Test
    @DisplayName("Descriptor toString methods contain all fields")
    void testDescriptorsToStringContainsFields() {
        EntityDescriptor order = introspector.getEntityDescriptor("textdt_Order");
        assertNotNull(order);

        String entityString = order.toString();
        assertTrue(entityString.contains("EntityDescriptor{name='textdt_Order'"));
        assertTrue(entityString.contains("localizedNames=[Test order]"));
        assertTrue(entityString.contains("properties=["));

        String relationString = relation(properties("textdt_Order"), "lines").toString();
        assertTrue(relationString.contains("RelationPropertyDescriptor{name='lines'"));
        assertTrue(relationString.contains("mappedBy='order'"));
        assertTrue(relationString.contains("cardinality='ONE_TO_MANY'"));

        String enumString = enumProperty(properties("textdt_Order"), "status").toString();
        assertTrue(enumString.contains("EnumPropertyDescriptor{name='status'"));
        assertTrue(enumString.contains("enumType=EnumClassDescriptor{name='Status'"));
        assertTrue(enumString.contains("OPEN=EnumValueDescriptor{id=O, name='OPEN', localizedName=[Open]}"));

        String embeddedString = embedded(properties("textdt_Order"), "address").toString();
        assertTrue(embeddedString.contains("EmbeddedPropertyDescriptor{name='address'"));
        assertTrue(embeddedString.contains("embedded=true"));
    }

    protected Map<String, EntityPropertyDescriptor> properties(String entityName) {
        EntityDescriptor descriptor = introspector.getEntityDescriptor(entityName);
        assertNotNull(descriptor, entityName);
        return descriptor.getProperties().stream()
                .collect(Collectors.toMap(EntityPropertyDescriptor::getName, Function.identity()));
    }

    protected DatatypePropertyDescriptor datatype(Map<String, EntityPropertyDescriptor> properties, String name) {
        EntityPropertyDescriptor descriptor = properties.get(name);
        assertNotNull(descriptor, name);
        assertInstanceOf(DatatypePropertyDescriptor.class, descriptor);
        return (DatatypePropertyDescriptor) descriptor;
    }

    protected EmbeddedPropertyDescriptor embedded(Map<String, EntityPropertyDescriptor> properties, String name) {
        EntityPropertyDescriptor descriptor = properties.get(name);
        assertNotNull(descriptor, name);
        assertInstanceOf(EmbeddedPropertyDescriptor.class, descriptor);
        return (EmbeddedPropertyDescriptor) descriptor;
    }

    protected EnumPropertyDescriptor enumProperty(Map<String, EntityPropertyDescriptor> properties, String name) {
        EntityPropertyDescriptor descriptor = properties.get(name);
        assertNotNull(descriptor, name);
        assertInstanceOf(EnumPropertyDescriptor.class, descriptor);
        return (EnumPropertyDescriptor) descriptor;
    }

    protected RelationPropertyDescriptor relation(Map<String, EntityPropertyDescriptor> properties, String name) {
        EntityPropertyDescriptor descriptor = properties.get(name);
        assertNotNull(descriptor, name);
        assertInstanceOf(RelationPropertyDescriptor.class, descriptor);
        return (RelationPropertyDescriptor) descriptor;
    }
}
