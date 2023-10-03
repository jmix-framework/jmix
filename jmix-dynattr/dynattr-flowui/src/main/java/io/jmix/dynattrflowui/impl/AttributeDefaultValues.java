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

package io.jmix.dynattrflowui.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("dynat_AttributeDefaultValues")
public class AttributeDefaultValues {

    // todo is in Jmix ok to use constructor injections + final modifier
    protected final Metadata metadata;
    protected final DynAttrMetadata dynAttrMetadata;
    protected final TimeSource timeSource;
    protected final DataManager dataManager;
    protected final ReferenceToEntitySupport referenceToEntitySupport;

    public AttributeDefaultValues(Metadata metadata,
                                  DynAttrMetadata dynAttrMetadata,
                                  TimeSource timeSource,
                                  DataManager dataManager,
                                  ReferenceToEntitySupport referenceToEntitySupport) {
        this.metadata = metadata;
        this.dynAttrMetadata = dynAttrMetadata;
        this.timeSource = timeSource;
        this.dataManager = dataManager;
        this.referenceToEntitySupport = referenceToEntitySupport;
    }

    public void initDefaultAttributeValues(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        Collection<AttributeDefinition> attributes = dynAttrMetadata.getAttributes(metaClass);

        for (AttributeDefinition attribute : attributes) {
            setDefaultAttributeValue(entity, attribute, timeSource.currentTimestamp());
        }
    }

    protected void setDefaultAttributeValue(Object entity, AttributeDefinition attribute, Date currentTimestamp) {
        String propertyName = DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode());
        if (EntityValues.getValue(entity, propertyName) == null) {
            if (attribute.getDefaultValue() != null) {
                if (attribute.isCollection()) {
                    if (attribute.getDataType() == AttributeType.ENTITY) {
                        List<Object> defaultEntities = Stream.of(attribute.getDefaultValue())
                                .map(id -> reloadEntity(attribute, id))
                                .collect(Collectors.toList());
                        EntityValues.setValue(entity, propertyName, defaultEntities);
                    } else {
                        //noinspection unchecked
                        EntityValues.setValue(entity, propertyName,
                                new ArrayList<>((List<Object>) attribute.getDefaultValue()));
                    }
                } else {
                    if (attribute.getDataType() == AttributeType.ENTITY) {
                        EntityValues.setValue(entity, propertyName, reloadEntity(attribute, attribute.getDefaultValue()));
                    } else {
                        EntityValues.setValue(entity, propertyName, attribute.getDefaultValue());
                    }
                }
            }

            if (attribute.isDefaultDateCurrent()) {
                if (attribute.getDataType() == AttributeType.DATE_WITHOUT_TIME) {
                    EntityValues.setValue(entity, propertyName, DateUtils.truncate(currentTimestamp, Calendar.DATE));
                } else {
                    EntityValues.setValue(entity, propertyName, currentTimestamp);
                }
            }
        }
    }

    protected Object reloadEntity(AttributeDefinition attribute, Object entityId) {
        MetaClass metaClass = metadata.getClass(Objects.requireNonNull(attribute.getJavaType()));
        String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
        return dataManager.load(attribute.getJavaType())
                .query(String.format("e.%s = ?1", pkName), entityId)
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional()
                .orElse(null);
    }
}
