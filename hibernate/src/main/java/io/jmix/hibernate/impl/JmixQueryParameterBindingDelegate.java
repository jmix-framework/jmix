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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.Ids;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.data.persistence.DbmsFeatures;
import io.jmix.data.persistence.DbmsSpecifics;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.type.Type;
import org.springframework.beans.factory.BeanFactory;

import javax.persistence.TemporalType;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class JmixQueryParameterBindingDelegate implements QueryParameterBinding {

    protected QueryParameterBinding binding;
    protected DbmsSpecifics dbmsSpecifics;
    protected boolean isNative;

    public JmixQueryParameterBindingDelegate(QueryParameterBinding binding, boolean isNative, BeanFactory beanFactory) {
        this.binding = binding;
        this.dbmsSpecifics = beanFactory.getBean(DbmsSpecifics.class);
        this.isNative = isNative;
    }

    @Override
    public boolean isBound() {
        return binding.isBound();
    }

    @Override
    public void setBindValue(Object value) {
        binding.setBindValue(convertParameterValue(value));
    }

    @Override
    public void setBindValue(Object value, Type clarifiedType) {
        binding.setBindValue(value, clarifiedType);
    }

    @Override
    public void setBindValue(Object value, TemporalType clarifiedTemporalType) {
        binding.setBindValue(value, clarifiedTemporalType);
    }

    @Override
    public Object getBindValue() {
        return binding.getBindValue();
    }

    @Override
    public Type getBindType() {
        return binding.getBindType();
    }

    private Object convertParameterValue(Object value) {
        DbmsFeatures dbmsFeatures = dbmsSpecifics.getDbmsFeatures();
        if (isNative && (value instanceof UUID) && (dbmsFeatures.getUuidTypeClassName() != null)) {
            Class<?> c = ReflectionHelper.getClass(dbmsFeatures.getUuidTypeClassName());
            try {
                value = ReflectionHelper.newInstance(c, value);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Error setting parameter value", e);
            }

        }
        if (value instanceof Id) {
            value = ((Id) value).getValue();

        } else if (value instanceof Ids) {
            value = ((Ids) value).getValues();

        } else if (value instanceof EnumClass) {
            value = ((EnumClass) value).getId();

        } else if (isCollectionOfEntitiesOrEnums(value)) {
            value = convertToCollectionOfIds(value);

        }
        return value;
    }

    private boolean isCollectionOfEntitiesOrEnums(Object value) {
        return value instanceof Collection
                && ((Collection<?>) value).stream().allMatch(it -> it instanceof Entity || it instanceof EnumClass);
    }

    private Object convertToCollectionOfIds(Object value) {
        return ((Collection<?>) value).stream()
                .map(it -> it instanceof Entity ? EntityValues.getId(((Entity) it)) : ((EnumClass) it).getId())
                .collect(Collectors.toList());
    }
}
