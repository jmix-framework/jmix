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
        assertFalse(metadataTools.isTransient(User.class));

        assertFalse(metadataTools.isPersistent(LockInfo.class));
        assertTrue(metadataTools.isTransient(LockInfo.class));

        assertFalse(metadataTools.isPersistent(TestNotPersistentEntity.class));
        assertTrue(metadataTools.isNotPersistent(TestNotPersistentEntity.class));
    }

    @Test
    public void testPersistentAndTransientProperties() {

        // User
        MetaClass metaClass = metadata.getSession().getClassNN(User.class);
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("createTs")));
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("login")));
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("group")));
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("userRoles")));

        // EntityLogItem
        metaClass = metadata.getSession().getClassNN(EntityLogItem.class);
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("user")));
        assertFalse(metadataTools.isPersistent(metaClass.getPropertyNN("attributes")));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("attributes")));

        // Folder
        metaClass = metadata.getSession().getClassNN(Folder.class);
        assertTrue(metadataTools.isPersistent(metaClass.getPropertyNN("name")));
        assertTrue(metadataTools.isNotPersistent(new Folder(), "itemStyle"));

        // UserSessionEntity
        metaClass = metadata.getSession().getClassNN(UserSessionEntity.class);
        assertFalse(metadataTools.isPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isNotPersistent(metadata.create(metaClass), "id"));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("login")));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("login")));

        // TestTransientEntity
        metaClass = metadata.getSession().getClassNN(TestNotPersistentEntity.class);
        assertFalse(metadataTools.isPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("id")));
        assertTrue(metadataTools.isNotPersistent(metadata.create(metaClass), "id"));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("name")));
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("info")));
    }

    @Test
    @Disabled
    public void testEmbeddedProperty() {
        // TestTransientEntity
        MetaClass metaClass = metadata.getSession().getClassNN(TestNotPersistentEntity.class);
        assertTrue(metadataTools.isNotPersistent(metaClass.getPropertyNN("embeddedRef")));
        assertFalse(metadataTools.isEmbedded(metaClass.getPropertyNN("embeddedRef")));
    }

    @Test
    public void testSystemLevel() {
        assertTrue(metadataTools.isSystemLevel(metadata.getClassNN(UserRole.class)));

        assertFalse(metadataTools.isSystemLevel(metadata.getClassNN(TestNotPersistentEntity.class)));

        MetaClass metaClass = metadata.getClassNN(User.class);
        assertTrue(metadataTools.isSystemLevel(metaClass.getPropertyNN("password")));

        assertTrue(metadataTools.isSystemLevel(metadata.getClassNN(SearchFolder.class)));
    }
}