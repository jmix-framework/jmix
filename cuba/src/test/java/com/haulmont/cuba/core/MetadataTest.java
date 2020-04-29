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
 *
 */
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.model.not_persistent.TestNotPersistentEntity;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoreTest
public class MetadataTest {

    @Inject
    private Metadata metadata;
    @Inject
    private MetadataTools metadataTools;

    @Test
    public void testPersistentAndTransientEntities() throws Exception {
        assertTrue(metadataTools.isPersistent(User.class));
        assertFalse(metadataTools.isPersistent(LockInfo.class));
        assertFalse(metadataTools.isPersistent(TestNotPersistentEntity.class));
    }

    @Test
    public void testPersistentAndTransientProperties() {

        // User
        MetaClass metaClass = metadata.getSession().getClass(User.class);
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("id")));
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("id")));
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("createTs")));
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("login")));
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("group")));
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("userRoles")));

        // EntityLogItem
        metaClass = metadata.getSession().getClass(EntityLogItem.class);
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("user")));
        assertFalse(metadataTools.isPersistent(metaClass.getProperty("attributes")));
        assertFalse(metadataTools.isPersistent(metaClass.getProperty("attributes")));

        // Folder
        metaClass = metadata.getSession().getClass(Folder.class);
        assertTrue(metadataTools.isPersistent(metaClass.getProperty("name")));

        // UserSessionEntity
        metaClass = metadata.getSession().getClass(UserSessionEntity.class);
        assertFalse(metadataTools.isPersistent(metaClass.getProperty("id")));

        // TestTransientEntity
        metaClass = metadata.getSession().getClass(TestNotPersistentEntity.class);
        assertFalse(metadataTools.isPersistent(metaClass.getProperty("id")));
    }

    @Test
    @Disabled
    public void testEmbeddedProperty() {
        // TestTransientEntity
        MetaClass metaClass = metadata.getSession().getClass(TestNotPersistentEntity.class);
        assertFalse(metadataTools.isPersistent(metaClass.getProperty("embeddedRef")));
        assertFalse(metadataTools.isEmbedded(metaClass.getProperty("embeddedRef")));
    }

    @Test
    public void testSystemLevel() {
        assertTrue(metadataTools.isSystemLevel(metadata.getClass(UserRole.class)));

        assertFalse(metadataTools.isSystemLevel(metadata.getClass(TestNotPersistentEntity.class)));

        MetaClass metaClass = metadata.getClass(User.class);
        assertTrue(metadataTools.isSystemLevel(metaClass.getProperty("password")));

        assertTrue(metadataTools.isSystemLevel(metadata.getClass(SearchFolder.class)));
    }
}
