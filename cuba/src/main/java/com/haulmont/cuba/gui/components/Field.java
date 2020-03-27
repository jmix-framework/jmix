/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.data.value.DatasourceValueSource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.value.ContainerValueSource;

import javax.annotation.Nullable;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.components.Field} instead
 */
@Deprecated
public interface Field<V> extends io.jmix.ui.components.Field<V>, DatasourceComponent<V> {

    /**
     * @return datasource instance
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    default Datasource getDatasource() {
        ValueSource<V> valueSource = getValueSource();
        return valueSource instanceof DatasourceValueSource ?
                ((DatasourceValueSource) valueSource).getDatasource() : null;
    }

    /**
     * @return datasource property
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    default MetaProperty getMetaProperty() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof DatasourceValueSource) {
            return ((DatasourceValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
        }
        if (valueSource instanceof ContainerValueSource) {
            return ((ContainerValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
        }
        return null;
    }

    /**
     * @return datasource property path
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    default MetaPropertyPath getMetaPropertyPath() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof DatasourceValueSource) {
            return ((DatasourceValueSource) valueSource).getMetaPropertyPath();
        }
        if (valueSource instanceof ContainerValueSource) {
            return ((ContainerValueSource) valueSource).getMetaPropertyPath();
        }
        return null;
    }

    /**
     * Set datasource and its property.
     *
     * @deprecated Use {@link #setValueSource(ValueSource)} instead
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default void setDatasource(@Nullable Datasource datasource, @Nullable String property) {
        if (datasource == null && property != null) {
            throw new IllegalArgumentException("datasource is null");
        }

        if (datasource != null && property != null) {
            this.setValueSource(new DatasourceValueSource(datasource, property));
        } else {
            this.setValueSource(null);
        }
    }
}
