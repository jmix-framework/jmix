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
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.google.common.collect.Sets;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.condition.VisibleItems;
import io.jmix.masquerade.condition.VisibleItemsContains;
import io.jmix.masquerade.condition.VisibleItemsCount;
import io.jmix.masquerade.sys.ByLocator;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

/**
 * Abstract class for overlays. Supports scrolling, {@link VisibleItems}, {@link VisibleItemsContains},
 * {@link VisibleItemsCount} condition checking.
 *
 * @param <T> inheritor overlay class type
 * @param <P> parent web-element wrapper class type
 */
public abstract class AbstractOverlay<T extends AbstractOverlay<T, P>, P extends AbstractComponent<P>>
        extends AbstractSpecificConditionHandler<T> implements ByLocator {

    protected By by;
    protected SelenideElement wrappedElement;
    protected P parentComponent;

    protected AbstractOverlay(By by, P parentComponent) {
        this.by = by;
        this.wrappedElement = $(by);
        this.parentComponent = parentComponent;
    }

    @Override
    public By getBy() {
        return by;
    }

    /**
     * Scrolls the current overlay up to one page.
     *
     * @return {@code this} to call fluent API
     */
    public T scrollUp() {
        getVisibleElements().first()
                .scrollIntoView(false);

        //noinspection unchecked
        return ((T) this);
    }

    /**
     * Scrolls the current overlay down to one page.
     *
     * @return {@code this} to call fluent API
     */
    public T scrollDown() {
        getVisibleElements().last()
                .scrollIntoView(true);

        //noinspection unchecked
        return ((T) this);
    }

    /**
     * @return parent component web-element wrapper
     */
    public P getParentComponent() {
        return parentComponent;
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof VisibleItems visibleItems) {
            List<String> currentVisibleItems = getVisibleItems();
            return new CheckResult(
                    CollectionUtils.isEqualCollection(currentVisibleItems, visibleItems.getValue()),
                    currentVisibleItems
            );
        } else if (condition instanceof VisibleItemsCount visibleItemsCount) {
            List<String> currentVisibleItems = getVisibleItems();
            return new CheckResult(
                    currentVisibleItems.size() == visibleItemsCount.getValue(),
                    currentVisibleItems
            );
        } else if (condition instanceof VisibleItemsContains visibleItemsContains) {
            List<String> currentVisibleItems = getVisibleItems();
            return new CheckResult(
                    Sets.newHashSet(currentVisibleItems).containsAll(visibleItemsContains.getValue()),
                    currentVisibleItems
            );
        }

        return super.check(condition);
    }

    protected List<String> getVisibleItems() {
        return getVisibleElements()
                .texts();
    }

    @Override
    public SelenideElement getDelegate() {
        return wrappedElement;
    }

    /**
     * @return {@link ElementsCollection} of the visible overlay elements
     */
    public abstract ElementsCollection getVisibleElements();
}
