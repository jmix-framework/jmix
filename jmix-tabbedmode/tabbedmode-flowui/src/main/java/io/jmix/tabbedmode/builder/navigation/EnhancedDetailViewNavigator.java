/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder.navigation;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.DetailViewClassNavigator;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class EnhancedDetailViewNavigator<E> extends DetailViewNavigator<E> implements EnhancedViewNavigator<E> {

    protected ListDataComponent<E> listDataComponent;
    protected HasValue<?, E> field;

    public EnhancedDetailViewNavigator(DetailViewNavigator<E> viewNavigator) {
        super(viewNavigator);
    }

    @Override
    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    @Override
    public Optional<HasValue<?, ?>> getField() {
        return Optional.ofNullable(field);
    }

    public EnhancedDetailViewNavigator<E> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public EnhancedDetailViewNavigator<E> withField(@Nullable HasValue<?, E> field) {
        this.field = field;
        return this;
    }

    @Override
    public <V extends View<?>> DetailViewClassNavigator<E, V> withViewClass(Class<V> viewClass) {
        return new EnhancedDetailViewClassNavigator<>(this, viewClass);
    }
}
