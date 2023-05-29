/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.EntityLoadInfoBuilder;
import com.haulmont.cuba.core.testsupport.CoreTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class EntityLoadInfoBuilderTest {

    private EntityLoadInfoBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = AppBeans.get(EntityLoadInfoBuilder.class);
    }

    @Test
    public void testParseStringIdEntity() {
        EntityLoadInfo info = builder.parse("test$StringKeyEntity-{abc}");
        assertNotNull(info);
        assertEquals("test$StringKeyEntity", info.getMetaClass().getName());
        assertEquals("abc", info.getId());
        assertNull(info.getViewName());
        assertFalse(info.isNewEntity());

        info = builder.parse("test$StringKeyEntity-{abc}-my-view");
        assertNotNull(info);
        assertEquals("test$StringKeyEntity", info.getMetaClass().getName());
        assertEquals("abc", info.getId());
        assertEquals("my-view", info.getViewName());
        assertFalse(info.isNewEntity());
    }
}