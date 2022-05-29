/*
 * Copyright 2022 Haulmont.
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

package entity_set;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.EntitySet;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Owner;
import test_support.app.entity.Pet;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
class EntitySetTest {

    @Autowired
    Metadata metadata;

    @Test
    void test() {
        Pet pet1 = metadata.create(Pet.class);
        Pet pet2 = metadata.create(Pet.class);
        Owner owner = metadata.create(Owner.class);

        EntitySet entitySet = EntitySet.of(Arrays.asList(pet1, pet2, owner));

        assertSame(pet1, entitySet.get(pet1));
        assertSame(pet1, entitySet.get(Pet.class, pet1.getId()));

        assertFalse(entitySet.optional(metadata.create(Pet.class)).isPresent());

        assertEquals(2, entitySet.getAll(Pet.class).size());
        assertEquals(1, entitySet.getAll(Owner.class).size());
    }
}