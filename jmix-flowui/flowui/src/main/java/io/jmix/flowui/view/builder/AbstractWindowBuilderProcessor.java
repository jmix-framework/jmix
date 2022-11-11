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

import io.jmix.flowui.Views;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.BeanUtil;
import org.springframework.context.ApplicationContext;

public abstract class AbstractWindowBuilderProcessor {

    protected ApplicationContext applicationContext;

    protected Views views;
    protected ViewRegistry viewRegistry;

    public AbstractWindowBuilderProcessor(ApplicationContext applicationContext,
                                          Views views,
                                          ViewRegistry viewRegistry) {
        this.applicationContext = applicationContext;
        this.views = views;
        this.viewRegistry = viewRegistry;
    }

    protected <V extends View<?>> DialogWindow<V> createDialog(V view) {
        DialogWindow<V> dialogWindow = new DialogWindow<>(view);
        BeanUtil.autowireContext(applicationContext, dialogWindow);

        return dialogWindow;
    }

    protected <V extends View<?>> V createView(DialogWindowBuilder<V> builder) {
        Class<V> viewClass = getViewClass(builder);
        return views.create(viewClass);
    }

    @SuppressWarnings("unchecked")
    protected <V extends View<?>> Class<V> getViewClass(DialogWindowBuilder<V> builder) {
        if (builder.getViewId().isPresent()) {
            String viewId = builder.getViewId().get();
            return (Class<V>) viewRegistry.getViewInfo(viewId).getControllerClass();
        } else if (builder instanceof DialogWindowClassBuilder
                && ((DialogWindowClassBuilder<?>) builder).getViewClass().isPresent()) {
            return ((DialogWindowClassBuilder<V>) builder).getViewClass().get();
        } else {
            return inferViewClass(builder);
        }
    }

    protected abstract <V extends View<?>> Class<V> inferViewClass(DialogWindowBuilder<V> builder);

    protected <V extends View<?>> void initDialog(DialogWindowBuilder<V> builder, DialogWindow<V> dialog) {
        builder.getAfterOpenListener().ifPresent(dialog::addAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(dialog::addAfterCloseListener);
    }
}
