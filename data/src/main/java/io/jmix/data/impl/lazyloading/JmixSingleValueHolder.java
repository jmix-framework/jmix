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
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.lang.reflect.Field;

public class JmixSingleValueHolder extends JmixAbstractValueHolder {
    private static final long serialVersionUID = -9161805285177725933L;

    protected Object parentEntity;
    protected String propertyName;
    protected String inversePropertyName;
    protected Class valueClass;

    protected transient DataManager dataManager;
    protected transient FetchPlanBuilder fetchPlanBuilder;
    protected transient Metadata metadata;
    protected transient MetadataTools metadataTools;

    public JmixSingleValueHolder(Object parentEntity, String propertyName, String inversePropertyName,
                                 Class valueClass, DataManager dataManager, FetchPlanBuilder fetchPlanBuilder,
                                 Metadata metadata, MetadataTools metadataTools) {
        this.parentEntity = parentEntity;
        this.propertyName = propertyName;
        this.inversePropertyName = inversePropertyName;
        this.valueClass = valueClass;
        this.dataManager = dataManager;
        this.fetchPlanBuilder = fetchPlanBuilder;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    public Object getParentEntity() {
        return parentEntity;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                MetaClass metaClass;
                LoadContext lc;
                Object parentId = EntityValues.getId(parentEntity);
                PreservedLoadContext plc = getPreservedLoadContext();
                if (plc.getAccessConstraints() != null && !plc.getAccessConstraints().isEmpty()) {
                    metaClass = metadata.getClass(parentEntity);
                    lc = new LoadContext(metaClass);
                    lc.setFetchPlan(fetchPlanBuilder.add(propertyName).build());
                    lc.setId(parentId);
                    lc.setSoftDeletion(false);
                    lc.setHints(plc.getHints());
                    lc.setAccessConstraints(plc.getAccessConstraints());
                    Object result = dataManager.load(lc);
                    value = EntityValues.getValue(result, propertyName);
                    if (value == null) {
                        SecurityState resultState = EntitySystemAccess.getSecurityState(result);
                        SecurityState parentState = EntitySystemAccess.getSecurityState(parentEntity);
                        parentState.addErasedIds(propertyName, resultState.getErasedIds(propertyName));
                    }
                } else {
                    metaClass = metadata.getClass(valueClass);
                    String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
                    lc = new LoadContext(metaClass);
                    lc.setQueryString(String.format("select e from %s e where e.%s.%s = :entityId",
                            metaClass.getName(), inversePropertyName, primaryKeyName));
                    lc.getQuery().setParameter("entityId", parentId);
                    lc.setSoftDeletion(false);
                    lc.setHints(plc.getHints());
                    value = dataManager.load(lc);
                }

                EntityAttributeVisitor av = new EntityAttributeVisitor() {
                    @Override
                    public void visit(Object entity, MetaProperty property) {
                        if (property.getRange().asClass().getJavaClass().isAssignableFrom(parentEntity.getClass())) {
                            visitEntity(entity, property, parentEntity);
                        }
                        if (plc.isSoftDeletion()) {
                            switch (property.getRange().getCardinality()) {
                                case ONE_TO_ONE:
                                case MANY_TO_ONE:
                                    try {
                                        Field declaredField = entity.getClass().getDeclaredField("_persistence_" + property.getName() + "_vh");
                                        boolean accessible = declaredField.isAccessible();
                                        declaredField.setAccessible(true);
                                        Object fieldInstance = declaredField.get(entity);
                                        if (fieldInstance instanceof JmixAbstractValueHolder) {
                                            ((JmixAbstractValueHolder) fieldInstance).setPreservedLoadContext(
                                                    plc.isSoftDeletion(),
                                                    plc.getHints(),
                                                    plc.getAccessConstraints());
                                        }
                                        declaredField.setAccessible(accessible);
                                    } catch (NoSuchFieldException | IllegalAccessException e) {
                                    }
                                    break;
                                case ONE_TO_MANY:
                                case MANY_TO_MANY:
                                    IndirectCollection fieldValue = EntityValues.getValue(entity, property.getName());
                                    if (fieldValue != null && fieldValue.getValueHolder() instanceof JmixAbstractValueHolder) {
                                        JmixAbstractValueHolder vh = (JmixAbstractValueHolder) fieldValue.getValueHolder();
                                        vh.setPreservedLoadContext(
                                                plc.isSoftDeletion(),
                                                plc.getHints(),
                                                plc.getAccessConstraints());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    @Override
                    public boolean skip(MetaProperty property) {
                        return !(property.getRange().isClass());
                    }
                };
                if (value != null) {
                    metadataTools.traverseAttributes((Entity) value, av);
                }
                isInstantiated = true;
            }
        }
        return value;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BeanFactory beanFactory = SerializationContext.getThreadLocalBeanFactory();
        dataManager = (DataManager) beanFactory.getBean(DataManager.NAME);
        fetchPlanBuilder = beanFactory.getBean(FetchPlanBuilder.class, parentEntity.getClass());
        metadata = (Metadata) beanFactory.getBean(Metadata.NAME);
        metadataTools = (MetadataTools) beanFactory.getBean(MetadataTools.NAME);
    }
}
