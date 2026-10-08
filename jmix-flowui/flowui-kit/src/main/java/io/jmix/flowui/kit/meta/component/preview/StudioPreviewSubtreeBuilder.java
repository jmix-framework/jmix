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

package io.jmix.flowui.kit.meta.component.preview;

import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.orderedlayout.Scroller;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

/**
 * Builds a static component subtree from descriptor XML for cases where the whole tree is
 * rendered at once and never edited in place — e.g. the content of a {@code <fragment>} used
 * inside a view. Unlike the designer flow (where Studio creates children one by one and attaches
 * them itself), this walks the XML recursively through the registered SPI loaders.
 */
final class StudioPreviewSubtreeBuilder {

    /** Fragments may include fragments; a cycle would otherwise recurse forever. */
    private static final int MAX_FRAGMENT_DEPTH = 3;

    private static final ThreadLocal<Integer> FRAGMENT_DEPTH = ThreadLocal.withInitial(() -> 0);

    /** Non-component children that must not be walked as visual subtree. */
    private static final Set<String> NON_VISUAL_CHILD_NAMES = Set.of(
            "data", "facets", "actions", "tooltip", "prefix", "suffix", "validators", "formatter",
            StudioXmlElements.COLUMNS, StudioXmlElements.ITEMS, "responsiveSteps");

    private StudioPreviewSubtreeBuilder() {
    }

    /**
     * Renders the content of a fragment descriptor: builds the first component under
     * {@code <fragment><content>} with all its descendants. {@code null} when the descriptor
     * has no renderable content or the recursion limit is reached.
     */
    @Nullable
    static Component buildFragmentContent(String fragmentDescriptorXml, StudioPreviewEnvironment environment) {
        int depth = FRAGMENT_DEPTH.get();
        if (depth >= MAX_FRAGMENT_DEPTH) {
            return null;
        }
        FRAGMENT_DEPTH.set(depth + 1);
        try {
            Element fragmentRoot = StudioPreviewComponentProvider.parseXmlRoot(fragmentDescriptorXml);
            if (fragmentRoot == null || !StudioXmlElements.FRAGMENT.equals(fragmentRoot.getName())) {
                return null;
            }
            Element content = fragmentRoot.element(StudioXmlElements.CONTENT);
            if (content == null) {
                return null;
            }
            for (Element child : content.elements()) {
                Component component = buildSubtree(child, fragmentRoot, environment);
                if (component != null) {
                    return component;
                }
            }
            return null;
        } finally {
            FRAGMENT_DEPTH.set(depth);
        }
    }

    @Nullable
    private static Component buildSubtree(Element componentElement, Element viewElement,
                                          StudioPreviewEnvironment environment) {
        Component component =
                StudioPreviewComponentProvider.loadSingleComponent(componentElement, viewElement, environment);
        if (component == null) {
            return null;
        }
        for (Element child : componentElement.elements()) {
            if (NON_VISUAL_CHILD_NAMES.contains(child.getName())) {
                continue;
            }
            Component childComponent = buildSubtree(child, viewElement, environment);
            if (childComponent == null) {
                continue;
            }
            if (component instanceof HasComponents hasComponents) {
                hasComponents.add(childComponent);
            } else if (component instanceof Scroller scroller) {
                scroller.setContent(childComponent);
            }
            // other exotic containers are left childless: better a partial fragment than a crash
        }
        return component;
    }
}
