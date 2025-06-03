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

import io.jmix.masquerade.sys.ByLocator;
import io.jmix.masquerade.sys.Composite;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Interface for a web-element wrappers that have an actions to trigger.
 *
 * @param <T> inheritor class type
 */
public interface HasActions<T extends HasActions<T>> extends ByLocator {

    /**
     * Default open action ID.
     */
    String OPEN = "open";

    /**
     * Default lookup action ID.
     */
    String LOOKUP = "lookup";

    /**
     * Default clear action ID.
     */
    String CLEAR = "clear";

    /**
     * Triggers the action with the passed ID by clicking button.
     *
     * @param actionId action ID to trigger
     * @return {@code this} to call fluent API
     */
    default T triggerAction(String actionId) {
        $(byChained(getBy(), byUiTestId(actionId)))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        //noinspection unchecked
        return ((T) this);
    }

    /**
     * Triggers the actions with the passed ID by clicking button and returns the
     * opened wired composite (view).
     *
     * @param clazz    composite class to wire and return
     * @param actionId action ID to trigger
     * @param <V>      composite class type
     * @return wired web-element wrapper for opened composite
     */
    default <V extends Composite<V>> V triggerActionWithView(Class<V> clazz, String actionId) {
        triggerAction(actionId);
        return $j(clazz);
    }
}
