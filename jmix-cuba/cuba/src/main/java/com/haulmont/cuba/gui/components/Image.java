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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.components.data.value.DatasourceValueSource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;

import javax.annotation.Nullable;

/**
 * Component compatible with {@link Datasource}.
 *
 * @deprecated Use {@link io.jmix.ui.component.Image} instead
 */
@Deprecated
public interface Image extends DatasourceComponent<FileDescriptor>, io.jmix.ui.component.Image<FileDescriptor>,
        ResourceView {

    /**
     * Sets datasource and its property.
     *
     * @deprecated Use {@link #setValueSource(ValueSource)} instead.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default void setDatasource(Datasource datasource, String property) {
        if (datasource != null) {
            this.setValueSource(new DatasourceValueSource(datasource, property));
        } else {
            this.setValueSource(null);
        }
    }

    /**
     * @return datasource instance
     * @deprecated Use {@link #getValueSource()} instead.
     */
    @Deprecated
    default Datasource getDatasource() {
        ValueSource<FileDescriptor> valueSource = getValueSource();
        return valueSource instanceof DatasourceValueSource ?
                ((DatasourceValueSource) valueSource).getDatasource() : null;
    }

    /**
     * @return return null if value source is not EntityValueSource or value source is not defined
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Nullable
    @Deprecated
    default MetaPropertyPath getMetaPropertyPath() {
        if (getValueSource() == null) {
            return null;
        }

        ValueSource<FileDescriptor> valueSource = getValueSource();
        return valueSource instanceof EntityValueSource ?
                ((EntityValueSource) valueSource).getMetaPropertyPath() : null;
    }
}
