/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.component;

import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.Masquerade;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for accordion. Supports working with accordion panels.
 */
public class Accordion extends AbstractComponent<Accordion>
        implements Container {

    public Accordion(By by) {
        super(by);
    }

    /**
     * @param panelUiTestId {@link Masquerade#UI_TEST_ID UI_TEST_ID} attribute value pf the accordion panel web-element
     * @return web-element wrapper for accordion panel found by the passed ID
     */
    public Panel getPanelById(String panelUiTestId) {
        return getPanelBy(byUiTestId(panelUiTestId));
    }

    /**
     * @param panelText accordion panel heading text
     * @return web-element wrapper for accordion panel found by the passed heading text
     */
    public Panel getPanelByText(String panelText) {
        return getPanelBy(getPanelByTextBy(panelText));
    }

    /**
     * @param by {@link By} selector to find accordion panel
     * @return web-element wrapper for accordion panel found by the passed {@link By} selector
     */
    public Panel getPanelBy(By by) {
        return new Panel(byChained(getBy(), by));
    }

    protected By getPanelByTextBy(String panelText) {
        return xpath("./vaadin-accordion-panel[vaadin-accordion-heading[span[text()='%s']]]"
                .formatted(panelText));
    }

    /**
     * Accordion panel web-element wrapper.
     */
    public static class Panel extends AbstractDetails<Panel> {

        protected Panel(By by) {
            super(by);
        }

        @Override
        public SelenideElement getSummaryElement() {
            return $(byChained(by, TagNames.ACCORDION_HEADING))
                    .shouldBe(VISIBLE);
        }
    }
}
