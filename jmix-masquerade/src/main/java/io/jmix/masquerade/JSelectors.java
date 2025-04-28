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

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.selector.ByAttribute;
import com.google.common.base.Preconditions;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ByChained;

import static io.jmix.masquerade.Masquerade.UI_TEST_ID;

/**
 * Utility class that provides the selectors of the {@link SelenideElement} wrappers.
 * Used to find {@link SelenideElement} wrappers for web-components.
 */
public class JSelectors {

    private JSelectors() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param uiTestIdPath path of {@link Masquerade#UI_TEST_ID UI_TEST_ID} attributes to the element
     * @return {@link By} selector by which the web-element can be found
     */
    public static By byPath(String... uiTestIdPath) {
        Preconditions.checkNotNull(uiTestIdPath);

        if (uiTestIdPath.length == 1) {
            return byUiTestId(uiTestIdPath[0]);
        }

        By[] bys = new By[uiTestIdPath.length];
        for (int i = 0; i < uiTestIdPath.length; i++) {
            bys[i] = byUiTestId(uiTestIdPath[i]);
        }

        return byChained(bys);
    }

    /**
     * @param bys sequence of the {@link By} selectors by which the web-element can be found
     * @return chained {@link By} selectors
     * @see ByChained
     */
    public static By byChained(By... bys) {
        return new ByChained(bys);
    }

    /**
     * Creates and returns a {@link By} selector that finds the web-element with
     * the {@link Masquerade#UI_TEST_ID UI_TEST_ID} attribute
     * equals to the simple name of the web-element's wrapper class.
     *
     * @param clazz target web-element wrapper class
     * @return {@link By} selector
     */
    public static By byJavaClassName(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return new ByJavaClassName(clazz);
    }

    /**
     * Create and returns a {@link By} selector that finds the web-element with
     * the {@link Masquerade#UI_TEST_ID UI_TEST_ID} attribute
     * equals to the passed value.
     *
     * @param uiTestId {@code UI_TEST_ID} attribute value
     * @return {@link By} selector
     */
    public static By byUiTestId(String uiTestId) {
        Preconditions.checkNotNull(uiTestId);

        return new ByUiTestId(uiTestId);
    }

    /**
     * The {@link By} selector, which finds a web-element with the {@link Masquerade#UI_TEST_ID UI_TEST_ID}
     * attribute equals to the simple name of the web-element's wrapper class.
     */
    public static class ByJavaClassName extends ByUiTestId {

        protected Class<?> clazz;

        public ByJavaClassName(Class<?> clazz) {
            super(clazz.getSimpleName());

            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return "By.javaClassName: " + clazz.getSimpleName();
        }
    }

    /**
     * The {@link By} selector, which find a web-element by the value of
     * the {@link Masquerade#UI_TEST_ID UI_TEST_ID} attribute.
     */
    public static class ByUiTestId extends ByAttribute {

        protected String uiTestId;

        public ByUiTestId(String uiTestId) {
            super(UI_TEST_ID, uiTestId);

            this.uiTestId = uiTestId;
        }

        @Override
        public String toString() {
            return "By.uiTestId: " + uiTestId;
        }
    }
}
