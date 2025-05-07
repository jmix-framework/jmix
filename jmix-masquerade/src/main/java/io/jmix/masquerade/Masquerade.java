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

package io.jmix.masquerade;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.sys.Composite;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JComponents.wireClassBy;
import static io.jmix.masquerade.JComponents.wireComposite;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.JSelectors.byUiTestId;

/**
 * The main starting point of Masquerade.
 * <p>
 * You start with methods {@link Selenide#open(String)} for opening the tested application page
 * and {@link #$j(String)} for searching web-elements.
 */
public class Masquerade {

    /**
     * A special static attribute for UI components in the framework that is generated for component.
     * To enable generation it is required to set the {@code jmix.ui.test-mode} application property to {@code true}.
     */
    public static final String UI_TEST_ID = "j-test-id";

    /**
     * Finds and returns the {@link SelenideElement} wrapper for the UI element with the passed {@link #UI_TEST_ID}.
     *
     * @param uiTestId {@link #UI_TEST_ID} element attribute
     * @return {@link SelenideElement} wrapper of the found web-element
     */
    public static SelenideElement $j(String uiTestId) {
        return $(byUiTestId(uiTestId));
    }

    /**
     * Finds and returns the {@link SelenideElement} wrapper for the UI element by the path of {@link #UI_TEST_ID}.
     *
     * @param uiTestIdPath path of {@link #UI_TEST_ID} attributes to the element
     * @return {@link SelenideElement} wrapper of the found web-element
     */
    public static SelenideElement $j(String... uiTestIdPath) {
        return $(byPath(uiTestIdPath));
    }

    /**
     * Finds and returns the element wrapper with the passed class type by the path of {@link #UI_TEST_ID}.
     *
     * @param clazz        wrapper class of the required UI element
     * @param uiTestIdPath path of {@link #UI_TEST_ID} attributes to the element
     * @param <T>          type of the UI element wrapper
     * @return UI element wrapper with the {@code T} type
     */
    public static <T> T $j(Class<T> clazz, String... uiTestIdPath) {
        return wireClassBy(clazz, byPath(uiTestIdPath));
    }

    /**
     * Finds and returns the element wrapper with the passed class type by the passed {@link By}.
     *
     * @param clazz wrapper class of the required UI element
     * @param by    {@link By} to select UI element
     * @param <T>   type of the UI element wrapper
     * @return UI element wrapper with the {@code T} type
     */
    public static <T> T $j(Class<T> clazz, By by) {
        return wireClassBy(clazz, by);
    }

    /**
     * Finds and returns the {@link SelenideElement} wrapper for the {@link Composite} UI element
     * of the passed class type.
     *
     * @param clazz class of the {@link Composite} wrapper
     * @param <T>   type of the {@link Composite} wrapper
     * @return {@link  Composite} UI element
     * @see #$j(Class, String...)
     */
    public static <T extends Composite<?>> T $j(Class<T> clazz) {
        return wireComposite(clazz);
    }
}
