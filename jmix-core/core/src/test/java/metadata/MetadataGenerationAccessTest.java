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

package metadata;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.impl.MetadataImpl;
import io.jmix.core.impl.MetadataLoader;
import io.jmix.core.impl.metadata.MetadataGeneration;
import io.jmix.core.impl.metadata.MetadataGenerationManager;
import io.jmix.core.impl.metadata.MetadataSessionCloneSupport;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.ExtFoo;
import test_support.app.entity.Pet;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
class MetadataGenerationAccessTest {

    @Autowired
    MetadataImpl metadata;
    @Autowired
    MetadataLoader metadataLoader;
    @Autowired
    ExtendedEntities extendedEntities;
    @Autowired
    MetadataGenerationManager generationManager;
    @Autowired
    MetadataSessionCloneSupport metadataSessionCloneSupport;

    @Test
    void getSession_afterPublish_returnsPublishedSession() {
        MetadataGeneration initialGeneration = generationManager.getCurrentGeneration();
        MetadataSessionCloneSupport.SessionCloneResult cloneResult =
                metadataSessionCloneSupport.cloneSession(initialGeneration);
        try {
            generationManager.publish(cloneResult.getSession(), cloneResult.getExtendedEntitiesState(), List.of());

            assertNotSame(initialGeneration.getSession(), cloneResult.getSession());
            assertSame(cloneResult.getSession(), metadata.getSession());
            assertSame(cloneResult.getSession().getClass(Pet.class), metadata.getClass(Pet.class));
        } finally {
            restore(initialGeneration);
        }
    }

    @Test
    void getCurrentStateSnapshot_afterPublish_returnsPublishedState() {
        MetadataGeneration initialGeneration = generationManager.getCurrentGeneration();
        MetadataSessionCloneSupport.SessionCloneResult cloneResult =
                metadataSessionCloneSupport.cloneSession(initialGeneration);
        try {
            generationManager.publish(cloneResult.getSession(), cloneResult.getExtendedEntitiesState(), List.of());

            assertNotSame(initialGeneration.getExtendedEntitiesState(), cloneResult.getExtendedEntitiesState());
            assertSame(cloneResult.getExtendedEntitiesState(), extendedEntities.getCurrentStateSnapshot());
        } finally {
            restore(initialGeneration);
        }
    }

    @Test
    void getSession_repeatedCalls_resolveGenerationManagerOnce() {
        MetadataImpl testMetadata = new MetadataImpl(metadataLoader);
        CountingProvider provider = new CountingProvider(generationManager);
        ReflectionTestUtils.setField(testMetadata, "metadataGenerationManagerProvider", provider);

        for (int i = 0; i < 10; i++) {
            testMetadata.getSession();
        }

        assertEquals(1, provider.calls);
    }

    @Test
    void getCurrentStateSnapshot_repeatedCalls_resolveGenerationManagerOnce() {
        ExtendedEntities testExtendedEntities = new ExtendedEntities(metadata);
        CountingProvider provider = new CountingProvider(generationManager);
        ReflectionTestUtils.setField(testExtendedEntities, "metadataGenerationManagerProvider", provider);

        MetaClass extFooMetaClass = metadata.getClass(ExtFoo.class);

        for (int i = 0; i < 10; i++) {
            testExtendedEntities.getCurrentStateSnapshot();
            testExtendedEntities.getOriginalMetaClass(extFooMetaClass);
        }

        assertEquals(1, provider.calls);
    }

    private void restore(MetadataGeneration generation) {
        Session session = generation.getSession();
        generationManager.publish(session, generation.getExtendedEntitiesState(), List.of());
    }

    private static class CountingProvider implements ObjectProvider<MetadataGenerationManager> {

        private final MetadataGenerationManager manager;
        private int calls;

        CountingProvider(MetadataGenerationManager manager) {
            this.manager = manager;
        }

        @Override
        public MetadataGenerationManager getObject() {
            calls++;
            return manager;
        }

        @Override
        public MetadataGenerationManager getIfAvailable() {
            calls++;
            return manager;
        }
    }
}
