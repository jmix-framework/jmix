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

package io.jmix.ui.component.filter.converter;

import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.entity.FilterCondition;

import javax.annotation.Nullable;

public abstract class AbstractFilterComponentConverter<C extends FilterComponent, M extends FilterCondition>
        implements FilterConverter<C, M> {

    protected final Filter filter;

    public AbstractFilterComponentConverter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public C convertToComponent(M model) {
        C filterComponent = createComponent();
        filterComponent.setConditionModificationDelegated(true);
        filterComponent.setDataLoader(filter.getDataLoader());
        filterComponent.setAutoApply(filter.isAutoApply());

        filterComponent.setVisible(model.getVisible());
        filterComponent.setEnabled(model.getEnabled());

        filterComponent.setId(model.getComponentId());
        filterComponent.setStyleName(model.getStyleName());

        return filterComponent;
    }

    @Override
    public M convertToModel(C component) {
        M model = createModel();
        model.setVisible(component.isVisible());
        model.setEnabled(component.isEnabled());

        model.setComponentId(component.getId());
        model.setStyleName(component.getStyleName());

        return model;
    }

    protected abstract C createComponent();

    protected abstract M createModel();

    @Nullable
    protected abstract String getLocalizedModelCaption(C component);
}
