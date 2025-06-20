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

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

import static io.jmix.masquerade.JConditions.DISABLED;
import static io.jmix.masquerade.JConditions.ENABLED;

/**
 * Wrapper component for the {@link SelenideElement}. By default, delegates execution of standard element check methods
 * to the wrapped {@link SelenideElement}.
 *
 * @param <T> wrapper component type
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
public interface SelenideElementWrapper<T> {

    /**
     * @return original wrapped {@link SelenideElement}
     */
    SelenideElement getDelegate();

    /**
     * Checks if element exist true on the current page. Throws an exception if element is not found,
     * browser is closed or any {@link WebDriver} exception happened.
     *
     * @return {@code this} to call fluent API
     */
    default T exists() {
        getDelegate().shouldBe(Condition.exist);
        return (T) this;
    }

    /**
     * Checks if element is visible on the current page. Throws an exception if element is invisible, browser is closed
     * or any {@link WebDriver} exception happened.
     *
     * @return {@code this} to call fluent API
     */
    default T displayed() {
        getDelegate().shouldBe(Condition.visible);
        return ((T) this);
    }

    /**
     * Checks if element is disabled on the current page. Throws an exception if element is not disabled,
     * browser is closed or any {@link WebDriver} exception happened.
     *
     * @return {@code this} to call fluent API
     */
    default T disabled() {
        getDelegate().shouldHave(DISABLED);
        return (T) this;
    }

    /**
     * Checks if element is not disabled on the current page. Throws an exception if element is disabled,
     * browser is closed or any {@link WebDriver} exception happened.
     *
     * @return {@code this} to call fluent API
     */
    default T enabled() {
        getDelegate().shouldBe(ENABLED);
        return (T) this;
    }

    /**
     * Sequentially checks that given element meets all given conditions.
     * <p>
     * <b>NOTE:</b> If element doesn't match then conditions immediately, waits up to
     * 4 seconds until element meets the conditions. It's extremely useful for dynamic content.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     * @see #should(WebElementCondition, Duration)
     */
    default T should(WebElementCondition... condition) {
        getDelegate().should(condition);
        return (T) this;
    }

    /**
     * Wait until given element meets given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #should(WebElementCondition...)
     */
    default T should(WebElementCondition condition, Duration timeout) {
        getDelegate().should(condition, timeout);
        return (T) this;
    }

    /**
     * Same as {@link #should(WebElementCondition...)}. Should be used for better readability.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     */
    default T shouldHave(WebElementCondition... condition) {
        getDelegate().shouldHave(condition);
        return (T) this;
    }

    /**
     * Wait until given element meets given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #shouldHave(WebElementCondition...)
     */
    default T shouldHave(WebElementCondition condition, Duration timeout) {
        getDelegate().shouldHave(condition, timeout);
        return (T) this;
    }

    /**
     * Same as {@link #should(WebElementCondition...)}. Should be used for better readability.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     */
    default T shouldBe(WebElementCondition... condition) {
        getDelegate().shouldBe(condition);
        return (T) this;
    }

    /**
     * Wait until given element meets given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #shouldBe(WebElementCondition...)
     */
    default T shouldBe(WebElementCondition condition, Duration timeout) {
        getDelegate().shouldBe(condition, timeout);
        return (T) this;
    }

    /**
     * Sequentially checks that given element doesn't meet given conditions.
     * <p>
     * <b>NOTE:</b> If element doesn't match then conditions immediately, waits up to
     * 4 seconds until element meets the conditions. It's extremely useful for dynamic content.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     * @see #shouldNot(WebElementCondition, Duration)
     */
    default T shouldNot(WebElementCondition... condition) {
        getDelegate().shouldNot(condition);
        return (T) this;
    }

    /**
     * Wait until given element doesn't meet given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #shouldNot(WebElementCondition...)
     */
    default T shouldNot(WebElementCondition condition, Duration timeout) {
        getDelegate().shouldNot(condition, timeout);
        return (T) this;
    }

    /**
     * Same as {@link #shouldNot(WebElementCondition...)}. Should be used for better readability.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     */
    default T shouldNotHave(WebElementCondition... condition) {
        getDelegate().shouldNotHave(condition);
        return (T) this;
    }

    /**
     * Wait until given element doesn't meet given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #shouldNotHave(WebElementCondition...)
     */
    default T shouldNotHave(WebElementCondition condition, Duration timeout) {
        getDelegate().shouldNotHave(condition, timeout);
        return (T) this;
    }

    /**
     * Same as {@link #shouldNot(WebElementCondition...)}. Should be used for better readability.
     *
     * @param condition conditions for checks
     * @return {@code this} to call fluent API
     */
    default T shouldNotBe(WebElementCondition... condition) {
        getDelegate().shouldNotBe(condition);
        return (T) this;
    }

    /**
     * Wait until given element doesn't meet given condition (with given timeout).
     *
     * @param condition conditions for checks
     * @param timeout   timeout for checks
     * @return {@code this} to call fluent API
     * @see #shouldNotBe(WebElementCondition...)
     */
    default T shouldNotBe(WebElementCondition condition, Duration timeout) {
        getDelegate().shouldNotBe(condition, timeout);
        return (T) this;
    }

    /**
     * Checks whether the given element has an attribute with given name.
     *
     * @param name name of the attribute to check
     * @return {@code this} to call fluent API
     */
    default T shouldHaveAttribute(String name) {
        getDelegate().shouldHave(Condition.attribute(name));
        return (T) this;
    }

    /**
     * Checks whether the given element hasn't an attribute with given name.
     *
     * @param name name of the attribute to check
     * @return {@code this} to call fluent API
     */
    default T shouldNotHaveAttribute(String name) {
        getDelegate().shouldNotHave(Condition.attribute(name));
        return (T) this;
    }

    /**
     * Checks whether the given element has an attribute with given name and value.
     *
     * @param name                   name of the attribute to check
     * @param expectedAttributeValue value of the attribute to check
     * @return {@code this} to call fluent API
     */
    default T shouldHaveAttributeValue(String name, String expectedAttributeValue) {
        getDelegate().shouldHave(Condition.attribute(name, expectedAttributeValue));
        return (T) this;
    }

    /**
     * Checks whether the given element hasn't an attribute with given name and value.
     *
     * @param name                   name of the attribute to check
     * @param expectedAttributeValue value of the attribute to check
     * @return {@code this} to call fluent API
     */
    default T shouldNotHaveAttributeValue(String name, String expectedAttributeValue) {
        getDelegate().shouldNotHave(Condition.attribute(name, expectedAttributeValue));
        return (T) this;
    }

    /**
     * Checks that element has the given CSS class.
     *
     * @param cssClass CSS class name to check
     * @return {@code this} to  call fluent API
     */
    default T shouldHaveCss(String cssClass) {
        getDelegate().shouldHave(Condition.cssClass(cssClass));
        return (T) this;
    }

    /**
     * Checks that element hasn't the given CSS class.
     *
     * @param cssClass CSS class name to check
     * @return {@code this} to  call fluent API
     */
    default T shouldNotHaveCss(String cssClass) {
        getDelegate().shouldNotHave(Condition.cssClass(cssClass));
        return (T) this;
    }

    /**
     * Checks that element has the given CSS attribute with the given value.
     *
     * @param cssAttribute     CSS attribute name to check
     * @param expectedCssValue expected value of the CSS attribute
     * @return {@code this} to call fluent API
     */
    default T shouldHaveCssValue(String cssAttribute, String expectedCssValue) {
        getDelegate().shouldHave(Condition.cssValue(cssAttribute, expectedCssValue));
        return (T) this;
    }

    /**
     * Checks that element hasn't the given CSS attribute with the given value.
     *
     * @param cssAttribute     CSS attribute name to check
     * @param expectedCssValue expected value of the CSS attribute
     * @return {@code this} to call fluent API
     */
    default T shouldNotHaveCssValue(String cssAttribute, String expectedCssValue) {
        getDelegate().shouldNotHave(Condition.cssValue(cssAttribute, expectedCssValue));
        return (T) this;
    }
}
