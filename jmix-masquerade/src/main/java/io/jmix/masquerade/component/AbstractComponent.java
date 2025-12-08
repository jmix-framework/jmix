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
import io.jmix.masquerade.sys.ByLocator;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

/**
 * Abstract class for web-element wrappers.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractComponent<T> extends AbstractSpecificConditionHandler<T> implements ByLocator {

    protected By by;
    protected SelenideElement wrappedElement;

    protected AbstractComponent(By by) {
        this.by = by;
        this.wrappedElement = $(by);
    }

    @Override
    public By getBy() {
        return by;
    }

    @Override
    public SelenideElement getDelegate() {
        return wrappedElement;
    }
}
