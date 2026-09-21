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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import io.jmix.flowui.kit.xml.layout.support.LoaderUtils;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * A renderer for creating popover-enabled components in a user interface.
 * This class extends {@link ComponentRenderer} and uses provided functions
 * to generate components with optional popover tooltips containing additional information.
 *
 * @param <ITEM> the type of the items being rendered
 */
public class PopoverRenderer<ITEM> extends ComponentRenderer<Component, ITEM> {

    private final Function<ITEM, String> popoverTextProvider;

    protected PopoverPosition position = PopoverPosition.BOTTOM_START;
    protected String classNames;
    protected String css;

    public PopoverRenderer(Function<ITEM, String> popoverTextProvider) {
        this(popoverTextProvider, item -> {
            String value = popoverTextProvider.apply(item);

            Div button = new Div(value);
            button.setClassName("jmix-popover-renderer-button");

            return button;
        });
    }

    public PopoverRenderer(Function<ITEM, String> popoverTextProvider,
                           SerializableFunction<ITEM, Component> componentFunction) {
        super(componentFunction);

        this.popoverTextProvider = popoverTextProvider;
    }

    @Nullable
    @Override
    public Component createComponent(ITEM item) {
        String text = popoverTextProvider.apply(item);
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        Component target = super.createComponent(item);
        applyStyles(target);
        createPopover(text, target);

        return target;
    }

    protected void applyStyles(Component target) {
        applyClassNames(target);

        if (css != null) {
            LoaderUtils.applyCss(css, target.getStyle()::set);
        }
    }

    protected void applyClassNames(HasStyle component) {
        if (classNames != null) {
            LoaderUtils.split(classNames, component::addClassName);
        }
    }

    private void createPopover(String text, Component target) {
        Div popoverContent = new Div(text);
        popoverContent.setClassName("jmix-popover-renderer-content");

        Popover popover = new Popover(popoverContent);
        popover.setPosition(position);
        popover.setClassName("jmix-popover-renderer-popover");
        // The popover is not nested in the rendered component, so it gets the configured class names too:
        // they are the only way to reach it from CSS.
        applyClassNames(popover);
        popover.setTarget(target);
    }

    /**
     * Sets the popover position in relation to the rendered component.
     *
     * @param position popover position
     * @return this renderer
     */
    public PopoverRenderer<ITEM> withPosition(PopoverPosition position) {
        this.position = position;
        return this;
    }

    /**
     * Sets class names for the rendered component and for its popover.
     *
     * @param classNames space- or comma-separated class names, or {@code null} to clear them
     * @return this renderer
     */
    public PopoverRenderer<ITEM> withClassNames(@Nullable String classNames) {
        this.classNames = classNames;
        return this;
    }

    /**
     * Sets inline CSS declarations for the rendered component. They are not applied to the popover.
     *
     * @param css CSS declarations separated by semicolons, or {@code null} to clear them
     * @return this renderer
     */
    public PopoverRenderer<ITEM> withCss(@Nullable String css) {
        this.css = css;
        return this;
    }
}
