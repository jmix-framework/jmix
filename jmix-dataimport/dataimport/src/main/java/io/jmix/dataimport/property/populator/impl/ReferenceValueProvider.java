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

package io.jmix.dataimport.property.populator.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dataimport.DuplicateEntityManager;
import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy;
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferencePropertyMapping;
import io.jmix.dataimport.exception.ImportException;
import io.jmix.dataimport.property.populator.PropertyMappingContext;
import io.jmix.dataimport.property.populator.PropertyMappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component("datimp_ReferenceValueProvider")
public class ReferenceValueProvider {
    public static final Logger log = LoggerFactory.getLogger(ReferenceValueProvider.class);
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ReferenceCreator referenceCreator;
    @Autowired
    protected DuplicateEntityManager duplicateEntityManager;
    @Autowired
    protected PropertyMappingUtils propertyMappingUtils;

    @Nullable
    public Object getSingleEntity(PropertyMappingContext context, @Nullable List<Object> createdReferences) {
        MetaProperty referenceMetaProperty = context.getMetaProperty();
        if (metadataTools.isEmbedded(referenceMetaProperty)) {
            return processEmbeddedReferenceMapping(context);
        }

        Range.Cardinality cardinality = referenceMetaProperty.getRange().getCardinality();
        switch (cardinality) {
            case MANY_TO_ONE:
                return processManyToOneReferenceMapping(context, createdReferences);
            case ONE_TO_ONE:
                return processOneToOneReferenceMapping(context);
            default:
                break;
        }
        return null;
    }

    @Nullable
    public Collection<Object> getEntityCollection(Object entityToPopulate, PropertyMappingContext context) {
        MetaProperty referenceMetaProperty = context.getMetaProperty();
        Range.Cardinality cardinality = referenceMetaProperty.getRange().getCardinality();
        if (cardinality == Range.Cardinality.ONE_TO_MANY) {
            ReferenceMultiFieldPropertyMapping referenceMapping = (ReferenceMultiFieldPropertyMapping) context.getPropertyMapping();
            ReferenceImportPolicy referenceImportPolicy = referenceMapping.getReferenceImportPolicy();
            if (referenceImportPolicy == ReferenceImportPolicy.CREATE) {
                return referenceCreator.createEntityCollection(entityToPopulate, context);
            }
        }
        return null;
    }

    @Nullable
    protected Object processEmbeddedReferenceMapping(PropertyMappingContext context) {
        PropertyMapping referenceMapping = context.getPropertyMapping();
        ReferenceImportPolicy referenceImportPolicy = getReferenceImportPolicy(referenceMapping);
        if (referenceImportPolicy == ReferenceImportPolicy.CREATE) {
            return referenceCreator.createEntity(context);
        }
        return null;
    }

    @Nullable
    protected Object processOneToOneReferenceMapping(PropertyMappingContext context) {
        PropertyMapping referenceMapping = context.getPropertyMapping();
        ReferenceImportPolicy referenceImportPolicy = getReferenceImportPolicy(referenceMapping);
        if (referenceImportPolicy != null) {
            Object resultValue;
            if (referenceImportPolicy == ReferenceImportPolicy.CREATE) {
                resultValue = referenceCreator.createEntity(context);
            } else {
                resultValue = loadEntity(context);
                if (resultValue == null) {
                    switch (referenceImportPolicy) {
                        case CREATE_IF_MISSING:
                            resultValue = referenceCreator.createEntity(context);
                            break;
                        case IGNORE_IF_MISSING:
                            logIgnoredReference(context);
                            break;
                        case FAIL_IF_MISSING:
                            processFailedReference(context);
                            break;
                        default:
                            break;
                    }
                }
            }
            return resultValue;
        }
        return null;
    }

    @Nullable
    protected Object processManyToOneReferenceMapping(PropertyMappingContext context, @Nullable List<Object> createdReferences) {
        PropertyMapping referenceMapping = context.getPropertyMapping();
        ReferenceImportPolicy referenceImportPolicy = getReferenceImportPolicy(referenceMapping);
        if (referenceImportPolicy != null) {
            Object resultValue;
            if (referenceImportPolicy == ReferenceImportPolicy.CREATE) {
                resultValue = referenceCreator.createEntity(context, createdReferences);
            } else {
                resultValue = loadEntity(context);
                if (resultValue == null) {
                    switch (referenceImportPolicy) {
                        case CREATE_IF_MISSING:
                            resultValue = referenceCreator.createEntity(context, createdReferences);
                            break;
                        case IGNORE_IF_MISSING:
                            logIgnoredReference(context);
                            break;
                        case FAIL_IF_MISSING:
                            processFailedReference(context);
                            break;
                        default:
                            break;
                    }
                }
            }
            return resultValue;
        }
        return null;
    }

    @Nullable
    protected ReferenceImportPolicy getReferenceImportPolicy(PropertyMapping referenceMapping) {
        ReferenceImportPolicy referenceImportPolicy = null;
        if (referenceMapping instanceof ReferenceMultiFieldPropertyMapping) {
            referenceImportPolicy = ((ReferenceMultiFieldPropertyMapping) referenceMapping).getReferenceImportPolicy();
        } else if (referenceMapping instanceof ReferencePropertyMapping) {
            referenceImportPolicy = ((ReferencePropertyMapping) referenceMapping).getReferenceImportPolicy();
        }
        return referenceImportPolicy;
    }

    @Nullable
    protected Object loadEntity(PropertyMappingContext context) {
        MetaProperty referenceMetaProperty = context.getMetaProperty();
        MetaClass referenceMetaClass = referenceMetaProperty.getRange().asClass();

        Map<String, Object> propertyValues = propertyMappingUtils.getPropertyValues(context);

        if (!propertyValues.isEmpty()) {
            return duplicateEntityManager.load(referenceMetaClass.getJavaClass(), propertyValues, null);
        }

        return null;
    }

    protected void logIgnoredReference(PropertyMappingContext context) {
        log.trace(String.format("Existing value not found for property [%s] in entity [%s] by values [%s], but new one is not created by policy.",
                context.getPropertyMapping().getEntityPropertyName(),
                context.getOwnerEntityMetaClass().getName(),
                propertyMappingUtils.getPropertyValues(context)));
    }

    protected void processFailedReference(PropertyMappingContext context) {
        log.warn(String.format("Existing value not found for property [%s] in entity [%s] by values [%s], entity import failed.",
                context.getPropertyMapping().getEntityPropertyName(),
                context.getOwnerEntityMetaClass().getName(),
                propertyMappingUtils.getPropertyValues(context)));
        throw new ImportException(String.format("Existing value not found for property [%s] in entity [%s]", context.getPropertyMapping().getEntityPropertyName(),
                context.getOwnerEntityMetaClass().getName()));
    }
}
