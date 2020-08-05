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

package io.jmix.data.impl.lazyloading;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.internal.indirection.UnitOfWorkQueryValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

@Component(LazyLoadingHelper.NAME)
public class LazyLoadingHelper {

    public static final String NAME = "jmix_LazyLoadingHelper";

    private static final Logger log = LoggerFactory.getLogger(LazyLoadingHelper.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected BeanFactory beanFactory;

    public void replaceValueHolders(JmixEntity instance, List<FetchPlan> fetchPlans) {
        Map<JmixEntity, Set<FetchPlan>> collectedFetchPlans = new HashMap<>();

        for (FetchPlan fetchPlan : fetchPlans) {
            collectFetchPlans(instance, fetchPlan, collectedFetchPlans);
        }

        for (Map.Entry<JmixEntity, Set<FetchPlan>> entry : collectedFetchPlans.entrySet()) {
            MetaClass metaClass = metadata.getClass(entry.getKey().getClass());
            for (MetaProperty property : metaClass.getProperties()) {
                if (property.getRange().isClass() && !isPropertyContainedInFetchPlans(property, entry.getValue())) {
                    replaceValueHoldersInternal(entry.getKey(), property);
                }
            }
        }
    }

    protected void replaceValueHoldersInternal(JmixEntity instance, MetaProperty property) {
        if (entityStates.isLoaded(instance, property.getName())) {
            return;
        }
        switch (property.getRange().getCardinality()) {
            case ONE_TO_ONE:
                try {
                    Field declaredField = instance.getClass().getDeclaredField("_persistence_" + property.getName() + "_vh");
                    boolean accessible = declaredField.isAccessible();
                    declaredField.setAccessible(true);
                    Object fieldInstance = declaredField.get(instance);
                    if (fieldInstance instanceof JmixAbstractValueHolder) {
                        declaredField.setAccessible(accessible);
                        return;
                    }
                    if (metadataTools.isOwningSide(property)) {
                        declaredField.set(instance,
                                new JmixWrappingValueHolder((UnitOfWorkQueryValueHolder) fieldInstance,
                                        dataManager,
                                        metadata,
                                        metadataTools));
                    } else {
                        MetaProperty inverseProperty = property.getInverse();
                        declaredField.set(instance,
                                new JmixSingleValueHolder(inverseProperty.getName(),
                                        property.getJavaType(),
                                        instance.__getEntityEntry().getEntityId(),
                                        dataManager,
                                        metadata,
                                        metadataTools));
                    }
                    declaredField.setAccessible(accessible);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                }
                break;
            case MANY_TO_ONE:
                try {
                    Field declaredField = instance.getClass().getDeclaredField("_persistence_" + property.getName() + "_vh");
                    boolean accessible = declaredField.isAccessible();
                    declaredField.setAccessible(true);
                    Object fieldInstance = declaredField.get(instance);
                    if (fieldInstance instanceof JmixAbstractValueHolder) {
                        declaredField.setAccessible(accessible);
                        return;
                    }
                    declaredField.set(instance,
                            new JmixWrappingValueHolder((UnitOfWorkQueryValueHolder) fieldInstance,
                                    dataManager,
                                    metadata,
                                    metadataTools));
                    declaredField.setAccessible(accessible);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                }
                break;
            case ONE_TO_MANY:
            case MANY_TO_MANY:
                IndirectCollection fieldValue = instance.__getEntityEntry().getAttributeValue(property.getName());
                if (fieldValue == null || fieldValue.getValueHolder() instanceof JmixAbstractValueHolder) {
                    return;
                }
                fieldValue.setValueHolder(new JmixCollectionValueHolder(
                        property.getName(),
                        instance.getClass(),
                        instance.__getEntityEntry().getEntityId(),
                        dataManager,
                        beanFactory.getBean(FetchPlanBuilder.class, instance.getClass()),
                        metadata));
                break;
        }
    }

    protected void collectFetchPlans(JmixEntity instance, FetchPlan fetchPlan, Map<JmixEntity, Set<FetchPlan>> collectedFetchPlans) {
        Set<FetchPlan> fetchPlans = collectedFetchPlans.get(instance);
        if (fetchPlans == null) {
            fetchPlans = new HashSet<>();
            collectedFetchPlans.put(instance, fetchPlans);
        } else if (fetchPlans.contains(fetchPlan)) {
            return;
        }
        fetchPlans.add(fetchPlan);

        MetaClass metaClass = metadata.getClass(instance.getClass());
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metaClass.getProperty(property.getName());
            if (!metaProperty.getRange().isClass() && !isLazyFetchedLocalAttribute(metaProperty)
                    || !metadataTools.isPersistent(metaProperty))
                continue;

            Object value = EntityValues.getValue(instance, property.getName());
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    for (Object item : new ArrayList(((Collection) value))) {
                        if (item instanceof JmixEntity) {
                            collectFetchPlans((JmixEntity) item, propertyFetchPlan, collectedFetchPlans);
                        }
                    }
                } else if (value instanceof JmixEntity) {
                    collectFetchPlans((JmixEntity) value, propertyFetchPlan, collectedFetchPlans);
                }
            }
        }
    }

    protected boolean isPropertyContainedInFetchPlans(MetaProperty metaProperty, Set<FetchPlan> fetchPlans) {
        boolean contains = false;
        for (FetchPlan fetchPlan : fetchPlans) {
            if (fetchPlan.containsProperty(metaProperty.getName())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    protected boolean isLazyFetchedLocalAttribute(MetaProperty metaProperty) {
        AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();
        Basic annotation = annotatedElement.getAnnotation(Basic.class);
        return annotation != null && annotation.fetch() == FetchType.LAZY;
    }
}
