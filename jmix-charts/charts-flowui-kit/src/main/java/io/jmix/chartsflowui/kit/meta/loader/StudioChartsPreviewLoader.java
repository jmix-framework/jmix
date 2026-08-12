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

package io.jmix.chartsflowui.kit.meta.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the {@code charts:chart} component.
 * <p>
 * A chart has no data at design time and ECharts draws nothing into an empty canvas, so a real
 * {@code JmixChart} previews as a blank area. An image stand-in is no good either: this module's
 * {@code META-INF/resources} is not on the dev server's static resource path, so its icon never loads.
 * The preview is therefore built from core components only - a bordered box carrying the declared title.
 */
public class StudioChartsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String CHARTS_SCHEMA = "http://jmix.io/schema/charts/ui";
    protected static final String CHART_ELEMENT = "chart";
    protected static final String TITLE_ELEMENT = "title";
    protected static final String DEFAULT_LABEL = "Chart";

    @Override
    public boolean isSupported(Element element) {
        return CHARTS_SCHEMA.equals(element.getNamespaceURI())
                && CHART_ELEMENT.equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        Div preview = new Div();

        loadComponentBaseAttributes(preview, componentElement);

        preview.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("border", "1px dashed var(--lumo-contrast-30pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("color", "var(--lumo-secondary-text-color)");
        preview.add(new Span(loadLabel(componentElement, environment)));

        return preview;
    }

    protected String loadLabel(Element componentElement, StudioPreviewEnvironment environment) {
        Element titleElement = componentElement.element(TITLE_ELEMENT);
        if (titleElement == null) {
            return DEFAULT_LABEL;
        }
        return loadString(titleElement, "text")
                .map(text -> PreviewActionSupport.resolveText(environment, text))
                .orElse(DEFAULT_LABEL);
    }
}
