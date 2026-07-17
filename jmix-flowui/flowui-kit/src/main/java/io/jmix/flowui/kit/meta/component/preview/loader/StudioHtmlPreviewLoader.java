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

package io.jmix.flowui.kit.meta.component.preview.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.*;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Studio preview loader for the HTML components of the flowui module.
 */
public class StudioHtmlPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DEFAULT_HTML_CONTENT = "<span></span>";

    protected static final Map<String, Supplier<Component>> FACTORIES = Map.ofEntries(
            Map.entry(StudioXmlElements.DIV, Div::new),
            Map.entry(StudioXmlElements.SPAN, Span::new),
            Map.entry(StudioXmlElements.H1, H1::new),
            Map.entry(StudioXmlElements.H2, H2::new),
            Map.entry(StudioXmlElements.H3, H3::new),
            Map.entry(StudioXmlElements.H4, H4::new),
            Map.entry(StudioXmlElements.H5, H5::new),
            Map.entry(StudioXmlElements.H6, H6::new),
            Map.entry(StudioXmlElements.P, Paragraph::new),
            Map.entry(StudioXmlElements.PRE, Pre::new),
            Map.entry(StudioXmlElements.CODE, Code::new),
            Map.entry(StudioXmlElements.EMPHASIS, Emphasis::new),
            Map.entry(StudioXmlElements.HR, Hr::new),
            Map.entry(StudioXmlElements.ANCHOR, Anchor::new),
            Map.entry(StudioXmlElements.IFRAME, IFrame::new),
            Map.entry(StudioXmlElements.INPUT, Input::new),
            Map.entry(StudioXmlElements.RANGE_INPUT, RangeInput::new),
            Map.entry(StudioXmlElements.LIST_ITEM, ListItem::new),
            Map.entry(StudioXmlElements.UNORDERED_LIST, UnorderedList::new),
            Map.entry(StudioXmlElements.ORDERED_LIST, OrderedList::new),
            Map.entry(StudioXmlElements.FIELD_SET, FieldSet::new),
            Map.entry(StudioXmlElements.DESCRIPTION_LIST, DescriptionList::new),
            Map.entry(StudioXmlElements.TERM, DescriptionList.Term::new),
            Map.entry(StudioXmlElements.DESCRIPTION, DescriptionList.Description::new),
            Map.entry(StudioXmlElements.SECTION, Section::new),
            Map.entry(StudioXmlElements.NAV, Nav::new),
            Map.entry(StudioXmlElements.MAIN, Main::new),
            Map.entry(StudioXmlElements.FOOTER, Footer::new),
            Map.entry(StudioXmlElements.ASIDE, Aside::new),
            Map.entry(StudioXmlElements.ARTICLE, Article::new),
            Map.entry(StudioXmlElements.HEADER, Header::new),
            Map.entry(StudioXmlElements.HTML_OBJECT, HtmlObject::new),
            Map.entry(StudioXmlElements.PARAM, Param::new),
            Map.entry(StudioXmlElements.NATIVE_LABEL, NativeLabel::new),
            Map.entry(StudioXmlElements.NATIVE_BUTTON, NativeButton::new),
            Map.entry(StudioXmlElements.NATIVE_DETAILS, NativeDetails::new)
    );

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (StudioXmlElements.HTML.equals(element.getName()) || FACTORIES.containsKey(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        Component component = StudioXmlElements.HTML.equals(componentElement.getName())
                ? loadHtml(componentElement)
                : FACTORIES.get(componentElement.getName()).get();
        if (component == null) {
            return null;
        }
        loadComponentBaseAttributes(component, componentElement);
        return component;
    }

    @Nullable
    protected Component loadHtml(Element element) {
        String content = element.elements().stream()
                .filter(child -> StudioXmlElements.CONTENT.equals(child.getName()))
                .findFirst()
                .map(Element::getText)
                .or(() -> loadString(element, "content"))
                .filter(c -> c.trim().startsWith("<"))
                .orElse(null);
        if (content != null) {
            return htmlOrDefault(content.trim());
        }
        // No usable inline content. A `file` attribute points at a project resource that a
        // spring-free kit loader can't read - decline so Studio's PSI-based fallback resolves it.
        if (loadString(element, "file").isPresent()) {
            return null;
        }
        return new Html(DEFAULT_HTML_CONTENT);
    }

    /**
     * {@link Html} requires a single root element; multi-root or unparseable markup throws.
     * Wrap-and-retry, then fall back to a blank span, so user markup never breaks the preview.
     */
    protected Component htmlOrDefault(String content) {
        try {
            return new Html(content);
        } catch (IllegalArgumentException multiRoot) {
            try {
                return new Html("<div>" + content + "</div>");
            } catch (IllegalArgumentException unparseable) {
                return new Html(DEFAULT_HTML_CONTENT);
            }
        }
    }
}
