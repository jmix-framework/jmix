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
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.condition.SpecificConditionHandler;
import io.jmix.masquerade.condition.UnsupportedConditionException;
import io.jmix.masquerade.sys.SelenideElementWrapper;

import java.time.Duration;

import static io.jmix.masquerade.condition.SpecificConditionSupport.acceptFor;

/**
 * Abstract class for {@link SpecificConditionHandler}.
 *
 * @param <T> inheritor class type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSpecificConditionHandler<T>
        implements SelenideElementWrapper<T>, SpecificConditionHandler {

    @Override
    public CheckResult check(SpecificCondition condition) {
        throw new UnsupportedConditionException(condition, this);
    }

    @Override
    public T should(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.should(condition));
        return ((T) this);
    }

    @Override
    public T should(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.should(condition, timeout));
        return ((T) this);
    }

    @Override
    public T shouldHave(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldHave(condition));
        return ((T) this);
    }

    @Override
    public T shouldHave(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldHave(condition, timeout));
        return ((T) this);
    }

    @Override
    public T shouldBe(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldBe(condition));
        return ((T) this);
    }

    @Override
    public T shouldBe(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldBe(condition, timeout));
        return ((T) this);
    }

    @Override
    public T shouldNot(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNot(condition));
        return ((T) this);
    }

    @Override
    public T shouldNot(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNot(condition, timeout));
        return ((T) this);
    }

    @Override
    public T shouldNotHave(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNotHave(condition));
        return ((T) this);
    }

    @Override
    public T shouldNotHave(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNotHave(condition, timeout));
        return ((T) this);
    }

    @Override
    public T shouldNotBe(WebElementCondition... condition) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNotBe(condition));
        return ((T) this);
    }

    @Override
    public T shouldNotBe(WebElementCondition condition, Duration timeout) {
        acceptFor(this, () -> SelenideElementWrapper.super.shouldNotBe(condition, timeout));
        return ((T) this);
    }
}
