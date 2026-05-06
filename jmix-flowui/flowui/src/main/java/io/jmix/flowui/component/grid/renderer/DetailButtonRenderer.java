/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.grid.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class DetailButtonRenderer<E> extends AbstractDetailRenderer<JmixButton, E, DetailButtonRenderer<E>> {

    protected UiComponents uiComponents;
    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;

    protected ListDataComponent<E> ownerComponent;

    protected SerializableFunction<E, Component> iconProvider;

    protected Consumer<E> clickHandler;

    protected String themeNames;
    protected OpenMode openMode = OpenMode.NAVIGATION;

    public DetailButtonRenderer(UiComponents uiComponents, ViewNavigators viewNavigators, DialogWindows dialogWindows,
                                ListDataComponent<E> ownerComponent,
                                ValueProvider<E, String> textValueProvider) {
        this(uiComponents, viewNavigators, dialogWindows, ownerComponent, textValueProvider, null);
    }

    public DetailButtonRenderer(UiComponents uiComponents, ViewNavigators viewNavigators, DialogWindows dialogWindows,
                                ListDataComponent<E> ownerComponent,
                                ValueProvider<E, String> textValueProvider,
                                SerializableFunction<E, Component> iconProvider) {
        super(textValueProvider);

        this.uiComponents = uiComponents;
        this.viewNavigators = viewNavigators;
        this.dialogWindows = dialogWindows;
        this.ownerComponent = ownerComponent;
        this.iconProvider = iconProvider;

        initRenderer();
    }

    protected void initRenderer() {
        this.clickHandler = this::openDetailView;
    }

    @Override
    protected JmixButton createComponentInternal() {
        JmixButton button = uiComponents.create(JmixButton.class);

        if (themeNames != null) {
            split(themeNames).forEach(button::addThemeName);
        }

        return button;
    }

    @Override
    protected void configureComponent(JmixButton button, E item) {
        button.addClickListener(event -> clickHandler.accept(item));

        if (iconProvider != null) {
            button.setIcon(iconProvider.apply(item));
        }
    }

    protected void openDetailView(E item) {
        if (openMode == OpenMode.DIALOG) {
            openDialog(item);
        } else {
            navigate(item);
        }
    }

    protected void navigate(E item) {
        DetailViewNavigator<E> navigator = viewNavigators.detailView(ownerComponent)
                .editEntity(item)
                .withBackwardNavigation(true);

        Class<? extends View<?>> viewClass = this.viewClass;
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        } else if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        navigator.navigate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void openDialog(E item) {
        DetailWindowBuilder<E, View<?>> builder = dialogWindows.detail(ownerComponent)
                .editEntity(item);

        if (viewClass != null) {
            builder = builder.withViewClass((Class) viewClass);
        } else if (viewId != null) {
            builder = builder.withViewId(viewId);
        }

        builder.open();
    }

    public DetailButtonRenderer<E> withOpenMode(OpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    public DetailButtonRenderer<E> withIconProvider(@Nullable SerializableFunction<E, Component> iconProvider) {
        this.iconProvider = iconProvider;
        return this;
    }

    public DetailButtonRenderer<E> withThemeNames(@Nullable String themeNames) {
        this.themeNames = themeNames;
        return this;
    }

    public DetailButtonRenderer<E> withClickHandler(Consumer<E> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }
}
