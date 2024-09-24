/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class})
public class DtoMetadataTest {

    @Autowired
    Metadata metadata;

    @Test
    void testCompositionInverseProperty() {
        MetaClass contactMetaClass = metadata.getClass(CustomerContact.class);
        MetaProperty customerProp = contactMetaClass.getProperty("customer");

        MetaClass customerMetaClass = metadata.getClass(Customer.class);
        MetaProperty contactsProp = customerMetaClass.getProperty("contacts");

        MetaProperty inverseProp = contactsProp.getInverse();

        assertThat(inverseProp).isEqualTo(customerProp);
    }
}
