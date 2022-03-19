/*
 * Copyright 2021 Haulmont.
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

package entity_str_ref;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;
import test_support.app.entity.composite_id.CompositeKey;
import test_support.app.entity.composite_id.CompositeKeyEntity;
import test_support.app.entity.nullable_id.NFoo;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class IdSerializationTest {

    @Autowired
    IdSerialization idSerialization;

    @Test
    void testUuid() {
        Pet entity = new Pet();
        UUID uuid = UUID.fromString("4e4c5ca2-9a6e-43aa-8e67-3572b674f7c0");
        entity.setId(uuid);

        String strRef = idSerialization.idToString(Id.of(entity));
        assertEquals("app_Pet.\"4e4c5ca2-9a6e-43aa-8e67-3572b674f7c0\"", strRef);

        Id<Pet> entityId = idSerialization.stringToId(strRef);
        assertEquals(Pet.class, entityId.getEntityClass());
        assertEquals(uuid, entityId.getValue());
    }

    @Test
    void testLongId() {
        NFoo entity = new NFoo();
        entity.setId(10L);

        String strRef = idSerialization.idToString(Id.of(entity));
        assertEquals("test_NFoo.10", strRef);

        Id<NFoo> entityId = idSerialization.stringToId(strRef);
        assertEquals(NFoo.class, entityId.getEntityClass());
        assertEquals(10L, entityId.getValue());
    }

    @Test
    void testCompositeId() {
        CompositeKey key = new CompositeKey();
        key.setTenant("abc");
        key.setEntityId(10L);

        CompositeKeyEntity entity = new CompositeKeyEntity();
        entity.setId(key);

        String strRef = idSerialization.idToString(Id.of(entity));
        assertEquals("app_CompositeKeyEntity.{\"entityId\":10,\"tenant\":\"abc\"}", strRef);

        Id<CompositeKeyEntity> entityId = idSerialization.stringToId(strRef);
        assertEquals(CompositeKeyEntity.class, entityId.getEntityClass());
        assertEquals("abc", ((CompositeKey) entityId.getValue()).getTenant());
        assertEquals(10L, ((CompositeKey) entityId.getValue()).getEntityId());
    }
}
