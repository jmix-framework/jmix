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

package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionBinderUtils {

    public static void refreshShortcutCombinationProperty(ClickNotifier component,
                                                          @Nullable KeyCombination newShortcutCombination,
                                                          @Nullable KeyCombination oldShortcutCombination,
                                                          List<Registration> registrations) {
        List<ShortcutRegistration> shortcutRegistrations = registrations.stream()
                .filter(registration -> registration instanceof ShortcutRegistration)
                .map(registration -> (ShortcutRegistration) registration)
                .collect(Collectors.toList());

        if (!shortcutRegistrations.isEmpty() && oldShortcutCombination != null) {
            List<ShortcutRegistration> oldRegistrations = shortcutRegistrations.stream()
                    .filter(shortcutRegistration -> {
                        if (CollectionUtils.isEqualCollection(
                                shortcutRegistration.getKey().getKeys(),
                                oldShortcutCombination.getKey().getKeys())) {
                            List<String> modifiers = shortcutRegistration.getModifiers().stream()
                                    .flatMap(key -> key.getKeys().stream())
                                    .collect(Collectors.toList());
                            if (modifiers.isEmpty() && oldShortcutCombination.getKeyModifiers() == null) {
                                return true;
                            }

                            if (oldShortcutCombination.getKeyModifiers() != null) {
                                List<String> oldModifiers = Arrays.stream(oldShortcutCombination.getKeyModifiers())
                                        .flatMap(keyModifier -> keyModifier.getKeys().stream())
                                        .collect(Collectors.toList());
                                return CollectionUtils.isEqualCollection(modifiers, oldModifiers);
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            oldRegistrations.forEach(ShortcutRegistration::remove);
            registrations.removeAll(oldRegistrations);
        }

        if (newShortcutCombination != null) {
            registrations.add(
                    component.addClickShortcut(newShortcutCombination.getKey(), newShortcutCombination.getKeyModifiers()));
        }
    }
}
