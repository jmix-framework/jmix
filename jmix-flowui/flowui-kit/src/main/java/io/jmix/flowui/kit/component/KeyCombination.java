/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class KeyCombination {

    protected static final String KEY_FIELD_PREFIX = "KEY";

    protected final Key key;
    protected final KeyModifier[] keyModifiers;
    protected Component[] listenOnComponents;
    protected boolean resetFocusOnActiveElement;

    protected KeyCombination(Key key, KeyModifier... keyModifiers) {
        this.key = key;
        this.keyModifiers = keyModifiers;
    }

    public Key getKey() {
        return key;
    }

    @Nullable
    public KeyModifier[] getKeyModifiers() {
        return keyModifiers;
    }

    /**
     * @return components onto which the shortcut listeners are bound
     */
    @Nullable
    public Component[] getListenOnComponents() {
        return listenOnComponents;
    }

    /**
     * @return {@code true} if the focus should be reset for the active focused element, {@code false} otherwise
     */
    public boolean isResetFocusOnActiveElement() {
        return resetFocusOnActiveElement;
    }

    /**
     * Reset the focus for active focused element. Lose focus (i.e., it’s blurred) and
     * receive focus again before a shortcut is triggered for this key combination. This ensures any
     * pending input value change events for that focused element are submitted before a shortcut is activated.<br/>
     * The resetFocusOnActiveElement is {@code false} by default.
     * @param resetFocusOnActiveElement whether to reset focus
     */
    public void setResetFocusOnActiveElement(boolean resetFocusOnActiveElement) {
        this.resetFocusOnActiveElement = resetFocusOnActiveElement;
    }

    public static KeyCombination create(Key key, KeyModifier... keyModifiers) {
        return new KeyCombination(key, keyModifiers);
    }

    /**
     * Creates a new <code>KeyCombination</code> instance from a string representation.
     *
     * @param keyString string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     * @return new instance
     */
    @Nullable
    public static KeyCombination create(@Nullable String keyString) {
        if (Strings.isNullOrEmpty(keyString)) {
            return null;
        }

        keyString = keyString.toUpperCase();

        Key key;
        KeyModifier[] modifiers = null;

        if (keyString.contains("-")) {
            String[] keys = keyString.split("-", -1);

            int modifiersCount = keys.length;

            key = valueOf(keys[modifiersCount - 1]);
            --modifiersCount;
            modifiers = new KeyModifier[modifiersCount];
            for (int i = 0; i < modifiersCount; i++) {
                modifiers[i] = KeyModifier.valueOf(keys[i]);
            }
        } else {
            key = valueOf(keyString);
        }

        if (modifiers != null) {
            return new KeyCombination(key, modifiers);
        } else {
            return new KeyCombination(key);
        }
    }

    /**
     * Creates a new <code>KeyCombination</code> instance from a string representation.
     *
     * @param keyString          string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     * @param listenOnComponents {@code Component}s onto which the shortcut listeners are
     *                           bound. Must not be null. Must not contain null. Must not have
     *                           duplicate components.
     * @return new instance
     */
    @Nullable
    public static KeyCombination create(@Nullable String keyString, Component... listenOnComponents) {
        KeyCombination keyCombination = create(keyString);
        if (keyCombination != null) {
            keyCombination.listenOnComponents = listenOnComponents;
        }

        return keyCombination;
    }

    public static Key valueOf(String keyString) {
        Field keyField = Arrays.stream(Key.class.getDeclaredFields())
                .filter(field ->
                        StringUtils.equalsIgnoreCase(field.getName(), keyString)
                                || StringUtils.equalsIgnoreCase(field.getName(), KEY_FIELD_PREFIX + "_" + keyString)
                )
                .findFirst()
                .orElse(null);

        if (keyField != null) {
            try {
                return (Key) keyField.get(null);
            } catch (IllegalAccessException ignored) {
            }
        }

        throw new IllegalArgumentException("No keyboard key " + keyString);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        if (ArrayUtils.isNotEmpty(keyModifiers)) {
            for (KeyModifier modifier : keyModifiers) {
                if (!sb.isEmpty()) {
                    sb.append("+");
                }
                sb.append(modifier.getKeys().get(0));
            }
        }
        if (!sb.isEmpty()) {
            sb.append("+");
        }

        String keyName = key.getKeys().get(0);
        String keyPrefix = StringUtils.capitalize(KEY_FIELD_PREFIX.toLowerCase());
        if (keyName.startsWith(keyPrefix)) {
            keyName = keyName.substring(keyPrefix.length());
        }
        sb.append(keyName);

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyCombination that = (KeyCombination) o;
        return key.equals(that.key) && Arrays.equals(keyModifiers, that.keyModifiers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key);
        result = 31 * result + Arrays.hashCode(keyModifiers);
        return result;
    }
}
