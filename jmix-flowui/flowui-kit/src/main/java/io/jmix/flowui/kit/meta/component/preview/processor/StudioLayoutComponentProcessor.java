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

package io.jmix.flowui.kit.meta.component.preview.processor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewChildProcessor;

/**
 * Studio preview processor for generic layout containers: {@link Scroller}, {@link SplitLayout},
 * {@link Accordion}, and any other {@link HasComponents}.
 */
public class StudioLayoutComponentProcessor implements StudioPreviewChildProcessor {

    @Override
    public boolean isSupported(Component parent) {
        return parent instanceof Scroller
                || parent instanceof SplitLayout
                || parent instanceof Accordion
                || parent instanceof HasComponents;
    }

    @Override
    public boolean addChild(Component parent, Component child, int index) {
        if (parent instanceof Scroller scroller) {
            scroller.setContent(child);
            return true;
        }
        if (parent instanceof SplitLayout splitLayout) {
            if (splitLayout.getPrimaryComponent() == null) {
                splitLayout.addToPrimary(child);
            } else {
                splitLayout.addToSecondary(child);
            }
            return true;
        }
        if (parent instanceof Accordion accordion) {
            // Accordion has no add(Component) or indexed add: real children are always
            // AccordionPanel instances (Jmix's <accordion> XML only nests <accordionPanel> tags),
            // and Accordion#add always appends.
            if (child instanceof AccordionPanel panel) {
                accordion.add(panel);
                return true;
            }
            return false;
        }
        if (parent instanceof HasComponents hasComponents) {
            if (index < 0) {
                hasComponents.add(child);
            } else {
                hasComponents.addComponentAtIndex(index, child);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChild(Component parent, Component child) {
        if (parent instanceof Scroller scroller) {
            scroller.setContent(null);
            return true;
        }
        if (parent instanceof SplitLayout splitLayout) {
            splitLayout.remove(child);
            return true;
        }
        if (parent instanceof Accordion accordion) {
            accordion.remove(child);
            return true;
        }
        if (parent instanceof HasComponents hasComponents) {
            hasComponents.remove(child);
            return true;
        }
        return false;
    }
}
