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

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;

public class JmixSingleValueHolder extends JmixAbstractValueHolder {
    protected Object entityId;
    protected String propertyName;
    protected Class valueClass;

    protected transient DataManager dataManager;
    protected transient Metadata metadata;
    protected transient MetadataTools metadataTools;

    public JmixSingleValueHolder(String propertyName, Class valueClass, Object entityId, DataManager dataManager,
                                 Metadata metadata, MetadataTools metadataTools) {
        this.propertyName = propertyName;
        this.valueClass = valueClass;
        this.entityId = entityId;
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
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
                lc.getQuery().setParameter("entityId", entityId);
                value = dataManager.load(lc);
                isInstantiated = true;
            }
        }
        return value;
    }

    @Override
    public Object clone() {
        return new JmixSingleValueHolder(propertyName, valueClass, entityId, dataManager,
                metadata, metadataTools);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BeanFactory beanFactory = SerializationContext.getThreadLocalBeanFactory();
        dataManager = (DataManager) beanFactory.getBean(DataManager.NAME);
        metadata = (Metadata) beanFactory.getBean(Metadata.NAME);
        metadataTools = (MetadataTools) beanFactory.getBean(MetadataTools.NAME);
    }
}
