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
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.tabbedmode.Views;

public abstract class AbstractViewBuilderProcessor {

    protected Views views;
    protected ViewRegistry viewRegistry;
    protected UiAccessChecker uiAccessChecker;

    public AbstractViewBuilderProcessor(Views views,
                                        ViewRegistry viewRegistry,
                                        UiAccessChecker uiAccessChecker) {
        this.views = views;
        this.viewRegistry = viewRegistry;
        this.uiAccessChecker = uiAccessChecker;
    }

    protected <V extends View<?>> V createView(AbstractViewBuilder<V, ?> builder) {
        Class<V> viewClass = getViewClass(builder);
        uiAccessChecker.checkViewPermitted(viewClass);
        return views.create(viewClass);
    }

    protected <V extends View<?>> void initView(AbstractViewBuilder<V, ?> builder, V view) {
        builder.getReadyListener().ifPresent(listener ->
                ViewControllerUtils.addReadyListener(view, new ViewReadyListenerAdapter<>(listener)));
        builder.getAfterCloseListener().ifPresent(listener ->
                ViewControllerUtils.addAfterCloseListener(view, new ViewAfterCloseListenerAdapter<>(listener)));
        builder.getViewConfigurer().ifPresent(viewConfigurer -> viewConfigurer.accept(view));

    }

    @SuppressWarnings("unchecked")
    protected <V extends View<?>> Class<V> getViewClass(AbstractViewBuilder<V, ?> builder) {
        if (builder.getViewClass().isPresent()) {
            return builder.getViewClass().get();
        } else if (builder.getViewId().isPresent()) {
            String viewId = builder.getViewId().get();
            return (Class<V>) viewRegistry.getViewInfo(viewId).getControllerClass();
        } else {
            return inferViewClass(builder);
        }
    }

    protected abstract <V extends View<?>> Class<V> inferViewClass(AbstractViewBuilder<V, ?> builder);
}
