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

package data_manager;

import com.google.common.collect.Sets;
import io.jmix.core.*;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class SaveContextTest {

    @Autowired
    FetchPlans fetchPlans;

    @Test
    public void test() {
        Pet pet = new Pet();
        Owner owner = new Owner();
        FetchPlan petFetchPlan = fetchPlans.builder(Pet.class).add("name").build();
        FetchPlan ownerFetchPlan = fetchPlans.builder(Owner.class).add("name").build();
        SaveContext ctx;

        // when:
        ctx = new SaveContext().saving(pet, owner);
        // then:
        assertEquals(Sets.newHashSet(pet, owner), ctx.getEntitiesToSave());
        assertTrue(ctx.getEntitiesToRemove().isEmpty());

        // when:
        List<Pet> pets = Arrays.asList(pet);
        ctx = new SaveContext().saving(pets);
        // then:
        assertEquals(Sets.newHashSet(pet), ctx.getEntitiesToSave());
        assertTrue(ctx.getEntitiesToRemove().isEmpty());

        // when:
        List<Object> entitiesToSave = Arrays.asList(pet, owner);
        ctx = new SaveContext().saving(entitiesToSave);
        // then:
        assertEquals(Sets.newHashSet(pet, owner), ctx.getEntitiesToSave());
        assertTrue(ctx.getEntitiesToRemove().isEmpty());

        // when:
        List<Pet> ownersToRemove = Arrays.asList(pet);
        ctx = new SaveContext().removing(ownersToRemove);
        // then:
        assertTrue(ctx.getEntitiesToSave().isEmpty());
        assertEquals(Sets.newHashSet(pet), ctx.getEntitiesToRemove());

        // when:
        ctx = new SaveContext().saving(pet, petFetchPlan).saving(owner, ownerFetchPlan);
        // then:
        assertEquals(Sets.newHashSet(pet, owner), ctx.getEntitiesToSave());
        assertSame(petFetchPlan, ctx.getFetchPlans().get(pet));
        assertSame(ownerFetchPlan, ctx.getFetchPlans().get(owner));

        // when:
        ctx = new SaveContext().saving(pet, null);
        // then:
        assertEquals(Sets.newHashSet(pet), ctx.getEntitiesToSave());
        assertNull(ctx.getFetchPlans().get(pet));

        // when:
        ctx = new SaveContext().saving(pet).removing(owner)
                .setHint("jmix.softDeletion", false)
                .setDiscardSaved(true)
                .setJoinTransaction(true)
                .setHint("h1", "v1");
        // then:
        assertFalse((Boolean) ctx.getHints().get("jmix.softDeletion"));
        assertTrue(ctx.isDiscardSaved());
        assertTrue(ctx.isJoinTransaction());
        assertEquals("v1", ctx.getHints().get("h1"));
    }

    @Test
    void testCollectionsAreModifiable() {
        Pet pet1 = new Pet();
        Pet pet2 = new Pet();
        SaveContext ctx = new SaveContext();

        // when:
        ctx.getEntitiesToSave().add(pet1);
        ctx.getEntitiesToRemove().add(pet2);

        // then:
        assertTrue(ctx.getEntitiesToSave().contains(pet1));
        assertTrue(ctx.getEntitiesToRemove().contains(pet2));
    }
}
