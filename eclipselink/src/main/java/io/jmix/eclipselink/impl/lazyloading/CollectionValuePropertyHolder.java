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

import io.jmix.core.EntityAttributeVisitor;
import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.util.*;

import static io.jmix.core.entity.EntitySystemAccess.getSecurityState;

public class CollectionValuePropertyHolder extends AbstractValueHolder {
    private static final long serialVersionUID = -8280038568067316785L;

    private Object rootEntity;

    public CollectionValuePropertyHolder(BeanFactory beanFactory,
                                         ValueHolderInterface originalValueHolder,
                                         Object owner,
                                         MetaProperty metaProperty) {
        super(beanFactory, originalValueHolder, owner, metaProperty);
    }

    public void setRootEntity(Object rootEntity) {
        this.rootEntity = rootEntity;
    }

    public Object getRootEntity() {
        return rootEntity;
    }

    @Override
    protected Object loadValue() {
        MetaClass metaClass = getMetadata().getClass(getOwner());

        LoadContext<?> loadContext = createLoadContextByOwner(metaClass);

        Object reloadedOwner = getDataManager().load(loadContext);
        Collection<Object> value = EntityValues.getValue(reloadedOwner, getPropertyInfo().getName());

        getSecurityState(getOwner()).addErasedIds(getPropertyInfo().getName(),
                getSecurityState(reloadedOwner).getErasedIds(getPropertyInfo().getName()));

        if (getRootEntity() != null) {
            replaceCollectionExistingReferences(value, getRootEntity());
        }

        return value;
    }

    @Override
    protected void afterLoadValue(Object value) {
        //noinspection unchecked
        for (Object entity : (Collection<Object>) value) {
            getMetadataTools().traverseAttributes(entity, new CollectionValuePropertyVisitor());
        }
    }

    protected LoadContext<?> createLoadContextByOwner(MetaClass metaClass) {
        return new LoadContext<>(metaClass)
                .setId(Objects.requireNonNull(EntityValues.getId(getOwner())))
                .setFetchPlan(
                        getFetchPlans().builder(metaClass.getJavaClass())
                                .add(getPropertyInfo().getName(), builder -> builder.addFetchPlan(FetchPlan.BASE))
                                .build())
                .setAccessConstraints(getLoadOptions().getAccessConstraints())
                .setHints(getLoadOptions().getHints());
    }

    protected void replaceCollectionExistingReferences(Collection<Object> collection, Object entityToReplace) {
        if (collection instanceof List) {
            for (ListIterator<Object> iterator = ((List<Object>) collection).listIterator(); iterator.hasNext(); ) {
                Object entity = iterator.next();
                if (Objects.equals(rootEntity, entity) && entityToReplace != entity) {
                    iterator.remove();
                    iterator.add(entityToReplace);
                }
            }
        } else if (collection instanceof Set) {
            Set<Object> set = (Set<Object>) collection;
            for (Object entity : set) {
                if (Objects.equals(entityToReplace, entity) && entityToReplace != entity) {
                    set.remove(entity);
                    set.add(entityToReplace);
                    break;
                }
            }
        } else {
            List<Object> toRemove = new ArrayList<>();
            for (Object entity : collection) {
                if (Objects.equals(entityToReplace, entity) && entityToReplace != entity) {
                    toRemove.add(entity);
                }
            }
            if (!toRemove.isEmpty()) {
                collection.removeAll(toRemove);
                collection.addAll(Collections.nCopies(toRemove.size(), entityToReplace));
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        beanFactory = SerializationContext.getThreadLocalBeanFactory();
    }

    protected class CollectionValuePropertyVisitor implements EntityAttributeVisitor {

        @Override
        public void visit(Object entity, MetaProperty property) {
            MetadataTools metadataTools = getMetadataTools();
            if (metadataTools.isJpa(property) && !metadataTools.isEmbedded(property)) {
                if (property.getRange().asClass().getJavaClass().isAssignableFrom(getOwner().getClass())) {
                    if (!Objects.equals(getRootEntity(), entity)) {
                        replaceToExistingReferences(entity, property, getOwner());
                    }
                }
            }
        }

        @Override
        public boolean skip(MetaProperty property) {
            return !property.getRange().isClass();
        }
    }
}
