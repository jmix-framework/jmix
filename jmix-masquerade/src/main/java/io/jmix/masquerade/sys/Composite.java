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

import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.AbstractSpecificConditionHandler;
import org.openqa.selenium.By;

/**
 * Abstract class for composite UI components: layouts, views, etc.
 *
 * @param <T> type of class
 */
public abstract class Composite<T> extends AbstractSpecificConditionHandler<T> implements ByLocator {

    @TestComponent
    protected SelenideElement wrappedElement;
    @TestComponent
    protected By by;

    @Override
    public SelenideElement getDelegate() {
        return wrappedElement;
    }

    @Override
    public By getBy() {
        return by;
    }
}
