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

package io.jmix.search.index.mapping.processor.impl.dynattr;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.exception.IndexConfigurationException;
import io.jmix.search.index.annotation.DynamicAttributes;
import io.jmix.search.index.impl.dynattr.DynamicAttributesModulePresenceChecker;
import io.jmix.search.index.mapping.DynamicAttributesGroupConfiguration;
import io.jmix.search.index.mapping.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.index.mapping.processor.AbstractFieldAnnotationProcessor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Processor for the {@link DynamicAttributes} annotation used in field mapping definitions.
 * This class provides the functionality to process the annotation, extract its parameters,
 * and define a mapping of dynamic attributes to be indexed.
 * <p>
 * If the 'Dynamic attributes' module is absent, the {@link IndexConfigurationException} is thrown.
 */
@Component("search_DynamicAttributesAnnotationProcessor")
public class DynamicAttributesAnnotationProcessor extends AbstractFieldAnnotationProcessor<DynamicAttributes> {

    protected final DynamicAttributesModulePresenceChecker modulePresenceChecker;

    public DynamicAttributesAnnotationProcessor(DynamicAttributesModulePresenceChecker modulePresenceChecker) {
        this.modulePresenceChecker = modulePresenceChecker;
    }

    @Override
    public Class<DynamicAttributes> getAnnotationClass() {
        return DynamicAttributes.class;
    }

    /**
     * Processes the {@link DynamicAttributes} annotation by verifying the presence of the necessary module
     * and adding a dynamic attributes group to the specified mapping definition builder.
     * <p>
     * If the 'Dynamic Attributes' module is not present, this method throws an {@link IndexConfigurationException}.
     *
     * @param builder the {@link MappingDefinitionBuilder} used to define the mapping of attributes
     * @param rootEntityMetaClass the root {@link MetaClass} of the entity being processed
     * @param annotation the {@link DynamicAttributes} annotation containing configuration for dynamic attributes
     * @throws IndexConfigurationException if the Dynamic Attributes module is not present
     */
    @Override
    protected void processSpecificAnnotation(MappingDefinitionBuilder builder,
                                             MetaClass rootEntityMetaClass,
                                             DynamicAttributes annotation) {
        if (!modulePresenceChecker.isModulePresent()) {
            throw new IndexConfigurationException("Dynamic attributes module is not present in the application. " +
                    "Make sure the module is properly included in your dependencies.");
        }
        builder.addDynamicAttributesGroup(createDefinition(annotation));
    }


    protected DynamicAttributesGroupConfiguration createDefinition(DynamicAttributes annotation) {
        return DynamicAttributesGroupConfiguration
                .builder()
                .excludeCategories(annotation.excludeCategories())
                .excludeProperties(annotation.excludeAttributes())
                .withParameters(createParameters(annotation))
                .withReferenceAttributesIndexingMode(annotation.referenceAttributesIndexingMode())
                .withFieldMappingStrategyClass(AutoMappingStrategy.class)
                .build();
    }


    @Override
    protected Map<String, Object> createParameters(DynamicAttributes specificAnnotation) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (StringUtils.isNotBlank(specificAnnotation.analyzer())) {
            parameters.put(ParameterKeys.ANALYZER, specificAnnotation.analyzer());
        }
        return parameters;
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return AutoMappingStrategy.class;
    }
}
