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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains details of single mapped field.
 */
public class MappingFieldDescriptor {

    protected String entityPropertyFullName;

    protected String indexPropertyFullName;

    protected MetaPropertyPath metaPropertyPath;

    protected boolean standalone;

    protected FieldConfiguration fieldConfiguration;

    protected PropertyValueExtractor propertyValueExtractor;

    protected int order;

    protected List<MetaPropertyPath> instanceNameRelatedProperties;

    protected Map<String, Object> parameters = Collections.emptyMap();

    /**
     * Gets full property name in metamodel.
     *
     * @return property name
     */
    public String getEntityPropertyFullName() {
        return entityPropertyFullName;
    }

    /**
     * Gets full name of field in index.
     *
     * @return index field name
     */
    public String getIndexPropertyFullName() {
        return indexPropertyFullName;
    }

    /**
     * Gets metamodel property.
     *
     * @return {@link MetaPropertyPath}
     */
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    /**
     * Gets meta class of metamodel property.
     *
     * @return {@link MetaClass}
     */
    public MetaClass getPropertyMetaClass() {
        return metaPropertyPath.getMetaClass();
    }

    /**
     * @return true if this field doesn't have metamodel property and exists within index only, false otherwise
     */
    public boolean isStandalone() {
        return standalone;
    }


    public void setEntityPropertyFullName(String entityPropertyFullName) {
        this.entityPropertyFullName = entityPropertyFullName;
    }

    public void setIndexPropertyFullName(String indexPropertyFullName) {
        this.indexPropertyFullName = indexPropertyFullName;
    }

    public void setMetaPropertyPath(MetaPropertyPath metaPropertyPath) {
        this.metaPropertyPath = metaPropertyPath;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    /**
     * Gets field configuration
     *
     * @return {@link FieldConfiguration}
     */
    public FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setFieldConfiguration(FieldConfiguration fieldConfiguration) {
        this.fieldConfiguration = fieldConfiguration;
    }

    /**
     * Gets descriptor order based on mapping strategy it created by.
     * If several descriptors are related to the same field the one with the latest order will be used.
     *
     * @return order
     */
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Extracts value from entity instance
     *
     * @param entity instance
     * @return value as json
     */
    public JsonNode getValue(Object entity) {
        return propertyValueExtractor.getValue(entity, metaPropertyPath, getParameters());
    }

    public void setPropertyValueExtractor(PropertyValueExtractor propertyValueExtractor) {
        this.propertyValueExtractor = propertyValueExtractor;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(parameters);
    }

    /**
     * Gets all properties that used in instance name value.
     * Makes sense only if current {@link MappingFieldDescriptor} relates to reference property
     *
     * @return list of {@link MetaPropertyPath}
     */
    public List<MetaPropertyPath> getInstanceNameRelatedProperties() {
        return instanceNameRelatedProperties;
    }

    public void setInstanceNameRelatedProperties(List<MetaPropertyPath> instanceNameRelatedProperties) {
        this.instanceNameRelatedProperties = instanceNameRelatedProperties;
    }

    @Override
    public String toString() {
        return "MappingFieldDescriptor{" +
                "entityPropertyFullName='" + entityPropertyFullName + '\'' +
                ", indexPropertyFullName='" + indexPropertyFullName + '\'' +
                ", metaPropertyPath=" + metaPropertyPath +
                '}';
    }
}
