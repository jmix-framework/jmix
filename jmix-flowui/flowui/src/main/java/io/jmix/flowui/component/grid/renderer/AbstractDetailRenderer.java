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
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Base class for component renderers that create controls for opening an entity detail view.
 *
 * @param <C> component type rendered in a grid cell
 * @param <E> entity item type
 * @param <R> renderer type used for fluent configuration methods
 */
public abstract class AbstractDetailRenderer<
        C extends Component & HasText & HasStyle,
        E,
        R extends AbstractDetailRenderer<C, E, R>>
        extends ComponentRenderer<C, E> {

    protected final ValueProvider<E, String> textValueProvider;

    protected String text;
    protected String classNames;
    protected String css;

    protected String viewId;
    protected Class<? extends View<?>> viewClass;

    /**
     * Creates a renderer with a value provider used as a fallback text source.
     *
     * @param textValueProvider value provider for cell text when explicit text is not configured
     */
    protected AbstractDetailRenderer(ValueProvider<E, String> textValueProvider) {
        // cause we are overriding createComponent method
        super((SerializableSupplier<C>) null);

        this.textValueProvider = textValueProvider;
    }

    @Override
    public C createComponent(E item) {
        C component = createComponentInternal();

        component.setText(getTextValue(item));

        applyClassNames(component);
        applyCss(component);

        configureComponent(component, item);
        return component;
    }

    protected abstract void configureComponent(C component, E item);

    protected abstract C createComponentInternal();

    protected String getTextValue(E item) {
        return text != null
                ? text
                : Objects.toString(textValueProvider.apply(item), "");
    }

    protected void applyClassNames(C component) {
        if (classNames != null) {
            split(classNames).forEach(component::addClassName);
        }
    }

    protected void applyCss(C component) {
        if (css != null) {
            applyCss(css, component.getStyle()::set);
        }
    }

    protected List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    protected void applyCss(String css, BiConsumer<String, String> setter) {
        Arrays.stream(StringUtils.split(css, ';'))
                .filter(StringUtils::isNotBlank)
                .forEach(propertyStatement -> {
                    int separatorIndex = propertyStatement.indexOf(':');
                    if (separatorIndex < 0) {
                        throw new IllegalArgumentException("Incorrect CSS string: " + css);
                    }

                    String propertyName = StringUtils.trimToEmpty(propertyStatement.substring(0, separatorIndex));
                    String propertyValue = StringUtils.trimToEmpty(propertyStatement.substring(separatorIndex + 1));

                    if (StringUtils.isBlank(propertyName)) {
                        throw new IllegalArgumentException("Incorrect CSS string, empty property name: " + css);
                    }

                    setter.accept(propertyName, propertyValue);
                });
    }

    /**
     * Sets explicit text for the rendered component.
     *
     * @param text text to show, or {@code null} to use the fallback item value
     * @return this renderer
     */
    public R withText(@Nullable String text) {
        this.text = text;
        return getThis();
    }

    /**
     * Sets class names for the rendered component.
     *
     * @param classNames space- or comma-separated class names, or {@code null} to clear them
     * @return this renderer
     */
    public R withClassNames(@Nullable String classNames) {
        this.classNames = classNames;
        return getThis();
    }

    /**
     * Sets inline CSS declarations for the rendered component.
     *
     * @param css CSS declarations separated by semicolons, or {@code null} to clear them
     * @return this renderer
     */
    public R withCss(@Nullable String css) {
        this.css = css;
        return getThis();
    }

    /**
     * Sets detail view id.
     *
     * @param viewId detail view id
     * @return this renderer
     */
    public R withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return getThis();
    }

    /**
     * Sets detail view class.
     *
     * @param viewClass detail view class
     * @return this renderer
     */
    public R withViewClass(@Nullable Class<? extends View<?>> viewClass) {
        this.viewClass = viewClass;
        return getThis();
    }

    @SuppressWarnings("unchecked")
    protected R getThis() {
        return (R) this;
    }
}
