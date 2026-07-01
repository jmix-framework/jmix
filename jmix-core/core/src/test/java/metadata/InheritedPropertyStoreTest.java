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
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.metadata.MetadataSessionCloneSupport;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.sales.Customer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class InheritedPropertyStoreTest {

    @Autowired
    Metadata metadata;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    ExtendedEntities extendedEntities;
    @Autowired
    MetadataSessionCloneSupport metadataSessionCloneSupport;
    @Autowired
    DatatypeRegistry datatypeRegistry;

    @Test
    void liveSessionKeepsJpaFlagAtAnyDepth() {
        MetaClass customer = metadata.getClass(Customer.class);

        // createdBy is declared in BaseEntity (one level above Customer)
        assertTrue(metadataTools.isJpa(customer.getProperty("createdBy")),
                "createdBy from first-level mapped superclass must be JPA");

        // id is declared in BaseUuidEntity (two levels above Customer)
        assertTrue(metadataTools.isJpa(customer.getProperty("id")),
                "id from second-level mapped superclass must be JPA");
    }

    @Test
    void clonedSessionKeepsJpaFlagAtAnyDepth() {
        MetadataSessionCloneSupport.SessionCloneResult cloneResult = metadataSessionCloneSupport.cloneSession(
                metadata.getSession(),
                extendedEntities.getCurrentStateSnapshot()
        );

        MetaClass customer = cloneResult.getSession().getClass(Customer.class);

        assertTrue(metadataTools.isJpa(customer.getProperty("createdBy")),
                "createdBy must keep JPA flag after session cloning");
        assertTrue(metadataTools.isJpa(customer.getProperty("id")),
                "id from second-level mapped superclass must keep JPA flag after session cloning");
    }

    @Test
    void rebuildAfterLoadKeepsJpaFlagAtAnyDepth() {
        MetaClass customer = metadata.getClass(Customer.class);
        Store mainStore = customer.getStore();

        // Register a new own property on Customer. This triggers a property-snapshot rebuild
        // of the meta class (the same thing dynamic model apply does), which re-resolves
        // inherited properties from their mapped-superclass ancestors.
        MetaPropertyImpl extra = new MetaPropertyImpl((MetaClassImpl) customer, "extraDynamicAttr");
        extra.setStore(mainStore);
        extra.setRange(new DatatypeRange(datatypeRegistry.get(String.class)));
        extra.setType(MetaProperty.Type.DATATYPE);
        extra.setJavaType(String.class);

        try {
            assertTrue(metadataTools.isJpa(customer.getProperty("createdBy")),
                    "createdBy must keep JPA flag after property-snapshot rebuild");
            assertTrue(metadataTools.isJpa(customer.getProperty("id")),
                    "id from second-level mapped superclass must keep JPA flag after property-snapshot rebuild");
        } finally {
            ((MetaClassImpl) customer).unregisterProperty(extra);
        }
    }
}
