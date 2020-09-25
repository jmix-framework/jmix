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
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.util.*;

public class JmixCollectionValueHolder extends JmixAbstractValueHolder {
    private static final long serialVersionUID = -8280038568067316785L;

    protected Object parentEntity;
    protected String propertyName;
    protected Object rootEntity;

    protected transient DataManager dataManager;
    protected transient FetchPlanBuilder fetchPlanBuilder;
    protected transient Metadata metadata;
    protected transient MetadataTools metadataTools;

    public JmixCollectionValueHolder(String propertyName, Object parentEntity, DataManager dataManager,
                                     FetchPlanBuilder fetchPlanBuilder, Metadata metadata, MetadataTools metadataTools) {
        this.propertyName = propertyName;
        this.parentEntity = parentEntity;
        this.dataManager = dataManager;
        this.fetchPlanBuilder = fetchPlanBuilder;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    public void setRootEntity(Object rootEntity) {
        this.rootEntity = rootEntity;
    }

    public Object getRootEntity() {
        return rootEntity;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                MetaClass metaClass = metadata.getClass(parentEntity.getClass());
                LoadContext lc = new LoadContext(metaClass);
                lc.setFetchPlan(fetchPlanBuilder.add(propertyName).build());
                lc.setId(EntityValues.getId(parentEntity));
                PreservedLoadContext plc = getPreservedLoadContext();
                lc.setSoftDeletion(plc.isSoftDeletion());
                lc.setHints(plc.getHints());
                if (plc.getAccessConstraints() != null && !plc.getAccessConstraints().isEmpty()) {
                    lc.setAccessConstraints(plc.getAccessConstraints());
                }
                Entity result = (Entity) dataManager.load(lc);

                this.value = EntityValues.getValue(result, propertyName);
                SecurityState resultState = EntitySystemAccess.getSecurityState(result);
                SecurityState parentState = EntitySystemAccess.getSecurityState(parentEntity);
                Collection ids = resultState.getErasedIds(propertyName);
                if (ids != null && !ids.isEmpty()) {
                    parentState.addErasedIds(propertyName, ids);
                }
                Object rootEntity = getRootEntity();
                if (rootEntity != null) {
                    if (value instanceof List) {
                        for (ListIterator iterator = ((List) value).listIterator(); iterator.hasNext(); ) {
                            Entity entity = (Entity) iterator.next();
                            if (rootEntity.equals(entity) && !(rootEntity == entity)) {
                                iterator.remove();
                                iterator.add(rootEntity);
                            }
                        }
                    } else if (value instanceof Set) {
                        Set setValue = (Set) value;
                        for (Iterator iterator = setValue.iterator(); iterator.hasNext(); ) {
                            Entity entity = (Entity) iterator.next();
                            if (rootEntity.equals(entity) && !(rootEntity == entity)) {
                                setValue.remove(entity);
                                setValue.add(rootEntity);
                                break;
                            }
                        }
                    } else if (value instanceof Collection) {
                        Collection collectionValue = (Collection) value;
                        List<Entity> itemsToRemove = new ArrayList<>();
                        for (Iterator iterator = collectionValue.iterator(); iterator.hasNext(); ) {
                            Entity entity = (Entity) iterator.next();
                            if (rootEntity.equals(entity) && !(rootEntity == entity)) {
                                itemsToRemove.add(entity);
                            }
                        }
                        if (!itemsToRemove.isEmpty()) {
                            collectionValue.removeAll(itemsToRemove);
                            collectionValue.addAll(Collections.nCopies(itemsToRemove.size(), rootEntity));
                        }
                    }
                }

                EntityAttributeVisitor av = new EntityAttributeVisitor() {
                    @Override
                    public void visit(Object entity, MetaProperty property) {
                        if (rootEntity != null && rootEntity.equals(entity)) {
                            return;
                        }
                        visitEntity(entity, property, parentEntity);
                    }

                    @Override
                    public boolean skip(MetaProperty property) {
                        return !(property.getRange().isClass()
                                && property.getRange().asClass().getJavaClass().isAssignableFrom(parentEntity.getClass()));
                    }
                };

                for (Object entity : (Collection) value) {
                    metadataTools.traverseAttributes((Entity) entity, av);
                }
                isInstantiated = true;
            }
        }
        return value;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BeanFactory beanFactory = SerializationContext.getThreadLocalBeanFactory();
        dataManager = beanFactory.getBean(DataManager.class);
        fetchPlanBuilder = beanFactory.getBean(FetchPlanBuilder.class, parentEntity.getClass());
        metadata = beanFactory.getBean(Metadata.class);
        metadataTools = beanFactory.getBean(MetadataTools.class);
    }
}
