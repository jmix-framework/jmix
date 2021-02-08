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

import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Updates mapping by setting a correct fetch type - lazy or eager.
 * Relational mappings: 1:1, 1:m, m:1, m:m are set to lazy. Other types like {@link org.eclipse.persistence.mappings.AggregateObjectMapping}
 * are set to eager.
 */
@Component("eclipselink_FetchTypeMappingProcessor")
public class FetchTypeMappingProcessor implements MappingProcessor {
    private static final Logger log = LoggerFactory.getLogger(FetchTypeMappingProcessor.class);

    @Override
    public void process(MappingProcessorContext context) {
        DatabaseMapping mapping = context.getMapping();
        String entityClassName = mapping.getDescriptor().getJavaClass().getSimpleName();

        if (mapping.isOneToOneMapping() || mapping.isOneToManyMapping()
                || mapping.isManyToOneMapping() || mapping.isManyToManyMapping()) {
            if (!mapping.isLazy()) {
                mapping.setIsLazy(true);
                log.warn("EAGER fetch type detected for reference field {} of entity {}; Set to LAZY",
                        mapping.getAttributeName(), entityClassName);
            }
        } else {
            if (mapping.isLazy()) {
                mapping.setIsLazy(false);
                log.warn("LAZY fetch type detected for basic field {} of entity {}; Set to EAGER",
                        mapping.getAttributeName(), entityClassName);
            }
        }
    }
}
