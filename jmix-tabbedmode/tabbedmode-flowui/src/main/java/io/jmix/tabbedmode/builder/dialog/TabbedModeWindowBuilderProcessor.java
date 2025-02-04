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

package io.jmix.tabbedmode.builder.dialog;

import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.DialogWindowBuilder;
import io.jmix.flowui.view.builder.WindowBuilder;
import io.jmix.flowui.view.builder.WindowBuilderProcessor;
import io.jmix.tabbedmode.builder.ViewBuilder;
import io.jmix.tabbedmode.builder.ViewBuilderAdapter;
import io.jmix.tabbedmode.builder.ViewBuilderProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedWindowBuilderProcessor")
public class TabbedModeWindowBuilderProcessor extends WindowBuilderProcessor {

    protected final ViewBuilderProcessor viewBuilderProcessor;

    public TabbedModeWindowBuilderProcessor(ApplicationContext applicationContext,
                                            Views views,
                                            ViewRegistry viewRegistry,
                                            UiAccessChecker uiAccessChecker,
                                            ViewBuilderProcessor viewBuilderProcessor) {
        super(applicationContext, views, viewRegistry, uiAccessChecker);

        this.viewBuilderProcessor = viewBuilderProcessor;
    }

    @Override
    public <V extends View<?>> DialogWindow<V> build(WindowBuilder<V> builder) {
        ViewBuilder<V> viewBuilder = new ViewBuilderAdapter<>(builder,
                getViewClass(builder),
                viewBuilderProcessor::build, __ -> {});
        V view = viewBuilder.build();

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <V extends View<?>> void initDialog(DialogWindowBuilder<V> builder, DialogWindow<V> dialog) {
        builder.getAfterOpenListener().ifPresent(dialog::addAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(dialog::addAfterCloseListener);
    }
}
