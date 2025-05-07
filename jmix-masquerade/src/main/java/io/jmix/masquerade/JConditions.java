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

import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.DialogWindow;

import java.util.List;

import static com.codeborne.selenide.Condition.*;

/**
 * Utility class that provides the {@link WebElementCondition conditions} for checks in web-elements and their wrappers.
 */
public class JConditions {

    /**
     * Checks whether the web-element is in the {@code disabled} state.
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper.
     */
    public static final WebElementCondition DISABLED = domAttribute("disabled");

    /**
     * Checks whether the web-element is in the {@code enabled} state.
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper.
     */
    public static final WebElementCondition ENABLED = DISABLED.negate();

    /**
     * Checks whether the web-element is in the {@code visible} state.
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper.
     */
    public static final WebElementCondition VISIBLE = visible;

    /**
     * Checks whether the web-element is in the {@code readonly} state.
     * This {@link WebElementCondition condition} can be checked by web-element wrapper for components
     * that have a value.
     */
    public static final WebElementCondition READONLY = readonly;

    /**
     * Checks whether a web-element exists (element can be invisible or hidden).
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper.
     */
    public static final WebElementCondition EXIST = exist;

    /**
     * Checks whether the web-element is in the {@code required} state.
     * This {@link WebElementCondition condition} can be checked by web-elements wrapper for fields.
     */
    public static final WebElementCondition REQUIRED = domAttribute("required");

    /**
     * Checks whether a checkbox web-element is {@code checked}.
     * This {@link WebElementCondition condition} can only be checked by checkbox-like components.
     */
    public static final WebElementCondition CHECKED = domAttribute("checked");

    /**
     * Checks whether the web-element is in the {@code opened} state.
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper that supports opening
     * (e.g. {@link Details}, {@link Accordion.Panel}).
     */
    public static final WebElementCondition OPENED = domAttribute("opened");

    /**
     * Checks whether the web-element is in the {@code selected} state.
     * This {@link WebElementCondition condition} can be checked by each web-element wrapper that supports
     * selecting (e.g. {@link TabSheet.Tab}).
     */
    public static final WebElementCondition SELECTED = domAttribute("selected");

    private JConditions() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param position expected position of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks the notification position, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationPosition(Notification.Position position) {
        return new NotificationPosition(position);
    }

