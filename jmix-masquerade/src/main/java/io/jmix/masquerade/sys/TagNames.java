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

import org.openqa.selenium.By;

import static org.openqa.selenium.By.tagName;

/**
 * Utility class that provides the {@link By} selectors by web-element tag names.
 */
public class TagNames {

    public static final By DIV = tagName("div");
    public static final By SPAN = tagName("span");
    public static final By INPUT = tagName("input");
    public static final By LABEL = tagName("label");
    public static final By COMBO_BOX_OVERLAY = tagName("vaadin-combo-box-overlay");
    public static final By COMBO_BOX_ITEM = tagName("vaadin-combo-box-item");
    public static final By MENU_BAR_OVERLAY = tagName("vaadin-menu-bar-overlay");
    public static final By MENU_BAR_ITEM = tagName("vaadin-menu-bar-item");
    public static final By SELECT_OVERLAY = tagName("vaadin-select-overlay");
    public static final By SELECT_ITEM = tagName("vaadin-select-item");
    public static final By TEXT_AREA = tagName("textarea");
    public static final By MULTI_SELECT_COMBO_BOX_OVERLAY = tagName("vaadin-multi-select-combo-box-overlay");
    public static final By MULTI_SELECT_COMBO_BOX_ITEM = tagName("vaadin-multi-select-combo-box-item");
    public static final By ACCORDION_HEADING = tagName("vaadin-accordion-heading");
    public static final By DETAILS_SUMMARY = tagName("vaadin-details-summary");
    public static final By SLOT = tagName("slot");
    public static final By GRID_SORTER = tagName("vaadin-grid-sorter");

    private TagNames() {
        throw new UnsupportedOperationException();
    }
}
