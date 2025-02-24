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

package io.jmix.tabbedmode.builder;

import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.tabbedmode.Views;
import org.springframework.stereotype.Component;

@Component("tabmod_ViewBuilderProcessor")
public class ViewBuilderProcessor extends AbstractViewBuilderProcessor {

    public ViewBuilderProcessor(Views views,
                                ViewRegistry viewRegistry,
                                UiAccessChecker uiAccessChecker) {
        super(views, viewRegistry, uiAccessChecker);
    }

    public <V extends View<?>> V build(ViewBuilder<V> builder) {
        V view = createView(builder);
        initView(builder, view);

        return view;
    }

    @Override
    protected <V extends View<?>> Class<V> inferViewClass(AbstractViewBuilder<V, ?> builder) {
        throw new IllegalStateException("Can't open a view. " +
                "Either view id or view class must be defined");
    }
}
