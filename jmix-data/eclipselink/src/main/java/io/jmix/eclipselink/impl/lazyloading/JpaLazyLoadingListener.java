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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.*;
import io.jmix.core.constraint.InMemoryConstraint;
import io.jmix.core.datastore.DataStoreAfterEntityLoadEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.PersistenceHints;
import io.jmix.eclipselink.EclipselinkProperties;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.expressions.ExpressionIterator;
import org.eclipse.persistence.internal.expressions.FieldExpression;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.expressions.RelationExpression;
import org.eclipse.persistence.internal.indirection.QueryBasedValueHolder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.eclipselink.impl.lazyloading.AbstractSingleValueHolder.PREV_SOFT_DELETION;
import static io.jmix.eclipselink.impl.lazyloading.AbstractSingleValueHolder.SOFT_DELETION_ABSENT;
import static io.jmix.eclipselink.impl.lazyloading.ValueHoldersSupport.*;

@Component("eclipselink_JpaLazyLoadingInterceptor")
public class JpaLazyLoadingListener implements DataStoreEventListener {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected BeanFactory beanFactory;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected EclipselinkProperties eclipselinkProperties;

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        FetchPlan fetchPlan = context.getFetchPlan();
        if (fetchPlan == null) {
            fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.LOCAL);
        }
        for (Object entity : event.getResultEntities()) {
            processValueHolders(entity, context, fetchPlan);
        }
    }

    public void processValueHolders(Object entity, LoadContext<?> loadContext, FetchPlan fetchPlan) {
        Map<Object, Set<FetchPlan>> collectedFetchPlans = new HashMap<>();

        if (fetchPlan != null) {
            collectFetchPlans(entity, fetchPlan, collectedFetchPlans);
        }

        Map<String, Object> hints = loadContext.getHints();
        Map<String, Serializable> serializableHints = new HashMap<>();

        for (Map.Entry<String, Object> entry : hints.entrySet()) {
            if (entry.getValue() instanceof Serializable) {
                serializableHints.put(entry.getKey(), (Serializable) entry.getValue());
            }
        }

        restoreSoftDeletion(serializableHints);

        LoadOptions loadOptions = LoadOptions.with()
                .setAccessConstraints(loadContext.getAccessConstraints().stream()
                        .filter(c -> c instanceof InMemoryConstraint)
                        .collect(Collectors.toList()))
                .setHints(serializableHints);

        for (Map.Entry<Object, Set<FetchPlan>> entry : collectedFetchPlans.entrySet()) {
            MetaClass metaClass = metadata.getClass(entry.getKey());
            for (MetaProperty property : metaClass.getProperties()) {
                if (property.getRange().isClass() && !metadataTools.isEmbedded(property) &&
                        !isPropertyContainedInFetchPlans(property, entry.getValue()) &&
                        metadataTools.getCrossDataStoreReferenceIdProperty(property.getStore().getName(), property) == null) {
                    if (!entityStates.isLoaded(entry.getKey(), property.getName())) {
                        if (property.getRange().getCardinality().isMany()) {
                            processCollectionValueHolder(entry.getKey(), property, loadOptions);
                        } else if (property.getRange().getCardinality() == Range.Cardinality.ONE_TO_ONE) {
                            processOneToOneValueHolder(entry.getKey(), property, loadOptions);
                        } else if (property.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                            processManyToOneValueHolder(entry.getKey(), property, loadOptions);
                        }
                    }
                }
            }
        }
    }

    /**
     * Restores SOFT_DELETION hint state if value has been forcefully set in single value property holder
     */
    protected void restoreSoftDeletion(Map<String, Serializable> hints) {
        if (hints.containsKey(PREV_SOFT_DELETION)) {
            if (hints.get(PREV_SOFT_DELETION).equals(SOFT_DELETION_ABSENT)) {
                hints.remove(PersistenceHints.SOFT_DELETION);
            } else {
                hints.put(PersistenceHints.SOFT_DELETION, hints.get(PREV_SOFT_DELETION));
            }
            hints.remove(PREV_SOFT_DELETION);
        }
    }

    protected void processCollectionValueHolder(Object owner, MetaProperty property, LoadOptions loadOptions) {
        Object valueHolder = getCollectionValueHolder(owner, property.getName());
        if (valueHolder != null && !(valueHolder instanceof AbstractValueHolder)) {

            AbstractValueHolder wrappedValueHolder;
            if (eclipselinkProperties.isDisableLazyLoading()) {
                wrappedValueHolder = new NonLoadingValueHolder(beanFactory, (ValueHolderInterface) valueHolder, owner, property);
            } else {
                wrappedValueHolder = new CollectionValuePropertyHolder(beanFactory, (ValueHolderInterface) valueHolder, owner, property);
                wrappedValueHolder.setLoadOptions(LoadOptions.with(loadOptions));
            }

            setCollectionValueHolder(owner, property.getName(), wrappedValueHolder);
        }
    }

    protected void processOneToOneValueHolder(Object owner, MetaProperty property, LoadOptions loadOptions) {
        Object originalValueHolder = getSingleValueHolder(owner, property.getName());

        if (originalValueHolder != null && !(originalValueHolder instanceof AbstractValueHolder)) {
            AbstractValueHolder wrappedValueHolder = null;

            if (eclipselinkProperties.isDisableLazyLoading()) {
                wrappedValueHolder = new NonLoadingValueHolder(beanFactory, (ValueHolderInterface) originalValueHolder, owner, property);
            } else if (metadataTools.isOwningSide(property)) {
                QueryBasedValueHolder queryBasedValueHolder = unwrapToQueryBasedValueHolder(originalValueHolder);
                if (queryBasedValueHolder != null) {
                    Object entityId;

                    MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(property.getRange().asClass());
                    if (pkProperty != null && metadataTools.isEmbedded(pkProperty)) {
                        entityId = buildEmbeddedIdByValueHolder(pkProperty, queryBasedValueHolder);
                    } else {
                        entityId = getEntityIdFromValueHolder(queryBasedValueHolder);
                    }

                    wrappedValueHolder =
                            new SingleValueOwningPropertyHolder(beanFactory, (ValueHolderInterface) originalValueHolder,
                                    owner, property, entityId);

                    wrappedValueHolder.setLoadOptions(LoadOptions.with(loadOptions));
                }
            } else {
                //noinspection ConstantConditions
                wrappedValueHolder = new SingleValueMappedByPropertyHolder(beanFactory, (ValueHolderInterface) originalValueHolder,
                        owner, property);

                wrappedValueHolder.setLoadOptions(LoadOptions.with(loadOptions));
            }

            setSingleValueHolder(owner, property.getName(), wrappedValueHolder);
        }
    }

    protected void processManyToOneValueHolder(Object owner, MetaProperty property, LoadOptions loadOptions) {
        Object originalValueHolder = getSingleValueHolder(owner, property.getName());

        if (originalValueHolder != null && !(originalValueHolder instanceof AbstractValueHolder)) {
            QueryBasedValueHolder queryBasedValueHolder = unwrapToQueryBasedValueHolder(originalValueHolder);
            if (queryBasedValueHolder != null) {
                AbstractValueHolder wrappedValueHolder;

                if (eclipselinkProperties.isDisableLazyLoading()) {
                    wrappedValueHolder = new NonLoadingValueHolder(beanFactory, (ValueHolderInterface) originalValueHolder, owner, property);
                } else {
                    Object entityId;
                    MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(property.getRange().asClass());
                    if (pkProperty != null && metadataTools.isEmbedded(pkProperty)) {
                        entityId = buildEmbeddedIdByValueHolder(pkProperty, queryBasedValueHolder);
                    } else {
                        entityId = getEntityIdFromValueHolder(queryBasedValueHolder);
                    }

                    wrappedValueHolder =
                            new SingleValueOwningPropertyHolder(beanFactory, (ValueHolderInterface) originalValueHolder,
                                    owner, property, entityId);

                    wrappedValueHolder.setLoadOptions(LoadOptions.with(loadOptions));
                }
                setSingleValueHolder(owner, property.getName(), wrappedValueHolder);
            }
        }
    }

    protected void collectFetchPlans(Object instance, FetchPlan fetchPlan, Map<Object, Set<FetchPlan>> collectedFetchPlans) {
        Set<FetchPlan> instanceFetchPlans = collectedFetchPlans.get(instance);
        if (instanceFetchPlans == null) {
            instanceFetchPlans = new HashSet<>();
            collectedFetchPlans.put(instance, instanceFetchPlans);
        } else if (instanceFetchPlans.contains(fetchPlan)) {
            return;
        }

        FetchPlanBuilder actualBuilder = fetchPlans.builder(fetchPlan.getEntityClass());

        MetaClass metaClass = metadata.getClass(instance);
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metaClass.getProperty(property.getName());
            if (!metaProperty.getRange().isClass() && !isLazyFetchedLocalAttribute(metaProperty)
                    || !metadataTools.isJpa(metaProperty)) {
                actualBuilder.mergeProperty(property.getName(), property.getFetchPlan(), property.getFetchMode());
                continue;
            }

            //may be actually not loaded in complicated graph with several references to the same entity with different fetchPlans
            if (!entityStates.isLoaded(instance, property.getName())) {
                //so do not process this property and do not add it to collectedFetchPlans in order to create lazy loading ValueHolder for it
                continue;
            }

            actualBuilder.mergeProperty(property.getName(), property.getFetchPlan(), property.getFetchMode());

            Object value = EntityValues.getValue(instance, property.getName());
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    //noinspection unchecked
                    for (Object item : new ArrayList<>((Collection<Object>) value)) {
                        if (item instanceof Entity) {
                            collectFetchPlans(item, propertyFetchPlan, collectedFetchPlans);
                        }
                    }
                } else if (value instanceof Entity) {
                    collectFetchPlans(value, propertyFetchPlan, collectedFetchPlans);
                }
            }
        }

        actualBuilder.partial(fetchPlan.loadPartialEntities());
        instanceFetchPlans.add(actualBuilder.build());
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

    /**
     *
     * @param pkProperty {@code @EmbeddedId} foreign key property
     * @param queryBasedValueHolder - original Eclipselink value holder
     * @return id of the entity to load
     *
     */
    protected Object buildEmbeddedIdByValueHolder(MetaProperty pkProperty, QueryBasedValueHolder queryBasedValueHolder) {
        MetaClass pkMetaclass = pkProperty.getRange().asClass();
        Object primaryKey = metadata.create(pkMetaclass);

        Map<String, String> fieldNamesByColumnNames = new HashMap<>();
        for (MetaProperty property : pkMetaclass.getProperties()) {
            String columnName = metadataTools.getDatabaseColumn(property);
            if (columnName != null) {
                fieldNamesByColumnNames.put(columnName, ((Field) property.getAnnotatedElement()).getName());
            }
        }

        ExpressionIterator iterator = new ExpressionIterator() {
            @Override
            public void iterate(Expression each) {
                if (each instanceof RelationExpression rel) {
                    FieldExpression fieldExpression;
                    ParameterExpression parameterExpression;
                    if (rel.getFirstChild() instanceof FieldExpression fe && rel.getSecondChild() instanceof ParameterExpression p) {
                        fieldExpression = fe;
                        parameterExpression = p;
                    } else if (rel.getSecondChild() instanceof FieldExpression fe && rel.getFirstChild() instanceof ParameterExpression p) {
                        fieldExpression = fe;
                        parameterExpression = p;
                    } else {
                        return;
                    }
                    EntityValues.setValue(primaryKey,
                            fieldNamesByColumnNames.get(fieldExpression.getField().getName()),
                            queryBasedValueHolder.getRow().get(parameterExpression.getField().getQualifiedName()));
                }
            }
        };

        iterator.iterateOn(queryBasedValueHolder.getQuery().getSelectionCriteria());

        return primaryKey;
    }
}
