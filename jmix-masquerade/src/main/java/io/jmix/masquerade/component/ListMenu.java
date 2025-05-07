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
import io.jmix.masquerade.JSelectors;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.Wait;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.Masquerade.$j;
import static io.jmix.masquerade.sys.TagNames.DETAILS_SUMMARY;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

/**
 * Web-element wrapper for list menu.
 */
public class ListMenu extends AbstractMenu<ListMenu> {

    protected static String DETAILS_TAG_NAME = "vaadin-details";

    public ListMenu(By by) {
        super(by);
    }

    @Override
    public void openItem(String... path) {
        for (String pathElement : path) {
            SelenideElement menuItemElement = $j(pathElement)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);

            if (DETAILS_TAG_NAME.equals(menuItemElement.getTagName())) {
                SelenideElement summaryElement =
                        $(byChained(
                                new JSelectors.ByUiTestId(pathElement),
                                DETAILS_SUMMARY
                        ));

                Wait().until(elementToBeClickable(summaryElement));

                if (summaryElement.getDomAttribute("opened") == null) {
                    summaryElement.click();
                }
            } else {
                menuItemElement.click();
            }
        }
    }
}
