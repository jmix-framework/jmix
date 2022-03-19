/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.configuration.mapping;

import io.jmix.dataimport.property.populator.CustomMappingContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapping for reference property that is mapped by multiple data fields from the input data.
 * This mapping allows to set:
 * <ul>
 *     <li>Entity property name: name of the reference property</li>
 *     <li>Data field name (optional): name of the data field that contains a raw values of properties for the reference entity.
 *         <b>Note:</b> this field is helpful for JSON (if there is a separate object for reference property)
 *         or XML (if there is a separate tag for reference property).
 *     </li>
 *     <li>Reference import policy: {@link ReferenceImportPolicy}</li>
 *     <li>Lookup property names: property names by which existing entity should be searched.</li>
 *     <li>Reference property mappings: list of {@link PropertyMapping} for reference entity properties.</li>
 * </ul>
 */
public class ReferenceMultiFieldPropertyMapping implements PropertyMapping {
    protected String entityPropertyName;
    protected String dataFieldName; //optional
    protected List<PropertyMapping> referencePropertyMappings;
    protected List<String> lookupPropertyNames;
    protected ReferenceImportPolicy referenceImportPolicy;

    private ReferenceMultiFieldPropertyMapping(Builder builder) {
        this.entityPropertyName = builder.entityPropertyName;
        this.referencePropertyMappings = builder.referencePropertyMappings;
        this.lookupPropertyNames = builder.lookupPropertyNames;
        this.referenceImportPolicy = builder.referenceImportPolicy;
        this.dataFieldName = builder.dataFieldName;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public ReferenceMultiFieldPropertyMapping setDataFieldName(String dataFieldName) {
        this.dataFieldName = dataFieldName;
        return this;
    }

    public List<PropertyMapping> getReferencePropertyMappings() {
        return referencePropertyMappings;
    }

    public ReferenceMultiFieldPropertyMapping setReferencePropertyMappings(List<PropertyMapping> referencePropertyMappings) {
        this.referencePropertyMappings = referencePropertyMappings;
        return this;
    }

    public List<String> getLookupPropertyNames() {
        return lookupPropertyNames;
    }

    public ReferenceMultiFieldPropertyMapping setLookupPropertyNames(List<String> lookupPropertyNames) {
        this.lookupPropertyNames = lookupPropertyNames;
        return this;
    }

    public ReferenceImportPolicy getReferenceImportPolicy() {
        return referenceImportPolicy;
    }

    public String getEntityPropertyName() {
        return entityPropertyName;
    }

    public static Builder builder(String entityPropertyName, ReferenceImportPolicy policy) {
        return new Builder(entityPropertyName, policy);
    }

    public static class Builder {
        private String entityPropertyName;
        private List<PropertyMapping> referencePropertyMappings = new ArrayList<>();
        private List<String> lookupPropertyNames = new ArrayList<>();
        private ReferenceImportPolicy referenceImportPolicy;
        private String dataFieldName; //optional - makes sense for XML and JSON only
        private boolean lookupByAllSimpleProperties;

        public Builder(String entityPropertyName, ReferenceImportPolicy referenceImportPolicy) {
            this.entityPropertyName = entityPropertyName;
            this.referenceImportPolicy = referenceImportPolicy;
        }

        /**
         * Adds a mapping for specified simple property.
         *
         * @param entityPropertyName entity property name
         * @param dataFieldName      name of the field from input data that contains a raw value of property
         * @return current instance of builder
         */
        public Builder addSimplePropertyMapping(String entityPropertyName, String dataFieldName) {
            this.referencePropertyMappings.add(new SimplePropertyMapping(entityPropertyName, dataFieldName));
            return this;
        }

        /**
         * Adds a custom mapping for property.
         *
         * @param entityPropertyName  entity property name
         * @param customValueFunction function to get value for the
         * @return current instance of builder
         */
        public Builder addCustomPropertyMapping(String entityPropertyName,
                                                Function<CustomMappingContext, Object> customValueFunction) {
            this.referencePropertyMappings.add(new CustomPropertyMapping(entityPropertyName, customValueFunction));
            return this;
        }

        /**
         * Creates and adds a property mapping for the reference property mapped by one data field.
         *
         * @param entityPropertyName reference property name
         * @param dataFieldName      name of the field from input data that contains a raw value of lookup property
         * @param lookupPropertyName property name from the reference entity
         * @param policy             reference import policy
         * @return current instance of builder
         * @see ReferencePropertyMapping
         */
        public Builder addReferencePropertyMapping(String entityPropertyName,
                                                   String dataFieldName,
                                                   String lookupPropertyName,
                                                   ReferenceImportPolicy policy) {
            this.referencePropertyMappings.add(new ReferencePropertyMapping(entityPropertyName, dataFieldName, lookupPropertyName, policy));
            return this;
        }

        /**
         * Adds specified property mapping. Property mapping can be:
         * <ul>
         *     <li>{@link SimplePropertyMapping}</li>
         *     <li>{@link CustomPropertyMapping}</li>
         *     <li>{@link ReferencePropertyMapping}</li>
         *     <li>{@link ReferenceMultiFieldPropertyMapping}</li>
         * </ul>
         *
         * @param propertyMapping property mapping
         * @return current instance of builder
         * @see ReferenceMultiFieldPropertyMapping.Builder
         */
        public Builder addPropertyMapping(PropertyMapping propertyMapping) {
            this.referencePropertyMappings.add(propertyMapping);
            return this;
        }

        /**
         * Sets names of the properties by which an existing entity will be searched
         *
         * @param lookupPropertyNames names of the properties by which an existing entity will be searched
         * @return current instance of builder
         */
        public Builder withLookupPropertyNames(List<String> lookupPropertyNames) {
            this.lookupPropertyNames = lookupPropertyNames;
            return this;
        }

        /**
         * All simple properties for which mappings are set will be used to lookup existing entity.
         *
         * @return current instance of builder
         */
        public Builder lookupByAllSimpleProperties() {
            this.lookupByAllSimpleProperties = true;
            return this;
        }

        /**
         * Sets names of the properties by which an existing entity will be searched
         *
         * @param lookupPropertyNames names of the properties by which an existing entity will be searched
         * @return current instance of builder
         */
        public Builder withLookupPropertyNames(String... lookupPropertyNames) {
            this.lookupPropertyNames = Arrays.asList(lookupPropertyNames);
            return this;
        }

        /**
         * Sets a data field name.
         * <br>
         * <b>Note:</b> actual for JSON/XML formats
         *
         * @param dataFieldName field/tag name from the input data that has raw values of the reference entity properties
         * @return current instance of builder
         */
        public Builder withDataFieldName(String dataFieldName) {
            this.dataFieldName = dataFieldName;
            return this;
        }

        public ReferenceMultiFieldPropertyMapping build() {
            if (lookupByAllSimpleProperties) {
                this.lookupPropertyNames = referencePropertyMappings.stream().map(PropertyMapping::getEntityPropertyName).collect(Collectors.toList());
            }
            validate();
            return new ReferenceMultiFieldPropertyMapping(this);
        }

        protected void validate() {
            if (referenceImportPolicy == null) {
                throw new IllegalArgumentException(String.format("Reference import policy is not set for property [%s]", entityPropertyName));
            }
            if (referenceImportPolicy != ReferenceImportPolicy.CREATE && (CollectionUtils.isEmpty(lookupPropertyNames))) {
                throw new IllegalArgumentException(String.format("Lookup properties are not set for property [%s]", entityPropertyName));
            }
        }
    }
}
