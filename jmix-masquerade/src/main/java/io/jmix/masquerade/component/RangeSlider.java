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

package io.jmix.masquerade.component;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import java.util.Objects;

import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Web-element wrapper for range slider. Also, can be used for wrapping range-slider-like web-elements:
 * {@code integerRangeSlider}, {@code decimalRangeSlider}. Supports setting start and end values and
 * {@link RangeValue} condition checking.
 * <p>
 * A range slider has two values, so the {@link Value} and {@link ValueContains} conditions are not supported:
 * use the {@link RangeValue} condition to check both range bounds.
 */
public class RangeSlider extends AbstractSlider<RangeSlider> {

    protected static final int START_INPUT_INDEX = 0;
    protected static final int END_INPUT_INDEX = 1;

    public RangeSlider(By by) {
        super(by);
    }

    @Override
    public SpecificCheck resolve(SpecificCondition condition) {
        if (condition instanceof RangeValue rangeValueCondition) {
            String startValue = getStartValue();
            String endValue = getEndValue();

            boolean matched = Objects.equals(startValue, Strings.nullToEmpty(rangeValueCondition.getStartValue()))
                    && Objects.equals(endValue, Strings.nullToEmpty(rangeValueCondition.getEndValue()));

            return SpecificCheck.of(matched, "[%s, %s]".formatted(startValue, endValue));

        } else if (condition instanceof Value || condition instanceof ValueContains) {
            throw new UnsupportedConditionException(condition, this);
        }

        return super.resolve(condition);
    }

    /**
     * Sets the start and end values to the range slider.
     * <p>
     * A range slider limits its start value by the current end value and vice versa, so the bounds are set in the
     * order that keeps the intermediate range valid.
     *
     * @param startValue start value as a string presentation value
     * @param endValue   end value as a string presentation value
     * @return {@code this} to call fluent API
     */
    public RangeSlider setValue(String startValue, String endValue) {
        if (isAboveCurrentEndValue(startValue)) {
            setEndValue(endValue);
            setStartValue(startValue);
        } else {
            setStartValue(startValue);
            setEndValue(endValue);
        }

        return this;
    }

    /**
     * Sets the start value to the range slider. The value is limited by the current end value.
     *
     * @param value start value as a string presentation value
     * @return {@code this} to call fluent API
     */
    public RangeSlider setStartValue(String value) {
        setValueInternal(getStartInputDelegate(), value);

        return this;
    }

    /**
     * Sets the end value to the range slider. The value is limited by the current start value.
     *
     * @param value end value as a string presentation value
     * @return {@code this} to call fluent API
     */
    public RangeSlider setEndValue(String value) {
        setValueInternal(getEndInputDelegate(), value);

        return this;
    }

    protected boolean isAboveCurrentEndValue(String startValue) {
        return Double.parseDouble(startValue) > Double.parseDouble(getEndValue());
    }

    protected String getStartValue() {
        return Strings.nullToEmpty(getStartInputDelegate().getValue());
    }

    protected String getEndValue() {
        return Strings.nullToEmpty(getEndInputDelegate().getValue());
    }

    /**
     * @return {@link SelenideElement} of a start value input web-element
     */
    protected SelenideElement getStartInputDelegate() {
        return getInputDelegate(START_INPUT_INDEX);
    }

    /**
     * @return {@link SelenideElement} of an end value input web-element
     */
    protected SelenideElement getEndInputDelegate() {
        return getInputDelegate(END_INPUT_INDEX);
    }

    protected SelenideElement getInputDelegate(int index) {
        return $$(byChained(by, TagNames.INPUT))
                .get(index)
                .shouldBe(VISIBLE);
    }
}
