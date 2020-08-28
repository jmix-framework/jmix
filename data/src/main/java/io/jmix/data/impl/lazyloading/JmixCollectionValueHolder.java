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
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.util.*;

public class JmixCollectionValueHolder extends JmixAbstractValueHolder {
    private static final long serialVersionUID = -8280038568067316785L;

    protected JmixEntity parentEntity;
    protected String propertyName;
    protected JmixEntity rootEntity;

    protected transient DataManager dataManager;
    protected transient FetchPlanBuilder fetchPlanBuilder;
    protected transient Metadata metadata;
    protected transient MetadataTools metadataTools;

    public JmixCollectionValueHolder(String propertyName, JmixEntity parentEntity, DataManager dataManager,
                                     FetchPlanBuilder fetchPlanBuilder, Metadata metadata, MetadataTools metadataTools) {
        this.propertyName = propertyName;
        this.parentEntity = parentEntity;
        this.dataManager = dataManager;
        this.fetchPlanBuilder = fetchPlanBuilder;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    public void setRootEntity(JmixEntity rootEntity) {
        this.rootEntity = rootEntity;
    }

    public JmixEntity getRootEntity() {
        return rootEntity;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                MetaClass metaClass = metadata.getClass(parentEntity.getClass());
                LoadContext lc = new LoadContext(metaClass);
                lc.setFetchPlan(fetchPlanBuilder.add(propertyName).build());
                lc.setId(parentEntity.__getEntityEntry().getEntityId());
                PreservedLoadContext plc = getPreservedLoadContext();
                lc.setSoftDeletion(plc.isSoftDeletion());
                lc.setHints(plc.getHints());
                if (plc.getAccessConstraints() != null && !plc.getAccessConstraints().isEmpty()) {
                    lc.setAccessConstraints(plc.getAccessConstraints());
                }
                JmixEntity result = dataManager.load(lc);
                this.value = ((IndirectCollection) result.__getEntityEntry().getAttributeValue(propertyName))
                        .getValueHolder()
                        .getValue();
                JmixEntity rootEntity = getRootEntity();
                if (rootEntity != null) {
                    if (value instanceof List) {
                        for (ListIterator iterator = ((List) value).listIterator(); iterator.hasNext(); ) {
                            JmixEntity entity = (JmixEntity) iterator.next();
                            if (rootEntity.equals(entity) && !(rootEntity == entity)) {
                                iterator.remove();
                                iterator.add(rootEntity);
                            }
                        }
                    } else if (value instanceof Set) {
                        Set setValue = (Set) value;
                        for (Iterator iterator = setValue.iterator(); iterator.hasNext(); ) {
                            JmixEntity entity = (JmixEntity) iterator.next();
                            if (rootEntity.equals(entity) && !(rootEntity == entity)) {
                                setValue.remove(entity);
                                setValue.add(rootEntity);
                                break;
                            }
                        }
                    } else if (value instanceof Collection) {
                        Collection collectionValue = (Collection) value;
                        List<JmixEntity> itemsToRemove = new ArrayList<>();
                        for (Iterator iterator = collectionValue.iterator(); iterator.hasNext(); ) {
                            JmixEntity entity = (JmixEntity) iterator.next();
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
                    public void visit(JmixEntity entity, MetaProperty property) {
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
                    metadataTools.traverseAttributes((JmixEntity) entity, av);
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