    /**
     * @param theme expected theme of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks notification theme, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationTheme(Notification.Theme theme) {
        return new NotificationTheme(theme);
    }

    /**
     * @param title expected title of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks the notification title, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationTitle(String title) {
        return new NotificationTitle(title);
    }

    /**
     * @param title expected contained title part of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks the contained title value, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationTitleContains(String title) {
        return new NotificationTitleContains(title);
    }

    /**
     * @param message expected message of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks the message, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationMessage(String message) {
        return new NotificationMessage(message);
    }

    /**
     * @param message expected contained message part of the {@link Notification}
     * @return {@link WebElementCondition condition} that checks the contained message value, which can be checked
     * by the {@link Notification} web-element wrapper
     */
    public static WebElementCondition notificationMessageContains(String message) {
        return new NotificationMessageContains(message);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the selected items of
     * multi-select components web-element wrappers
     * (e.g {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
     *
     * @param itemsValue expected selected items as a string presentation value
     * @return {@link WebElementCondition condition} that checks selected items, which can be checked
     * by the multi-select component
     * @see #selectedItems(List)
     */
    public static WebElementCondition selectedItems(String... itemsValue) {
        return new SelectedItems(itemsValue);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the selected items of
     * multi-select components web-element wrappers
     * (e.g {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
     *
     * @param itemsValue list of expected selected items as a string presentation value
     * @return {@link WebElementCondition condition} that checks selected items, which can be checked
     * by the multi-select component
     * @see #selectedItems(String...)
     */
    public static WebElementCondition selectedItems(List<String> itemsValue) {
        return new SelectedItems(itemsValue);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the contained selected items of
     * multi-select components web-element wrappers
     * (e.g {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
     *
     * @param itemsValue list of expected contained selected items as a string presentation value
     * @return {@link WebElementCondition condition} that checks contained selected items, which can be checked
     * by the multi-select component
     * @see #selectedItemsContains(List)
     */
    public static WebElementCondition selectedItemsContains(String... itemsValue) {
        return new SelectedItemsContains(itemsValue);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the contained value of
     * multi-select components web-element wrappers
     * (e.g {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
     *
     * @param itemsValue list of expected contained items value as a string presentation value
     * @return {@link WebElementCondition condition} that checks contained items value, which can be checked
     * by the multi-select component
     * @see #selectedItemsContains(String...)
     */
    public static WebElementCondition selectedItemsContains(List<String> itemsValue) {
        return new SelectedItemsContains(itemsValue);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the count of selected items of
     * multi-select components web-element wrappers
     * (e.g {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
     *
     * @param count expected selected items count
     * @return {@link WebElementCondition condition} that checks the selected items count, which can be checked
     * by the multi-select component
     */
    public static WebElementCondition selectedItemsCount(int count) {
        return new SelectedItemsCount(count);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the checked items of
     * {@link CheckboxGroup} web-element wrapper.
     *
     * @param checkedItems expected checked items as a string presentation value
     * @return {@link WebElementCondition condition} that checks the checked items, which can be checked by the
     * {@link CheckboxGroup} web-element wrapper
     * @see #selectedItems(List)
     */
    public static WebElementCondition checkedItems(String... checkedItems) {
        return new CheckedItems(checkedItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the checked items of
     * {@link CheckboxGroup} web-element wrapper.
     *
     * @param checkedItems list of expected checked items as a string presentation value
     * @return {@link WebElementCondition condition} that checks the checked items, which can be checked by the
     * {@link CheckboxGroup} web-element wrapper
     * @see #selectedItems(String...)
     */
    public static WebElementCondition checkedItems(List<String> checkedItems) {
        return new CheckedItems(checkedItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the contained checked items of
     * {@link CheckboxGroup} web-element wrapper.
     *
     * @param checkedItems expected contained checked items as a string presentation value
     * @return {@link WebElementCondition condition} that checks the contained checked items, which can be checked
     * by the {@link CheckboxGroup} web-element wrapper
     * @see #selectedItemsContains(List)
     */
    public static WebElementCondition checkedItemsContains(String... checkedItems) {
        return new CheckedItemsContains(checkedItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the contained checked items of
     * {@link CheckboxGroup} web-element wrapper.
     *
     * @param checkedItems list of expected contained checked items as a string presentation value
     * @return {@link WebElementCondition condition} that checks the contained checked items, which can be checked
     * by the {@link CheckboxGroup} web-element wrapper
     * @see #selectedItemsContains(List)
     */
    public static WebElementCondition checkedItemsContains(List<String> checkedItems) {
        return new CheckedItemsContains(checkedItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks items count of {@link CheckboxGroup}
     * web-element wrapper.
     *
     * @param count expected items count
     * @return {@link WebElementCondition condition} that checks items count, which can be checked by the
     * {@link CheckboxGroup} web-element wrapper
     */
    public static WebElementCondition checkedItemsCount(int count) {
        return new CheckedItemsCount(count);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the value of web-element wrappers that
     * can have a value.
     *
     * @param value expected value as a string presentation value
     * @return {@link WebElementCondition condition} that checks value, which can be checked by web-element wrapper
     * for components that have a value
     */
    public static WebElementCondition value(String value) {
        return new Value(value);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the contained value of web-element
     * wrappers that can have a value.
     *
     * @param value expected contained value as a string presentation value
     * @return {@link WebElementCondition condition} that checks contained value, which can be checked by
     * web-element wrapper for components that have a value
     */
    public static WebElementCondition valueContains(String value) {
        return new ValueContains(value);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks the label value of web-element wrappers
     * for field components.
     *
     * @param label expected label value as a string presentation value
     * @return {@link WebElementCondition condition} that checks label, which can be checked by web-element wrapper
     * for field components
     */
    public static WebElementCondition label(String label) {
        return new Label(label);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks visible items for overlay web-element
     * wrappers (inheritors of {@link AbstractOverlay}).
     *
     * @param visibleItems expected visible items as a string presentation value
     * @return {@link WebElementCondition condition} that checks visible items, which can be checked by overlay
     * web-element wrapper
     * @see #visibleItems(List)
     */
    public static WebElementCondition visibleItems(String... visibleItems) {
        return new VisibleItems(visibleItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks visible items for overlay web-element
     * wrappers (inheritors of {@link AbstractOverlay}).
     *
     * @param visibleItems list of expected visible items as a string presentation value
     * @return {@link WebElementCondition condition} that checks visible items, which can be checked by overlay
     * web-element wrapper
     * @see #visibleItems(String...)
     */
    public static WebElementCondition visibleItems(List<String> visibleItems) {
        return new VisibleItems(visibleItems);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks visible contained items for overlay
     * web-element wrappers (inheritors of {@link AbstractOverlay}).
     *
     * @param visibleItemsContains expected visible contained items as a string presentation value
     * @return {@link WebElementCondition condition} that checks visible contained items, which can be checked by
     * overlay web-element wrapper
     * @see #visibleItemsContains(List)
     */
    public static WebElementCondition visibleItemsContains(String... visibleItemsContains) {
        return new VisibleItemsContains(visibleItemsContains);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks visible contained items for overlay
     * web-element wrappers (inheritors of {@link AbstractOverlay}).
     *
     * @param visibleItemsContains list of expected visible contained items as a string presentation value
     * @return {@link WebElementCondition condition} that checks visible contained items, which can be checked by
     * overlay web-element wrapper
     * @see #visibleItemsContains(String...)
     */
    public static WebElementCondition visibleItemsContains(List<String> visibleItemsContains) {
        return new VisibleItemsContains(visibleItemsContains);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks visible items count for overlay
     * web-element wrappers (inheritors of {@link AbstractOverlay}).
     *
     * @param count expected items count
     * @return {@link WebElementCondition condition} that checks items count, which can be checked by overlay
     * web-element wrappers (inheritors of {@link AbstractOverlay})
     */
    public static WebElementCondition visibleItemsCount(int count) {
        return new VisibleItemsCount(count);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks date value for web-element wrappers
     * for components that supports date (e.g. {@link DatePicker}, {@link DateTimePicker}).
     *
     * @param value expected date value as a string presentation value
     * @return {@link WebElementCondition condition} that checks date value, which can be checked by
     * web-element wrappers for components that supports date
     */
    public static WebElementCondition dateValue(String value) {
        return new DateValue(value);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks time value for web-element wrappers
     * for components that supports time (e.g. {@link TimePicker}, {@link DateTimePicker}).
     *
     * @param value expected time value as a string presentation value
     * @return {@link WebElementCondition condition} that checks time value, which can be checked by
     * web-element wrappers for components that supports time
     */
    public static WebElementCondition timeValue(String value) {
        return new TimeValue(value);
    }

    /**
     * Creates and returns a {@link WebElementCondition condition} that checks header value for {@link DialogWindow}
     * web-elements wrappers.
     *
     * @param value expected header value as a string presentation value
     * @return {@link WebElementCondition condition} that checks header value, which can be checked by
     * {@link DialogWindow} web-element wrapper
     */
    public static WebElementCondition dialogHeader(String value) {
        return new DialogHeader(value);
    }
}
