/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.meta;

/**
 * Behaviour of UI component on Studio Screen Designer canvas.
 *
 * @see StudioComponent
 */
public enum CanvasBehaviour {
    /**
     * Component is shown on canvas as a simple box with icon.
     */
    BOX,

    /**
     * Component is shown on canvas as a button.
     */
    BUTTON,

    /**
     * Component is shown on canvas as a calendar.
     */
    CALENDAR,

    /**
     * Component is shown on canvas as a checkBox.
     */
    CHECK_BOX,

    /**
     * Component is shown on canvas as a colorPicker.
     */
    COLOR_PICKER,

    /**
     * Component is shown on canvas as a comboBox.
     */
    COMBO_BOX,

    /**
     * Component is shown on canvas as a component container.
     */
    CONTAINER,

    /**
     * Component is shown on canvas as a dateField.
     */
    DATE_FIELD,

    /**
     * Component is shown on canvas as a datePicker.
     */
    DATE_PICKER,

    /**
     * Component is shown on canvas as a filter.
     */
    FILTER,

    /**
     * Component is shown on canvas as an input field.
     */
    INPUT_FIELD,

    /**
     * Component is shown on canvas as a label.
     */
    LABEL,

    /**
     * Component is shown on canvas as a link.
     */
    LINK,

    /**
     * Component is shown on canvas as a group of options.
     */
    OPTIONS_GROUP,

    /**
     * Component is shown on canvas as a popupButton.
     */
    POPUP_BUTTON,

    /**
     * Component is shown on canvas as a propertyFilter.
     */
    PROPERTY_FILTER,

    /**
     * Component is shown on canvas as a richTextArea.
     */
    RICH_TEXT_AREA,

    /**
     * Component is shown on canvas as a searchField.
     */
    SEARCH_FIELD,

    /**
     * Component is shown on canvas as a list component.
     */
    SELECT_LIST,

    /**
     * Component is shown on canvas as a slider.
     */
    SLIDER,

    /**
     * Component is shown on canvas as a sourceCodeEditor.
     */
    SOURCE_CODE_EDITOR,

    /**
     * Component is shown on canvas as a table.
     */
    TABLE,

    /**
     * Component is shown on canvas as a textArea.
     */
    TEXT_AREA,

    /**
     * Component is shown on canvas as a timeField.
     */
    TIME_FIELD,

    /**
     * Component is shown on canvas as a tree.
     */
    TREE,

    /**
     * Component is shown on canvas as a twinColumn.
     */
    TWIN_COLUMN,

    /**
     * Component is shown on canvas as a valuePicker.
     */
    VALUE_PICKER
}