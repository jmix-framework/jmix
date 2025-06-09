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

package io.jmix.reportsflowui.test_support;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.WindowBuilder;
import io.jmix.flowui.view.builder.WindowBuilderProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/*
 * Overridden bean needed to track view opened as dialog window.
 * Remove and refactor usages if https://github.com/jmix-framework/jmix/issues/4513 is ever done.
 */
public class TestWindowBuilderProcessor extends WindowBuilderProcessor implements OpenedDialogViewsTracker {

    private DialogWindow<?> lastOpenedDialogWindow;
    private View<?> lastOpenedView;

    public TestWindowBuilderProcessor(ApplicationContext applicationContext, Views views, ViewRegistry viewRegistry,
                                      UiAccessChecker uiAccessChecker) {
        super(applicationContext, views, viewRegistry, uiAccessChecker);
    }

    @Override
    public <V extends View<?>> DialogWindow<V> build(WindowBuilder<V> builder) {
        DialogWindow<V> result = super.build(builder);
        AtomicReference<Registration> registration = new AtomicReference<>();

        registration.set(result.addAfterOpenListener((event) -> {
            registerOpenedDialog(event.getSource(), event.getView());

            Registration reg = registration.get();
            reg.remove();
        }));

        return result;
    }

    private <V extends View<?>> void registerOpenedDialog(DialogWindow<V> dialogWindow, V view) {
        this.lastOpenedDialogWindow = dialogWindow;
        this.lastOpenedView = view;
    }

    @Override
    @Nullable
    public DialogWindow<?> getLastOpenedDialogWindow() {
        return lastOpenedDialogWindow;
    }

    @Override
    @Nullable
    public View<?> getLastOpenedView() {
        return lastOpenedView;
    }
}
