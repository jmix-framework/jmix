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

package entity_dataload;

import io.jmix.aitools.dataload.generation.EntityDataLoadQueryParameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDataLoadQueryParameterTest {

    @Test
    void testConstructorDefaultsFlagsToFalse() {
        EntityDataLoadQueryParameter parameter = new EntityDataLoadQueryParameter("dateFrom", "java.time.LocalDate");

        assertEquals("dateFrom", parameter.getName());
        assertEquals("java.time.LocalDate", parameter.getJavaType());
        assertFalse(parameter.isMultiValued());
        assertFalse(parameter.isOptional());
    }

    @Test
    void testFlagSettersMutateAndReturnSameInstance() {
        EntityDataLoadQueryParameter parameter = new EntityDataLoadQueryParameter("ids", "java.util.UUID");

        EntityDataLoadQueryParameter returned = parameter.setMultiValued(true).setOptional(true);

        assertSame(parameter, returned);
        assertTrue(parameter.isMultiValued());
        assertTrue(parameter.isOptional());
    }

    @Test
    void testNullNameOrTypeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntityDataLoadQueryParameter(null, "java.lang.String"));
        assertThrows(IllegalArgumentException.class,
                () -> new EntityDataLoadQueryParameter("x", null));
    }
}
