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
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.options.DatasourceOptions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class ComponentGenerationContext extends io.jmix.ui.component.ComponentGenerationContext {

    @Deprecated
    protected Datasource datasource;
    @Deprecated
    protected CollectionDatasource optionsDatasource;
    @Deprecated
    protected Class<? extends Component> componentClass;

    /**
     * Creates an instance of ComponentGenerationContext.
     *
     * @param metaClass the entity for which the component is created
     * @param property  the entity attribute for which the component is created
     */
    public ComponentGenerationContext(MetaClass metaClass, String property) {
        super(metaClass, property);
    }

    /**
     * @return a datasource that can be used to create the component
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    public Datasource getDatasource() {
        return datasource;
    }

    /**
     * Sets a datasource, using fluent API method.
     *
     * @param datasource a datasource
     * @return this object
     * @deprecated Use {@link #setValueSource(ValueSource)} instead
     */

    @Deprecated
    public ComponentGenerationContext setDatasource(Datasource datasource) {
        this.datasource = datasource;
        return this;
    }

    /**
     * @return a datasource that can be used to show options
     * @deprecated Use {@link #getOptions()} instead
     */
    @Deprecated
    @Nullable
    public CollectionDatasource getOptionsDatasource() {
        return optionsDatasource;
    }

    /**
     * Sets a datasource that can be used to show options, using fluent API method.
     *
     * @param optionsDatasource a datasource that can be used as optional to create the component
     * @return this object
     * @deprecated Use {@link #setOptions(Options)} instead
     */
    @Deprecated
    public ComponentGenerationContext setOptionsDatasource(CollectionDatasource optionsDatasource) {
        this.optionsDatasource = optionsDatasource;
        return this;
    }

    /**
     * @return a component class for which a component is created
     */
    @Nullable
    @Deprecated
    public Class<? extends Component> getComponentClass() {
        return Component.class.isAssignableFrom(targetClass)
                ? (Class<? extends Component>) targetClass
                : null;
    }

    /**
     * Sets a component class for which a component is created, using fluent API method.
     *
     * @param componentClass a component class for which a component is created
     * @return this object
     */
    @Deprecated
    public ComponentGenerationContext setComponentClass(@Nullable Class<? extends Component> componentClass) {
        return (ComponentGenerationContext) setTargetClass(componentClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public ValueSource getValueSource() {
        return super.getValueSource() == null && getDatasource() != null
                ? new DatasourceValueSource(getDatasource(), getProperty())
                : super.getValueSource();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Options getOptions() {
        return super.getOptions() == null && getOptionsDatasource() != null
                ? new DatasourceOptions(getOptionsDatasource())
                : super.getOptions();
    }
}
