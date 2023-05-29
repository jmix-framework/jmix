/*
 * Copyright 2019 Haulmont.
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

package cuba_metadata;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.metamodel.model.MetaClass;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class MetadataTest {

    @Autowired
    Metadata metadata;

    @Test
    void test() {
        MetaClass metaClass;

        metaClass = metadata.getClass("test_PetclinicPet");
        assertNotNull(metaClass);

        metaClass = metadata.getClassNN("test_PetclinicPet");
        assertNotNull(metaClass);

        metaClass = metadata.getClass("non_existent");
        assertNull(metaClass);

        try {
            metadata.getClassNN("non_existent");
            fail();
        } catch (Exception e) {
            // ok
        }
    }
}
