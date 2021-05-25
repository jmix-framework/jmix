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

package io.jmix.search.index.mapping.processor;

import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.*;

/**
 * Contains information about indexed properties defined via field-mapping annotations or method implementations
 * within index definition interface marked with {@link JmixEntitySearchIndex}
 * <p>
 * Also it can be directly created in method implementation.
 * Method should fulfil the following requirements:
 * <ul>
 *     <li>default</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>Without parameters</li>
 * </ul>
 * <p><b>Note:</b> if definition method has implementation any field-mapping annotations on it will be ignored
 */
public class MappingDefinition {

    protected List<MappingDefinitionElement> elements;

    protected MappingDefinition(MappingDefinitionBuilder builder) {
        this.elements = builder.elements;
    }

    /**
     * Gets all {@link MappingDefinitionElement}
     *
     * @return List of {@link MappingDefinitionElement}
     */
    public List<MappingDefinitionElement> getElements() {
        return elements;
    }

    public static MappingDefinitionBuilder builder() {
        return new MappingDefinitionBuilder();
    }

    public static class MappingDefinitionBuilder {
        private final List<MappingDefinitionElement> elements = new ArrayList<>();

        public BuilderInitInput newElement() {
            return MappingDefinitionElement.newElement(this);
        }

        public MappingDefinition buildMappingDefinition() {
            return new MappingDefinition(this);
        }
    }

    protected static class MappingDefinitionElement {
        protected String[] includedProperties;
        protected String[] excludedProperties;
        protected Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
        protected Map<String, Object> parameters;

        protected MappingDefinitionElement(MappingDefinitionElementBuilder builder) {
            this.includedProperties = builder.includedProperties;
            this.excludedProperties = builder.excludedProperties;
            this.fieldMappingStrategyClass = builder.fieldMappingStrategyClass;
            this.parameters = builder.parameters == null ? Collections.emptyMap() : builder.parameters;
        }

        /**
         * Provides full name of properties that should be indexed.
         *
         * @return property names
         */
        protected String[] getIncludedProperties() {
            return includedProperties;
        }

        /**
         * Provides full name of properties that should NOT be indexed.
         *
         * @return property names
         */
        protected String[] getExcludedProperties() {
            return excludedProperties;
        }

        /**
         * Provides {@link FieldMappingStrategy} implementation class that should be used to map properties.
         *
         * @return {@link FieldMappingStrategy} implementation class
         */
        protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
            return fieldMappingStrategyClass;
        }

        protected Map<String, Object> getParameters() {
            return parameters;
        }

        protected static BuilderInitInput newElement(MappingDefinitionBuilder parentBuilder) {
            return new MappingDefinitionElementBuilder(parentBuilder);
        }
    }

    public static class MappingDefinitionElementBuilder implements
            BuilderInitInput,
            BuilderEntityPropertiesExcludeInput,
            BuilderFieldMappingStrategyInput,
            BuilderEntityPropertyOptionalInput {

        private final MappingDefinitionBuilder parentBuilder;

        private String[] includedProperties = new String[0];
        private String[] excludedProperties = new String[0];
        private Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
        private Map<String, Object> parameters = null;

        public MappingDefinitionElementBuilder(MappingDefinitionBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
        }

        @Override
        public MappingDefinitionElementBuilder includeProperties(String... properties) {
            this.includedProperties = properties;
            return this;
        }

        @Override
        public MappingDefinitionElementBuilder excludeProperties(String... properties) {
            this.excludedProperties = properties;
            return this;
        }

        @Override
        public MappingDefinitionElementBuilder usingFieldMappingStrategyClass(Class<? extends FieldMappingStrategy> fieldMappingStrategyClass) {
            this.fieldMappingStrategyClass = fieldMappingStrategyClass;
            return this;
        }

        public MappingDefinitionElementBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        public MappingDefinitionElementBuilder withParameter(String parameterName, Object parameterValue) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(parameterName, parameterValue);
            return this;
        }

        public MappingDefinitionBuilder buildElement() {
            registerElement(new MappingDefinitionElement(this));
            return parentBuilder;
        }

        protected void registerElement(MappingDefinitionElement element) {
            this.parentBuilder.elements.add(element);
        }
    }

    public interface BuilderInitInput {
        BuilderEntityPropertyOptionalInput includeProperties(String... properties);
    }

    public interface BuilderEntityPropertyOptionalInput extends BuilderFieldMappingStrategyInput, BuilderEntityPropertiesExcludeInput {
    }

    public interface BuilderEntityPropertiesExcludeInput {
        BuilderFieldMappingStrategyInput excludeProperties(String... properties);
    }

    public interface BuilderFieldMappingStrategyInput {
        MappingDefinitionElementBuilder usingFieldMappingStrategyClass(Class<? extends FieldMappingStrategy> fieldMappingStrategyClass);
    }
}
