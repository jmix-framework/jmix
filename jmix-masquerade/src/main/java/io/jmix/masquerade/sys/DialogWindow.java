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

package io.jmix.masquerade.sys;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.condition.DialogHeader;
import io.jmix.masquerade.condition.SpecificCondition;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.cssSelector;

/**
 * Abstract class for dialog windows.
 *
 * @param <T> type of dialog class
 */
public abstract class DialogWindow<T extends DialogWindow<T>> extends Composite<T> {

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof DialogHeader dialogHeader) {
            getHeaderElement().shouldHave(exactText(dialogHeader.getValue()));
            return CheckResult.accepted();
        }

        return super.check(condition);
    }

    /**
     * Closes this dialog by clicking the close button.
     */
    public void close() {
        getCloseButtonElement()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * @return {@link SelenideElement} of the dialog header element
     */
    protected SelenideElement getHeaderElement() {
        return $(byChained(getBy(), cssSelector("[slot='title']")));
    }

    /**
     * @return {@link SelenideElement} of the close dialog button
     */
    protected SelenideElement getCloseButtonElement() {
        return $(byChained(getBy(), cssSelector(".jmix-dialog-window-close-button")));
    }
}
