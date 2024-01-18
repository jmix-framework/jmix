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

import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.util.Objects;

import static io.jmix.core.entity.EntitySystemAccess.getSecurityState;

public class SingleValueMappedByPropertyHolder extends AbstractSingleValueHolder {
    private static final long serialVersionUID = -9161805285177725933L;

    public SingleValueMappedByPropertyHolder(BeanFactory beanFactory,
                                             ValueHolderInterface originalValueHolder,
                                             Object owner,
                                             MetaProperty metaProperty) {
        super(beanFactory, originalValueHolder, owner, metaProperty);
    }

    @Override
    protected Object loadValue() {
        if (!getLoadOptions().getAccessConstraints().isEmpty()) {
            MetaClass metaClass = getMetadata().getClass(getOwner());

            LoadContext<?> loadContext = createLoadContextByOwner(metaClass);

            Object reloadedOwner = getDataManager().load(loadContext);
            Object value = EntityValues.getValue(reloadedOwner, getPropertyInfo().getName());

            if (value == null) {
                getSecurityState(getOwner()).addErasedIds(getPropertyInfo().getName(),
                        getSecurityState(reloadedOwner).getErasedIds(getPropertyInfo().getName()));
            }

            return value;
        } else {
            MetaClass metaClass = getMetadata().getClass(getPropertyInfo().getJavaType());
            String primaryKeyName = getMetadataTools().getPrimaryKeyName(metaClass);

            LoadContext<?> loadContext = createLoadContextByInverseProperty(metaClass, primaryKeyName);

            return getDataManager().load(loadContext);
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
                .setHints(getLoadOptions().getHints())
                .setHint(PREV_SOFT_DELETION,
                        getLoadOptions().getHints().getOrDefault(PersistenceHints.SOFT_DELETION, SOFT_DELETION_ABSENT))
                .setHint(PersistenceHints.SOFT_DELETION, false);
    }

    protected LoadContext<?> createLoadContextByInverseProperty(MetaClass metaClass, String primaryKeyName) {
        LoadContext<?> loadContext = new LoadContext<>(metaClass)
                .setHints(getLoadOptions().getHints())
                .setHint(PREV_SOFT_DELETION,
                        getLoadOptions().getHints().getOrDefault(PersistenceHints.SOFT_DELETION, SOFT_DELETION_ABSENT))
                .setHint(PersistenceHints.SOFT_DELETION, false);
        loadContext.setQueryString(String.format("select e from %s e where e.%s.%s = :entityId", metaClass.getName(),
                getPropertyInfo().getInversePropertyName(), primaryKeyName))
                .setParameter("entityId", Objects.requireNonNull(EntityValues.getId(getOwner())));
        return loadContext;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        beanFactory = SerializationContext.getThreadLocalBeanFactory();
    }
}
