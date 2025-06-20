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

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.condition.Value;
import io.jmix.masquerade.condition.ValueContains;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;

/**
 * Web-element wrapper for code editor. Supports setting value, {@link Value}, {@link ValueContains}
 * checking.
 */
public class CodeEditor extends AbstractTextInput<CodeEditor> {

    protected static final String TEXT_INPUT_CSS = ".ace_text-input";
    protected static final String TEXT_CONTENT_CSS = ".ace_content";

    public CodeEditor(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        SelenideElement content = getContentDelegate();
        if (condition instanceof Value valueCondition) {
            String expectedValue = Strings.nullToEmpty(valueCondition.getValue());

            content.shouldHave(Condition.exactText(expectedValue));
            return CheckResult.accepted();
        } else if (condition instanceof ValueContains valueContains) {
            String expectedValue = Strings.nullToEmpty(valueContains.getValue());

            content.shouldHave(Condition.text(expectedValue));
            return CheckResult.accepted();
        }

        return super.check(condition);
    }

    @Override
    public CodeEditor setValue(String value) {
        // clear previous value
        getInputDelegate().sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        return super.setValue(value);
    }

    protected SelenideElement getContentDelegate() {
        return $(shadowCss(TEXT_CONTENT_CSS, getHostCssSelector()))
                .shouldBe(VISIBLE);
    }

    @Override
    protected SelenideElement getInputDelegate() {
        return $(shadowCss(TEXT_INPUT_CSS, getHostCssSelector()))
                .shouldBe(EXIST);
    }

    protected String getHostCssSelector() {
        return "jmix-code-editor[%s='%s']".formatted(
                UI_TEST_ID,
                getDelegate().getDomAttribute(UI_TEST_ID));
    }
}
