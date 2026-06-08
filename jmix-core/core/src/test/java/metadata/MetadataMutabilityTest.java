/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.SessionImplementation;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import io.jmix.core.metamodel.model.impl.SessionImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;
import test_support.app.entity.sales.Customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MetadataMutabilityTest {

    @Autowired
    Metadata metadata;
    @Autowired
    ExtendedEntities extendedEntities;
    @Autowired
    DatatypeRegistry datatypeRegistry;

    @Test
    void test() {
        // when

        MetaClass metaClass = metadata.getClass(Customer.class);

        MetaClassImpl dynMetaClass = new MetaClassImpl(metadata.getSession(), "CustomerDynamic");
        dynMetaClass.setJavaClass(Customer.class);
        dynMetaClass.setStore(metaClass.getStore());
        dynMetaClass.addAncestor(metaClass);

        MetaProperty dynMetaProperty = new DynamicMetaProperty(dynMetaClass, "address", String.class,
                new DatatypeRange(datatypeRegistry.get(String.class)), MetaProperty.Type.DATATYPE);
        dynMetaClass.registerProperty(dynMetaProperty);

        ((SessionImplementation) metadata.getSession()).registerClass(metaClass.getName(), metaClass.getJavaClass(), dynMetaClass);

        extendedEntities.registerReplacedMetaClass(dynMetaClass);

        // then

        assertSame(dynMetaClass, metadata.getClass(Customer.class));
        assertSame(dynMetaClass, metadata.getClass("core_Customer"));

        MetaProperty metaProperty = dynMetaClass.findProperty("name");
        assertNotNull(metaProperty);
    }

    @Test
    void registerAndUnregisterPropertiesKeepOrderedSnapshots() {
        MetaClass customerMetaClass = metadata.getClass(Customer.class);

        SessionImpl session = new SessionImpl();

        MetaClassImpl baseMetaClass = new MetaClassImpl(session, "test_BaseDynamic");
        baseMetaClass.setJavaClass(Customer.class);
        baseMetaClass.setStore(customerMetaClass.getStore());

        MetaClassImpl childMetaClass = new MetaClassImpl(session, "test_ChildDynamic");
        childMetaClass.setJavaClass(Pet.class);
        childMetaClass.setStore(customerMetaClass.getStore());
        childMetaClass.addAncestor(baseMetaClass);

        createDatatypeProperty(baseMetaClass, "first");
        MetaPropertyImpl second = createDatatypeProperty(baseMetaClass, "second");
        createDatatypeProperty(childMetaClass, "child");

        assertEquals(List.of("first", "second"), propertyNames(baseMetaClass.getOwnProperties()));
        assertEquals(List.of("first", "second", "child"), propertyNames(childMetaClass.getProperties()));

        MetaPropertyImpl override = createDatatypeProperty(childMetaClass, "first");
        assertEquals(List.of("second", "child", "first"), propertyNames(childMetaClass.getProperties()));

        baseMetaClass.unregisterProperty(second);
        assertEquals(List.of("first"), propertyNames(baseMetaClass.getOwnProperties()));
        assertEquals(List.of("child", "first"), propertyNames(childMetaClass.getProperties()));

        childMetaClass.unregisterProperty(override);
        assertEquals(List.of("first", "child"), propertyNames(childMetaClass.getProperties()));

        createDatatypeProperty(baseMetaClass, "third");
        assertEquals(List.of("first", "third", "child"), propertyNames(childMetaClass.getProperties()));
    }

    private MetaPropertyImpl createDatatypeProperty(MetaClassImpl metaClass, String name) {
        MetaPropertyImpl property = new MetaPropertyImpl(metaClass, name);
        property.setStore(metaClass.getStore());
        property.setRange(new DatatypeRange(datatypeRegistry.get(String.class)));
        property.setType(MetaProperty.Type.DATATYPE);
        property.setJavaType(String.class);
        return property;
    }

    private List<String> propertyNames(Collection<MetaProperty> properties) {
        List<String> propertyNames = new ArrayList<>(properties.size());
        for (MetaProperty property : properties) {
            propertyNames.add(property.getName());
        }
        return propertyNames;
    }
}
