/*
 * Copyright 2020 Haulmont.
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

package io.jmix.pessimisticlock.impl;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.pessimisticlock.entity.LockDescriptor;
import io.jmix.pessimisticlock.LockDescriptorProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.pessimisticlock.annotation.PessimisticLock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@code AnnotationLockDescriptorProvider} creates and collects
 * {@link LockDescriptor} objects for locks annotated by {@link PessimisticLock}
 * annotation from data model objects.
 */
@Component("psmlock_AnnotationLockDescriptorProvider")
public class AnnotationLockDescriptorProvider implements LockDescriptorProvider {

    private final Logger log = LoggerFactory.getLogger(AnnotationLockDescriptorProvider.class);

    protected final ExtendedEntities extendedEntities;
    protected final MetadataTools metadataTools;
    protected final Metadata metadata;
    protected final MeterRegistry meterRegistry;

    public AnnotationLockDescriptorProvider(ExtendedEntities extendedEntities,
                                            MetadataTools metadataTools,
                                            Metadata metadata,
                                            MeterRegistry meterRegistry) {
        this.extendedEntities = extendedEntities;
        this.metadataTools = metadataTools;
        this.metadata = metadata;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<LockDescriptor> getLockDescriptors() {
        Timer.Sample sample = Timer.start(meterRegistry);
        List<LockDescriptor> config = new ArrayList<>();
        try {
            log.info("Collecting pessimistic locks configuration annotations");

            for (MetaClass metaClass : metadata.getSession().getClasses()) {
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                Map<String, Object> attributes =
                        metadataTools.getMetaAnnotationAttributes(originalMetaClass.getAnnotations(),
                                PessimisticLock.class);

                if (!attributes.isEmpty()) {
                    String originalName = originalMetaClass.getName();
                    Integer timeoutSec = (Integer) attributes.get("timeoutSec");
                    LockDescriptor descriptor = new LockDescriptor(originalName, timeoutSec);
                    config.add(descriptor);
                }
            }
        } finally {
            sample.stop(meterRegistry.timer("jmix.AnnotationLockDescriptorProvider.loadConfig"));
        }

        return config;
    }
}
