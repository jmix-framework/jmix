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
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;

public class JmixSingleValueHolder extends JmixAbstractValueHolder {
    private static final long serialVersionUID = -9161805285177725933L;

    protected JmixEntity parentEntity;
    protected String propertyName;
    protected Class valueClass;

    protected transient DataManager dataManager;
    protected transient Metadata metadata;
    protected transient MetadataTools metadataTools;

    public JmixSingleValueHolder(String propertyName, Class valueClass, JmixEntity parentEntity, DataManager dataManager,
                                 Metadata metadata, MetadataTools metadataTools) {
        this.propertyName = propertyName;
        this.valueClass = valueClass;
        this.parentEntity = parentEntity;
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    public JmixEntity getParentEntity() {
        return parentEntity;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                MetaClass metaClass = metadata.getClass(valueClass);
                String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
                LoadContext lc = new LoadContext(metaClass);
                lc.setQueryString(String.format("select e from %s e where e.%s.%s = :entityId",
                        metaClass.getName(), propertyName, primaryKeyName));
                lc.getQuery().setParameter("entityId", parentEntity.__getEntityEntry().getEntityId());
                value = dataManager.load(lc);

                EntityAttributeVisitor av = new EntityAttributeVisitor() {
                    @Override
                    public void visit(JmixEntity entity, MetaProperty property) {
                        visitEntity(entity, property, parentEntity);
                    }

                    @Override
                    public boolean skip(MetaProperty property) {
                        return !(property.getRange().isClass()
                                && property.getRange().asClass().getJavaClass().isAssignableFrom(parentEntity.getClass()));
                    }
                };
                if (value != null) {
                    metadataTools.traverseAttributes((JmixEntity) value, av);
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
        metadata = (Metadata) beanFactory.getBean(Metadata.NAME);
        metadataTools = (MetadataTools) beanFactory.getBean(MetadataTools.NAME);
    }
}
