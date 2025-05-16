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
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Web-element wrapper for checkbox. Supports changing check state.
 */
public class Checkbox extends AbstractCheckbox<Checkbox> {

    public Checkbox(By by) {
        super(by);
    }

    /**
     * Set the checked or unchecked state of a web-element.
     *
     * @param checked whether to set the checked state for the web-element
     * @return {@code this} to call fluent API
     */
    public Checkbox setChecked(boolean checked) {
        if (checked != isChecked()) {
            getInputDelegate().sendKeys(Keys.SPACE);
        }

        return this;
    }

    protected boolean isChecked() {
        return getDelegate().getDomAttribute("checked") != null;
    }

    protected SelenideElement getInputDelegate() {
        return $(byChained(by, TagNames.INPUT))
                .shouldBe(EXIST);
    }
}
