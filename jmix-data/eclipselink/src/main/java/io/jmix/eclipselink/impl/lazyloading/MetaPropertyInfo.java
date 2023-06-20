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

import io.jmix.core.metamodel.model.MetaProperty;

import java.io.Serializable;

public class MetaPropertyInfo implements Serializable {
    private static final long serialVersionUID = -6385834541263526148L;

    private final String name;
    private final Class<?> javaType;
    private final String inversePropertyName;

    public MetaPropertyInfo(MetaProperty metaProperty) {
        this.name = metaProperty.getName();
        this.javaType = metaProperty.getJavaType();
        MetaProperty inverseProperty = metaProperty.getInverse();
        if (inverseProperty != null) {
            this.inversePropertyName = inverseProperty.getName();
        } else {
            this.inversePropertyName = null;
        }
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public String getInversePropertyName() {
        return inversePropertyName;
    }
}
