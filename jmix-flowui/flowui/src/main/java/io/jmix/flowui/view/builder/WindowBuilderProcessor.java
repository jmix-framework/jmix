/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view.builder;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Views;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_WindowBuilderProcessor")
public class WindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected ApplicationContext applicationContext;

    protected ViewRegistry viewRegistry;

    public WindowBuilderProcessor(ApplicationContext applicationContext,
                                  Views views,
                                  ViewRegistry viewRegistry) {
        super(applicationContext, views, viewRegistry);
    }

    public <V extends View<?>> DialogWindow<V> build(WindowBuilder<V> builder) {
        V view = createView(builder);

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <V extends View<?>> Class<V> inferViewClass(DialogWindowBuilder<V> builder) {
        throw new IllegalStateException("Can't open a view. " +
                "Either view id or view class must be defined");
    }
}
