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

/**
 * A renderer that creates a server-side {@link JmixButton} opening an entity detail view.
 * <p>
 * The detail view can be opened either by navigation or in a dialog depending on the configured {@link OpenMode}.
 *
 * @param <E> entity item type
 */
public class DetailButtonRenderer<E> extends AbstractDetailRenderer<JmixButton, E, DetailButtonRenderer<E>> {

    protected UiComponents uiComponents;
    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;

    protected ListDataComponent<E> ownerComponent;

    protected SerializableFunction<E, Component> iconProvider;

    protected Consumer<E> clickHandler;

    protected String themeNames;
    protected OpenMode openMode = OpenMode.NAVIGATION;

    /**
     * Creates a detail button renderer.
     *
     * @param uiComponents      factory for creating UI components
     * @param viewNavigators    view navigation entry point
     * @param dialogWindows     dialog windows entry point
     * @param ownerComponent    list data component that owns the rendered column
     * @param textValueProvider value provider for button text when explicit text is not configured
     */
    public DetailButtonRenderer(UiComponents uiComponents, ViewNavigators viewNavigators, DialogWindows dialogWindows,
                                ListDataComponent<E> ownerComponent,
                                ValueProvider<E, String> textValueProvider) {
        this(uiComponents, viewNavigators, dialogWindows, ownerComponent, textValueProvider, null);
    }

    /**
     * Creates a detail button renderer.
     *
     * @param uiComponents      factory for creating UI components
     * @param viewNavigators    view navigation entry point
     * @param dialogWindows     dialog windows entry point
     * @param ownerComponent    list data component that owns the rendered column
     * @param textValueProvider value provider for button text when explicit text is not configured
     * @param iconProvider      provider for button icons, or {@code null} if no icon should be set
     */
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

    /**
     * Sets how the detail view is opened.
     *
     * @param openMode open mode
     * @return this renderer
     */
    public DetailButtonRenderer<E> withOpenMode(OpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    /**
     * Sets a provider used to create button icons.
     *
     * @param iconProvider icon provider, or {@code null} to render buttons without icons
     * @return this renderer
     */
    public DetailButtonRenderer<E> withIconProvider(@Nullable SerializableFunction<E, Component> iconProvider) {
        this.iconProvider = iconProvider;
        return this;
    }

    /**
     * Sets theme names for the rendered button.
     *
     * @param themeNames space- or comma-separated theme names, or {@code null} to clear them
     * @return this renderer
     */
    public DetailButtonRenderer<E> withThemeNames(@Nullable String themeNames) {
        this.themeNames = themeNames;
        return this;
    }

    /**
     * Sets a custom click handler.
     *
     * @param clickHandler click handler invoked with the current grid item
     * @return this renderer
     */
    public DetailButtonRenderer<E> withClickHandler(Consumer<E> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }
}
