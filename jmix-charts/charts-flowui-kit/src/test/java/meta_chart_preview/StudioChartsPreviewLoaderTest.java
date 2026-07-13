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

package meta_chart_preview;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.vaadin.flow.component.Component;
import io.jmix.chartsflowui.kit.component.ChartRenderer;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.meta.loader.StudioChartsPreviewLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioChartsPreviewLoaderTest {

    static final Namespace CHARTS_NS = Namespace.get("http://jmix.io/schema/charts/ui");
    static final Namespace OTHER_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioChartsPreviewLoader loader = new StudioChartsPreviewLoader();

    /** Fake env backed by a message map, per the dropdown/grid loader tests' FakeEnv pattern. */
    static class FakeEnv implements StudioPreviewEnvironment {
        final Map<String, String> messages = new HashMap<>();

        @Override
        public String resolveMessage(String messageKey) {
            return messages.get(messageKey);
        }

        @Override
        public String propertyCaption(String dataContainerId, String metaClass, String propertyPath) {
            return null;
        }
    }

    BaseElement chartElement(String... nameValuePairs) {
        BaseElement element = new BaseElement("chart", CHARTS_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    BaseElement titleElement(String... nameValuePairs) {
        BaseElement element = new BaseElement("title", CHARTS_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    @Test
    void testIsSupportedForChartsChartElement() {
        assertTrue(loader.isSupported(chartElement()));
    }

    @Test
    void testIsSupportedFalseForWrongNamespace() {
        assertFalse(loader.isSupported(new BaseElement("chart", OTHER_NS)));
    }

    @Test
    void testIsSupportedFalseForWrongElementName() {
        assertFalse(loader.isSupported(new BaseElement("dataSet", CHARTS_NS)));
    }

    @Test
    void testLoadReturnsJmixChart() {
        Component component = loader.load(chartElement(), chartElement());
        assertInstanceOf(JmixChart.class, component);
    }

    @Test
    void testBaseAttributesApplied() {
        Element element = chartElement("width", "100%", "visible", "false", "classNames", "foo bar");
        JmixChart chart = (JmixChart) loader.load(element, element);

        assertEquals("100%", chart.getWidth());
        assertFalse(chart.isVisible());
        assertTrue(chart.getClassNames().contains("foo"));
        assertTrue(chart.getClassNames().contains("bar"));
    }

    @Test
    void testAnimationAttributeApplied() {
        Element element = chartElement("animation", "false");
        JmixChart chart = (JmixChart) loader.load(element, element);

        assertEquals(Boolean.FALSE, chart.getAnimation());
    }

    @Test
    void testRendererAttributeApplied() {
        Element element = chartElement("renderer", "SVG");
        JmixChart chart = (JmixChart) loader.load(element, element);

        assertEquals(ChartRenderer.SVG, chart.getRenderer());
    }

    @Test
    void testTitleRawText() {
        Element element = chartElement();
        element.add(titleElement("text", "Sales", "subtext", "2024"));

        JmixChart chart = (JmixChart) loader.load(element, element);

        assertNotNull(chart.getTitle());
        assertEquals("Sales", chart.getTitle().getText());
        assertEquals("2024", chart.getTitle().getSubtext());
    }

    @Test
    void testTitleMessageReferenceResolvedWithEnv() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://report.title", "Sales Report");

        Element element = chartElement();
        element.add(titleElement("text", "msg://report.title"));

        JmixChart chart = (JmixChart) loader.load(element, element, env);

        assertEquals("Sales Report", chart.getTitle().getText());
    }

    @Test
    void testTitleMessageReferenceFallsBackToRawKeyWithNoopEnv() {
        Element element = chartElement();
        element.add(titleElement("text", "msg://unresolved.title"));

        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        JmixChart chart = (JmixChart) loader.load(element, element);

        assertEquals("msg://unresolved.title", chart.getTitle().getText());
    }

    @Test
    void testNoTitleElementLeavesTitleNull() {
        JmixChart chart = (JmixChart) loader.load(chartElement(), chartElement());

        assertNull(chart.getTitle());
    }

    @Test
    void testOwnedAspectsIsEmpty() {
        assertEquals(Set.of(), loader.ownedAspects(chartElement()));
    }

    @Test
    void testDiscoverableViaServiceLoader() {
        var loaded = ServiceLoader.load(StudioPreviewComponentLoader.class).stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertTrue(loaded.contains(StudioChartsPreviewLoader.class));
    }
}
