/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.data.binding.TextComponentReadonlyDataBinding;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link HtmlContainerReadonlyDataBinding} that delegates to
 * {@link TextComponentReadonlyDataBinding}.
 *
 * @deprecated use {@link TextComponentReadonlyDataBindingImpl} instead
 */
@NullMarked
@Deprecated(since = "3.1", forRemoval = true)
@Component("flowui_HtmlContainerDataBinding")
public class HtmlContainerReadonlyDataBindingImpl implements HtmlContainerReadonlyDataBinding {

    protected TextComponentReadonlyDataBinding textComponentReadonlyDataBinding;

    public HtmlContainerReadonlyDataBindingImpl(TextComponentReadonlyDataBinding textComponentReadonlyDataBinding) {
        this.textComponentReadonlyDataBinding = textComponentReadonlyDataBinding;
    }

    @Override
    public Registration bind(HtmlContainer htmlContainer, ValueSource<?> valueSource) {
        return textComponentReadonlyDataBinding.bind(htmlContainer, valueSource);
    }

    @Override
    public Registration bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property) {
        return textComponentReadonlyDataBinding.bind(htmlContainer, dataContainer, property);
    }

    @Override
    public Registration bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer, String property) {
        return textComponentReadonlyDataBinding.bind(htmlContainer, dataContainer, property);
    }
}
