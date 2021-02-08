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

import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updates database mapping to support embedded parameters.
 */
@Component("eclipselink_EmbeddedAttributesMappingProcessor")
public class EmbeddedAttributesMappingProcessor implements MappingProcessor {
    @Autowired
    protected Metadata metadata;

    @Override
    public void process(MappingProcessorContext context) {
        DatabaseMapping mapping = context.getMapping();

        if (mapping instanceof AggregateObjectMapping) {
            MetaClass metaClass = metadata.getClass(mapping.getDescriptor().getJavaClass());
            MetaProperty metaProperty = metaClass.getProperty(mapping.getAttributeName());

            EmbeddedParameters embeddedParameters =
                    metaProperty.getAnnotatedElement().getAnnotation(EmbeddedParameters.class);

            if (embeddedParameters != null && !embeddedParameters.nullAllowed()) {
                ((AggregateObjectMapping) mapping).setIsNullAllowed(false);
            }
        }
    }
}
