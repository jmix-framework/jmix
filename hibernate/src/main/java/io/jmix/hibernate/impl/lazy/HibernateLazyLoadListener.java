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

package io.jmix.hibernate.impl.lazy;

import io.jmix.core.*;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.constraint.InMemoryConstraint;
import io.jmix.core.datastore.DataStoreAfterEntityLoadEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component("hibernate_HibernateLazyLoadListener")
public class HibernateLazyLoadListener implements DataStoreEventListener {
    private static final Logger log = LoggerFactory.getLogger(HibernateLazyLoadListener.class);

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
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected ExtendedEntities extendedEntities;

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        FetchPlan fetchPlan = context.getFetchPlan();
        if (fetchPlan == null) {
            fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.LOCAL);
        }
        for (Object entity : event.getResultEntities()) {
            replaceValueHolders(entity, context, fetchPlan);
        }
    }

    public void replaceValueHolders(Object instance, LoadContext loadContext, FetchPlan fetchPlan) {
        Map<Object, Set<FetchPlan>> collectedFetchPlans = new HashMap<>();

        if (fetchPlan != null) {
            collectFetchPlans(instance, fetchPlan, collectedFetchPlans);
        }

        boolean softDeletion = loadContext.isSoftDeletion();
        Map<String, Serializable> hints = loadContext.getHints();
        List<AccessConstraint<?>> constraints = (List<AccessConstraint<?>>) loadContext.getAccessConstraints().stream()
                .filter(ac -> ac instanceof InMemoryConstraint)
                .collect(Collectors.toList());

        for (Map.Entry<Object, Set<FetchPlan>> entry : collectedFetchPlans.entrySet()) {
            MetaClass metaClass = metadata.getClass(entry.getKey().getClass());
            for (MetaProperty property : metaClass.getProperties()) {
                if (property.getRange().isClass() && !isPropertyContainedInFetchPlans(property, entry.getValue())) {
                    replaceValueHoldersInternal(entry.getKey(), property, softDeletion, hints, constraints);
                }
            }
        }
    }

    protected void replaceValueHoldersInternal(Object instance, MetaProperty property, boolean softDeletion,
                                               Map<String, Serializable> hints, List<AccessConstraint<?>> constraints) {
        if (entityStates.isLoaded(instance, property.getName())) {
            return;
        }
//        JmixAbstractValueHolder vh;
        switch (property.getRange().getCardinality()) {
            case ONE_TO_ONE:
                try {
                    Field declaredField = instance.getClass().getDeclaredField(property.getName());
                    boolean accessible = declaredField.isAccessible();
                    declaredField.setAccessible(true);
                    Object fieldInstance = declaredField.get(instance);
//                    if (fieldInstance instanceof JmixAbstractValueHolder) {
//                        declaredField.setAccessible(accessible);
//                        return;
//                    }
                    if (metadataTools.isOwningSide(property)) {
//                        UnitOfWorkQueryValueHolder originalValueHolder = (UnitOfWorkQueryValueHolder) fieldInstance;
//                        if (originalValueHolder.getWrappedValueHolder().isInstantiated()
//                                || !(originalValueHolder.getWrappedValueHolder() instanceof QueryBasedValueHolder)) {
//                            declaredField.setAccessible(accessible);
//                            return;
//                        }
//                        QueryBasedValueHolder wrappedValueHolder = (QueryBasedValueHolder) originalValueHolder.getWrappedValueHolder();
//                        AtomicReference<String> fieldName = new AtomicReference<>();
//                        ExpressionIterator iterator = new ExpressionIterator() {
//                            @Override
//                            public void iterate(Expression each) {
//                                if (each instanceof ParameterExpression) {
//                                    fieldName.set(((ParameterExpression) each).getField().getQualifiedName());
//                                }
//                            }
//                        };
//                        MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(instance.getClass());
//                        iterator.iterateOn(wrappedValueHolder.getQuery().getSelectionCriteria());
//                        Object id = wrappedValueHolder.getRow().get(fieldName.get());
//                        // Since UUID is stored as String in some cases
//                        if (idProperty.getJavaType() == UUID.class && id instanceof String) {
//                            id = UUID.fromString((String) id);
//                        }
//                        vh = new JmixWrappingValueHolder(
//                                instance,
//                                property.getName(),
//                                property.getJavaType(),
//                                id,
//                                dataManager,
//                                metadata,
//                                metadataTools);
                    } else {
                        MetaProperty inverseProperty = property.getInverse();
//                        vh = new JmixSingleValueHolder(
//                                instance,
//                                property.getName(),
//                                inverseProperty.getName(),
//                                property.getJavaType(),
//                                dataManager,
//                                beanFactory.getBean(FetchPlanBuilder.class, instance.getClass()),
//                                metadata,
//                                metadataTools);
                    }
//                    vh.setPreservedLoadContext(softDeletion, hints, constraints);
//                    declaredField.set(instance, vh);
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
//                    if (fieldInstance instanceof JmixAbstractValueHolder) {
//                        declaredField.setAccessible(accessible);
//                        return;
//                    }
//                    UnitOfWorkQueryValueHolder originalValueHolder = (UnitOfWorkQueryValueHolder) fieldInstance;
//                    if (originalValueHolder.getWrappedValueHolder().isInstantiated()
//                            || !(originalValueHolder.getWrappedValueHolder() instanceof QueryBasedValueHolder)) {
//                        declaredField.setAccessible(accessible);
//                        return;
//                    }
//                    QueryBasedValueHolder wrappedValueHolder = (QueryBasedValueHolder) originalValueHolder.getWrappedValueHolder();
//                    AtomicReference<String> fieldName = new AtomicReference<>();
//                    ExpressionIterator iterator = new ExpressionIterator() {
//                        @Override
//                        public void iterate(Expression each) {
//                            if (each instanceof ParameterExpression) {
//                                fieldName.set(((ParameterExpression) each).getField().getQualifiedName());
//                            }
//                        }
//                    };
//                    MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(instance.getClass());
//                    iterator.iterateOn(wrappedValueHolder.getQuery().getSelectionCriteria());
//                    Object id = wrappedValueHolder.getRow().get(fieldName.get());
//                    // Since UUID is stored as String in some cases
//                    if (idProperty.getJavaType() == UUID.class && id instanceof String) {
//                        id = UUID.fromString((String) id);
//                    }
//                    vh = new JmixWrappingValueHolder(
//                            instance,
//                            property.getName(),
//                            property.getJavaType(),
//                            id,
//                            dataManager,
//                            metadata,
//                            metadataTools);
//                    vh.setPreservedLoadContext(softDeletion, hints, constraints);
//                    declaredField.set(instance, vh);
                    declaredField.setAccessible(accessible);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                }
                break;
            case ONE_TO_MANY:
            case MANY_TO_MANY:
                Object fieldValue = EntityValues.getValue(instance, property.getName());
                if (fieldValue == null) {
                    return;
                }
//                vh = new JmixCollectionValueHolder(
//                        property.getName(),
//                        instance,
//                        dataManager,
//                        beanFactory.getBean(FetchPlanBuilder.class, instance.getClass()),
//                        metadata,
//                        metadataTools);
//                vh.setPreservedLoadContext(softDeletion, hints, constraints);
//                fieldValue.setValueHolder(vh);
                break;
            default:
                break;
        }
    }

    protected void collectFetchPlans(Object instance, FetchPlan fetchPlan, Map<Object, Set<FetchPlan>> collectedFetchPlans) {
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
                    || !metadataTools.isPersistent(metaProperty)) {
                continue;
            }

            if (metadataTools.isEmbedded(metaProperty) &&
                    !entityStates.isLoaded(instance, property.getName())) {
                continue;
            }

            Object value = EntityValues.getValue(instance, property.getName());
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    for (Object item : new ArrayList(((Collection) value))) {
                        if (item instanceof Entity) {
                            collectFetchPlans(item, propertyFetchPlan, collectedFetchPlans);
                        }
                    }
                } else if (value instanceof Entity) {
                    collectFetchPlans(value, propertyFetchPlan, collectedFetchPlans);
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
