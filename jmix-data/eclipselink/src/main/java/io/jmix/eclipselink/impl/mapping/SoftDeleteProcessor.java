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

package io.jmix.eclipselink.impl.mapping;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.persistence.DescriptorProcessor;
import io.jmix.eclipselink.persistence.DescriptorProcessorContext;
import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.OneToOne;

/**
 * Modifies mapping to support soft delete feature. Updates softDeletionForBatch and
 * softDeletionForValueHolder properties using corresponding setters.
 */
@Component("eclipselink_SoftDeleteProcessor")
public class SoftDeleteProcessor implements MappingProcessor, DescriptorProcessor {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;

    @Override
    public void process(MappingProcessorContext context) {
        DatabaseMapping mapping = context.getMapping();

        MetaClass metaClass = metadata.getClass(mapping.getDescriptor().getJavaClass());
        MetaProperty metaProperty = metaClass.getProperty(mapping.getAttributeName());

        if (mapping.isOneToOneMapping()) {
            OneToOneMapping oneToOneMapping = (OneToOneMapping) mapping;
            if (metadataTools.isSoftDeletable(oneToOneMapping.getReferenceClass())) {
                if (mapping.isManyToOneMapping()) {
                    oneToOneMapping.setSoftDeletionForBatch(false);
                    oneToOneMapping.setSoftDeletionForValueHolder(false);
                } else {
                    OneToOne oneToOne = metaProperty.getAnnotatedElement().getAnnotation(OneToOne.class);
                    if (oneToOne != null) {
                        if (Strings.isNullOrEmpty(oneToOne.mappedBy())) {
                            oneToOneMapping.setSoftDeletionForBatch(false);
                            oneToOneMapping.setSoftDeletionForValueHolder(false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void process(DescriptorProcessorContext context) {
        ClassDescriptor descriptor = context.getDescriptor();

        if (metadataTools.isSoftDeletable(descriptor.getJavaClass())) {
            String deletedDateProperty = metadataTools.findDeletedDateProperty(descriptor.getJavaClass());
            Preconditions.checkNotNull(deletedDateProperty);
            descriptor.setDeletePredicate(entity -> {
                if (EntityValues.isSoftDeletionSupported(entity)) {
                    return entityStates.isLoaded(entity, deletedDateProperty) && EntityValues.isSoftDeleted(entity);
                }
                return false;
            });
        }
    }
}
