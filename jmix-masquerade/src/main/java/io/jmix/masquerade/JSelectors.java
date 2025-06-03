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
     * Creates and returns a {@link By} selector that finds the web-element with
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
     * Creates and returns a {@link By} selector that finds the web-element that has display text that
     * contains the passed value at the last position in the DOM tree.
     *
     * @param displayedText text that is contained in the displayed text
     * @return {@link By} selector
     */
    public static By byDisplayedText(String displayedText) {
        Preconditions.checkNotNull(displayedText);

        return new ByDisplayedText(displayedText);
    }

    /**
     * Creates and returns a {@link By} selector that finds the web-element that has display text that
     * contains the passed value with the passed element tag name at the last position in the DOM tree.
     *
     * @param displayedText text that is contained in the displayed text
     * @param tagName       target element tag name
     * @return {@link By} selector
     */
    public static By byDisplayedText(String displayedText, String tagName) {
        Preconditions.checkNotNull(displayedText);
        Preconditions.checkNotNull(tagName);

        return new ByDisplayedText(displayedText, tagName);
    }

    /**
     * Creates and returns a {@link By} selector that finds the web-element that has display text that
     * contains the passed value at the passed element position
     * in the DOM tree (can be useful if several elements contain the same text).
     *
     * @param displayedText   text that is contained in the displayed text
     * @param elementPosition position of target element in DOM tree
     * @return {@link By} selector
     */
    public static By byDisplayedText(String displayedText, int elementPosition) {
        Preconditions.checkNotNull(displayedText);

        return new ByDisplayedText(displayedText, elementPosition);
    }

    /**
     * Creates and returns a {@link By} selector that finds the web-element that has display text that
     * contains the passed value with the passed element tag name at the passed element position
     * in the DOM tree (can be useful if several elements contain the same text).
     *
     * @param displayedText   text that is contained in the displayed text
     * @param tagName         target element tag name
     * @param elementPosition position of target element in DOM tree
     * @return {@link By} selector
     */
    public static By byDisplayedText(String displayedText, String tagName, int elementPosition) {
        Preconditions.checkNotNull(displayedText);
        Preconditions.checkNotNull(tagName);

        return new ByDisplayedText(displayedText, tagName, elementPosition);
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

    /**
     * The {@link By} selector, which find a web-element by its displayed text.
     */
    public static class ByDisplayedText extends By.ByXPath {

        protected String displayedText;
        protected String tagName;
        protected String elementPosition;

        protected ByDisplayedText(String displayedText, String tagName, String elementPosition) {
            super("(.//*[contains(text(),'%s')]/ancestor-or-self::%s[@%s])[%s]"
                    .formatted(displayedText, tagName, UI_TEST_ID, elementPosition));

            this.displayedText = displayedText;
            this.tagName = tagName;
            this.elementPosition = elementPosition;
        }

        public ByDisplayedText(String displayedText, String tagName, int elementPosition) {
            this(displayedText, tagName, String.valueOf(elementPosition));
        }

        public ByDisplayedText(String displayedText, String tagName) {
            this(displayedText, tagName, "last()");
        }

        public ByDisplayedText(String displayedText, int elementPosition) {
            this(displayedText, "*", elementPosition);
        }

        public ByDisplayedText(String displayedText) {
            this(displayedText, "*");
        }

        @Override
        public String toString() {
            return "By.ByDisplayedText: { displayedText: %s, tagName: %s, elementPosition: %s }"
                    .formatted(displayedText, tagName, elementPosition);
        }
    }
}
