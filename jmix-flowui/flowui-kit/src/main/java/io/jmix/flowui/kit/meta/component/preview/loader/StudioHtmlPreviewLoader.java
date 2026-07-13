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
            Map.entry("div", Div::new),
            Map.entry("span", Span::new),
            Map.entry("h1", H1::new),
            Map.entry("h2", H2::new),
            Map.entry("h3", H3::new),
            Map.entry("h4", H4::new),
            Map.entry("h5", H5::new),
            Map.entry("h6", H6::new),
            Map.entry("p", Paragraph::new),
            Map.entry("pre", Pre::new),
            Map.entry("code", Code::new),
            Map.entry("emphasis", Emphasis::new),
            Map.entry("hr", Hr::new),
            Map.entry("anchor", Anchor::new),
            Map.entry("iframe", IFrame::new),
            Map.entry("input", Input::new),
            Map.entry("rangeInput", RangeInput::new),
            Map.entry("listItem", ListItem::new),
            Map.entry("unorderedList", UnorderedList::new),
            Map.entry("orderedList", OrderedList::new),
            Map.entry("fieldSet", FieldSet::new),
            Map.entry("descriptionList", DescriptionList::new),
            Map.entry("term", DescriptionList.Term::new),
            Map.entry("description", DescriptionList.Description::new),
            Map.entry("section", Section::new),
            Map.entry("nav", Nav::new),
            Map.entry("main", Main::new),
            Map.entry("footer", Footer::new),
            Map.entry("aside", Aside::new),
            Map.entry("article", Article::new),
            Map.entry("header", Header::new),
            Map.entry("htmlObject", HtmlObject::new),
            Map.entry("param", Param::new),
            Map.entry("nativeLabel", NativeLabel::new),
            Map.entry("nativeButton", NativeButton::new),
            Map.entry("nativeDetails", NativeDetails::new)
    );

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && ("html".equals(element.getName()) || FACTORIES.containsKey(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        Component component = "html".equals(componentElement.getName())
                ? loadHtml(componentElement)
                : FACTORIES.get(componentElement.getName()).get();
        loadComponentBaseAttributes(component, componentElement);
        return component;
    }

    protected Component loadHtml(Element element) {
        String content = element.elements().stream()
                .filter(child -> "content".equals(child.getName()))
                .findFirst()
                .map(Element::getText)
                .or(() -> loadString(element, "content"))
                .orElse(DEFAULT_HTML_CONTENT);
        if (!content.trim().startsWith("<")) {
            content = DEFAULT_HTML_CONTENT;
        }
        return new Html(content.trim());
    }
}
