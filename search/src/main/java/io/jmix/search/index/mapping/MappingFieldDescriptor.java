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

import java.util.Collections;
import java.util.List;

public class MappingFieldDescriptor {

    protected String entityPropertyFullName; //TODO exclude this (have metaPropertyPath)?

    protected String indexPropertyFullName;

    //protected MetaClass rootEntityMetaClass;

    protected MetaPropertyPath metaPropertyPath;

    protected boolean standalone;

    protected FieldConfiguration fieldConfiguration;

    protected ValueMapper valueMapper;

    protected int order;

    protected List<MetaPropertyPath> instanceNameRelatedProperties;

    //todo runtime parameters


    public String getEntityPropertyFullName() {
        return entityPropertyFullName;
    }

    public String getIndexPropertyFullName() {
        return indexPropertyFullName;
    }

    /*public MetaClass getRootEntityMetaClass() {
        return rootEntityMetaClass;
    }*/

    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    public MetaClass getPropertyMetaClass() {
        return metaPropertyPath.getMetaClass();
    }

    public boolean isStandalone() {
        return standalone;
    }


    public void setEntityPropertyFullName(String entityPropertyFullName) {
        this.entityPropertyFullName = entityPropertyFullName;
    }

    public void setIndexPropertyFullName(String indexPropertyFullName) {
        this.indexPropertyFullName = indexPropertyFullName;
    }

    /*public void setRootEntityMetaClass(MetaClass rootEntityMetaClass) {
        this.rootEntityMetaClass = rootEntityMetaClass;
    }*/

    public void setMetaPropertyPath(MetaPropertyPath metaPropertyPath) {
        this.metaPropertyPath = metaPropertyPath;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setFieldConfiguration(FieldConfiguration fieldConfiguration) {
        this.fieldConfiguration = fieldConfiguration;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public JsonNode getValue(Object entity) {
        return valueMapper.getValue(entity, metaPropertyPath, Collections.emptyMap() /* runtime parameters */);
    }

    /*public ValueMapper getValueMapper() {
        return valueMapper;
    }*/

    public void setValueMapper(ValueMapper valueMapper) {
        this.valueMapper = valueMapper;
    }

    public ValueMapper getValueMapper() {
        return valueMapper;
    }

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
