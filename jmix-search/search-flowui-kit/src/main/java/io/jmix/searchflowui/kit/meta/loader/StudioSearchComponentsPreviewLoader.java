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

package io.jmix.searchflowui.kit.meta.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the search add-on's {@code search:searchField} and
 * {@code search:fullTextFilter} components. Neither has a spring-free kit-level counterpart: the
 * runtime {@code SearchField} is {@code ApplicationContextAware} and the runtime
 * {@code FullTextFilter} extends {@code SingleFilterComponentBase}, both needing a live Spring
 * context that isn't available to a kit-only preview loader. This loader instead builds the plain
 * Vaadin component that {@link io.jmix.searchflowui.kit.meta.component.StudioSearchComponents}
 * already declares as the kit-meta return type for each: a {@link TextField} for
 * {@code searchField}, a {@link HorizontalLayout} for {@code fullTextFilter}.
 * <p>
 * {@code fullTextFilter}'s runtime shape (a label + text field inside its root
 * {@code HorizontalLayout}) has no equivalent slot on a bare {@link HorizontalLayout}: there's no
 * label component to bind {@code label} to. The preview approximates it with a {@link Span} for
 * the resolved {@code label} text, followed by a placeholder {@link TextField}, added as plain
 * children of the layout - close enough to show the visible shape, not a faithful reproduction.
 * {@code labelPosition}/{@code labelWidth} act on that missing label slot so they're skipped, along
 * with the data-bound {@code dataLoader}/{@code autoApply}/{@code searchStrategy}/
 * {@code parameterName} attributes that need a runtime data context.
 */
public class StudioSearchComponentsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String SEARCH_SCHEMA = "http://jmix.io/schema/search/ui";
    protected static final String SEARCH_FIELD_ELEMENT = "searchField";
    protected static final String FULL_TEXT_FILTER_ELEMENT = "fullTextFilter";

    @Override
    public boolean isSupported(Element element) {
        return SEARCH_SCHEMA.equals(element.getNamespaceURI())
                && (SEARCH_FIELD_ELEMENT.equals(element.getName())
                        || FULL_TEXT_FILTER_ELEMENT.equals(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        return FULL_TEXT_FILTER_ELEMENT.equals(componentElement.getName())
                ? loadFullTextFilter(componentElement, environment)
                : loadSearchField(componentElement, environment);
    }

    protected TextField loadSearchField(Element componentElement, StudioPreviewEnvironment environment) {
        TextField field = new TextField();

        // themeNames is applied generically by loadComponentBaseAttributes below, since
        // TextField implements HasTheme (via HasThemeVariant).
        loadComponentBaseAttributes(field, componentElement);

        loadString(componentElement, "value", field::setValue);
        loadBoolean(componentElement, "autofocus", field::setAutofocus);
        loadString(componentElement, "ariaLabel", field::setAriaLabel);
        loadString(componentElement, "title")
                .ifPresent(value -> field.setTitle(PreviewActionSupport.resolveText(environment, value)));
        loadString(componentElement, "placeholder")
                .ifPresent(value -> field.setPlaceholder(PreviewActionSupport.resolveText(environment, value)));
        loadString(componentElement, "label")
                .ifPresent(value -> field.setLabel(PreviewActionSupport.resolveText(environment, value)));
        loadString(componentElement, "helperText")
                .ifPresent(value -> field.setHelperText(PreviewActionSupport.resolveText(environment, value)));

        return field;
    }

    protected HorizontalLayout loadFullTextFilter(Element componentElement, StudioPreviewEnvironment environment) {
        HorizontalLayout root = new HorizontalLayout();

        // size (layout:hasSize) is applied generically by loadComponentBaseAttributes below,
        // since HorizontalLayout implements HasSize.
        loadComponentBaseAttributes(root, componentElement);

        loadString(componentElement, "label")
                .ifPresent(value -> root.add(new Span(PreviewActionSupport.resolveText(environment, value))));
        root.add(new TextField());

        return root;
    }
}
