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
import io.jmix.chartsflowui.kit.component.ChartRenderer;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.ChartOptions;
import io.jmix.chartsflowui.kit.component.model.Title;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the {@code charts:chart} component: instantiates an empty
 * {@link JmixChart} with its pure-XML visual attributes and {@code <title>} applied, instead of
 * the reflection-fallback placeholder.
 * <p>
 * Data-driven or heavy sub-elements are intentionally skipped, so the preview is an honest empty
 * chart canvas rather than a guess at data that isn't available at design time:
 * {@code dataSet}/{@code series}/axes/{@code legend}/{@code toolbox} and the rest of the option
 * tree need the runtime data model or are heavy to reproduce here.
 * <p>
 * Studio has no chart-specific post-processing for any of the skipped sub-elements, so building
 * only the base attributes and title unconditionally (no {@link StudioPreviewEnvironment}
 * handshake gate, unlike {@code StudioGridPreviewLoader}'s columns) is safe: there's nothing for
 * Studio to double-add.
 */
public class StudioChartsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String CHARTS_SCHEMA = "http://jmix.io/schema/charts/ui";
    protected static final String CHART_ELEMENT = "chart";
    protected static final String TITLE_ELEMENT = "title";

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
        JmixChart chart = new JmixChart();

        loadComponentBaseAttributes(chart, componentElement);

        loadBoolean(componentElement, "animation", chart::setAnimation);
        loadInteger(componentElement, "animationThreshold", chart::setAnimationThreshold);
        loadInteger(componentElement, "animationDuration", chart::setAnimationDuration);
        loadString(componentElement, "animationEasing", chart::setAnimationEasing);
        loadInteger(componentElement, "animationDelay", chart::setAnimationDelay);
        loadInteger(componentElement, "animationDurationUpdate", chart::setAnimationDurationUpdate);
        loadString(componentElement, "animationEasingUpdate", chart::setAnimationEasingUpdate);
        loadInteger(componentElement, "animationDelayUpdate", chart::setAnimationDelayUpdate);
        loadEnum(componentElement, ChartOptions.BlendMode.class, "blendMode", chart::setBlendMode);
        loadInteger(componentElement, "hoverLayerThreshold", chart::setHoverLayerThreshold);
        loadEnum(componentElement, ChartRenderer.class, "renderer", chart::setRenderer);
        loadBoolean(componentElement, "useUtc", chart::setUseUtc);

        Element titleElement = componentElement.element(TITLE_ELEMENT);
        if (titleElement != null) {
            chart.setTitle(loadTitle(titleElement, environment));
        }

        return chart;
    }

    protected Title loadTitle(Element titleElement, StudioPreviewEnvironment environment) {
        Title title = new Title();

        loadString(titleElement, "text")
                .ifPresent(text -> title.setText(PreviewActionSupport.resolveText(environment, text)));
        loadString(titleElement, "subtext")
                .ifPresent(subtext -> title.setSubtext(PreviewActionSupport.resolveText(environment, subtext)));

        return title;
    }
}
