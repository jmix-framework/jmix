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

package io.jmix.searchflowui.view.result;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.UiComponents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SearchResultsHighlightHtmlReproTest {

    @Test
    void testHighlightStringIsSanitizedBeforeRendering() {
        SearchResultsView view = new SearchResultsView();
        view.uiComponents = new MinimalUiComponents();

        Div hitDiv = view.createHitDiv("Name", "hello <img src=\"x\" onerror=\"window.__jmixSearchXss=1\">");

        Assertions.assertEquals("Name : hello <img src=\"x\" onerror=\"window.__jmixSearchXss=1\">",
                hitDiv.getElement().getText());
        Assertions.assertTrue(hitDiv.getChildren().allMatch(Text.class::isInstance));
    }

    @Test
    void testHighlightBoldFragmentsArePreserved() {
        SearchResultsView view = new SearchResultsView();
        view.uiComponents = new MinimalUiComponents();

        Div hitDiv = view.createHitDiv("Name", "hello <b>world</b>");
        List<Text> textNodes = hitDiv.getChildren()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .toList();
        Span highlightSpan = hitDiv.getChildren()
                .filter(Span.class::isInstance)
                .map(Span.class::cast)
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(2, textNodes.size());
        Assertions.assertEquals("Name : ", textNodes.get(0).getText());
        Assertions.assertEquals("hello ", textNodes.get(1).getText());
        Assertions.assertEquals("world", highlightSpan.getElement().getText());
        Assertions.assertEquals("bold", highlightSpan.getElement().getStyle().get("font-weight"));
    }

    @Test
    void testDangerousHtmlInsideBoldHighlightIsRenderedAsBoldText() {
        SearchResultsView view = new SearchResultsView();
        view.uiComponents = new MinimalUiComponents();

        String payload = "<img src=\"x\" onerror=\"window.__jmixSearchXss=1\">";
        Div hitDiv = view.createHitDiv("Name", "hello <b>" + payload + "</b>");
        List<Text> textNodes = hitDiv.getChildren()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .toList();
        Span highlightSpan = hitDiv.getChildren()
                .filter(Span.class::isInstance)
                .map(Span.class::cast)
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(2, textNodes.size());
        Assertions.assertEquals("Name : ", textNodes.get(0).getText());
        Assertions.assertEquals("hello ", textNodes.get(1).getText());
        Assertions.assertEquals(payload, highlightSpan.getElement().getText());
        Assertions.assertEquals("bold", highlightSpan.getElement().getStyle().get("font-weight"));
        Assertions.assertTrue(highlightSpan.getChildren().findAny().isEmpty());
    }

    private static class MinimalUiComponents implements UiComponents {

        @Override
        public <T extends Component> T create(Class<T> type) {
            if (Div.class.equals(type)) {
                return type.cast(new Div());
            }
            throw new IllegalArgumentException("Unsupported component type: " + type.getName());
        }
    }
}
