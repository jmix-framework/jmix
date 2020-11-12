/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.widget.JmixFormLayout;
import io.jmix.ui.widget.JmixHorizontalActionsLayout;
import io.jmix.ui.widget.JmixVerticalActionsLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

import static io.jmix.ui.component.Component.AUTO_SIZE;

@Deprecated
public final class CubaComponentsHelper {

    private CubaComponentsHelper() {
    }

    public static String getFilterComponentPath(Filter filter) {
        StringBuilder sb = new StringBuilder(filter.getId() != null ? filter.getId() : "filterWithoutId");
        Frame frame = filter.getFrame();
        while (frame != null) {
            sb.insert(0, ".");
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            if (s.contains(".")) {
                s = "[" + s + "]";
            }
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }
        return sb.toString();
    }

    public static boolean convertFieldGroupCaptionAlignment(FieldGroup.FieldCaptionAlignment captionAlignment) {
        return captionAlignment == FieldGroup.FieldCaptionAlignment.LEFT;
    }

    @Nullable
    protected static Component findChildComponent(FieldGroup fieldGroup, com.vaadin.ui.Component target) {
        com.vaadin.ui.Component vaadinSource = fieldGroup.unwrap(JmixFormLayout.class);
        Collection<Component> components = fieldGroup.getFields().stream()
                .map(FieldGroup.FieldConfig::getComponentNN)
                .collect(Collectors.toList());

        return ComponentsHelper.findChildComponent(components, vaadinSource, target);
    }

    @Nullable
    protected static Component findChildComponent(Collection<Component> components,
                                                  com.vaadin.ui.Component vaadinSource,
                                                  com.vaadin.ui.Component target) {
        com.vaadin.ui.Component targetComponent = ComponentsHelper.getDirectChildComponent(target, vaadinSource);

        for (Component component : components) {
            com.vaadin.ui.Component unwrapped = component.unwrapComposition(com.vaadin.ui.Component.class);
            if (unwrapped == targetComponent) {
                Component child = null;

                if (component instanceof HasComponents) {
                    child = ComponentsHelper.findChildComponent((HasComponents) component, target);
                }


                if (component instanceof HasButtonsPanel) {
                    ButtonsPanel buttonsPanel = ((HasButtonsPanel) component).getButtonsPanel();
                    if (buttonsPanel != null) {
                        if (ComponentsHelper.getVaadinSource(buttonsPanel) == target) {
                            return buttonsPanel;
                        } else {
                            child = ComponentsHelper.findChildComponent(buttonsPanel, target);
                        }
                    }
                }

                if (component instanceof FieldGroup) {
                    FieldGroup fieldGroup = (FieldGroup) component;
                    child = ComponentsHelper.findChildComponent(fieldGroup, target);
                }

                return child != null ? child : component;
            }
        }
        return null;
    }

    public static void expand(AbstractOrderedLayout layout,
                              com.vaadin.ui.Component component, String height, String width) {
        if (!isHorizontalLayout(layout)
                && (StringUtils.isEmpty(height) || AUTO_SIZE.equals(height) || height.endsWith("%"))) {
            component.setHeight(100, Sizeable.Unit.PERCENTAGE);
        }

        if (!isVerticalLayout(layout)
                && (StringUtils.isEmpty(width) || AUTO_SIZE.equals(width) || width.endsWith("%"))) {
            component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        }

        layout.setExpandRatio(component, 1);
    }

    /**
     * Checks whether the given layout is horizontal.
     *
     * @param layout a layout to check
     * @return whether the layout is horizontal
     */
    public static boolean isHorizontalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof HorizontalLayout)
                || (layout instanceof JmixHorizontalActionsLayout);
    }

    /**
     * Checks whether the given layout is vertical.
     *
     * @param layout a layout to check
     * @return whether the layout is vertical
     */
    public static boolean isVerticalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof VerticalLayout)
                || (layout instanceof JmixVerticalActionsLayout);
    }

    /**
     * Get the topmost window for the specified component.
     *
     * @param component component instance
     * @return topmost client specific window in the hierarchy of frames for this component.
     *
     * <br>Can be null only if the component wasn't properly initialized.
     */
    @Nullable
    public static Window getWindowImplementation(Component.BelongToFrame component) {
        Frame frame = component.getFrame();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                Window window = (Window) frame;
                return window instanceof com.haulmont.cuba.gui.components.Window.Wrapper
                        ? ((com.haulmont.cuba.gui.components.Window.Wrapper) window).getWrappedWindow()
                        : window;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    /**
     * @deprecated Simply use {@link Frame#getFrameOwner()} call.
     */
    @Deprecated
    public static FrameOwner getFrameController(Frame frame) {
        return frame.getFrameOwner();
    }

    @Deprecated
    public static boolean hasFullWidth(Component c) {
        return (int) c.getWidth() == 100 && c.getWidthSizeUnit() == SizeUnit.PERCENTAGE;
    }

    @Deprecated
    public static boolean hasFullHeight(Component c) {
        return (int) c.getHeight() == 100 && c.getHeightSizeUnit() == SizeUnit.PERCENTAGE;
    }

    @Deprecated
    public static SizeUnit convertToSizeUnit(int unit) {
        switch (unit) {
            case com.haulmont.cuba.gui.components.Component.UNITS_PIXELS:
                return SizeUnit.PIXELS;
            case com.haulmont.cuba.gui.components.Component.UNITS_PERCENTAGE:
                return SizeUnit.PERCENTAGE;
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
    }

    @Deprecated
    public static int convertFromSizeUnit(SizeUnit unit) {
        switch (unit) {
            case PIXELS:
                return com.haulmont.cuba.gui.components.Component.UNITS_PIXELS;
            case PERCENTAGE:
                return com.haulmont.cuba.gui.components.Component.UNITS_PERCENTAGE;
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
    }
}
