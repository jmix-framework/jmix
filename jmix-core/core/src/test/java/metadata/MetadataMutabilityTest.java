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
import io.jmix.core.repository.JmixDataRepositoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;
import test_support.app.entity.sales.Customer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
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
}
