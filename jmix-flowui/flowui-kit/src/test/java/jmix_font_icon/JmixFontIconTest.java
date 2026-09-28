/*
 * Copyright 2026 Haulmont.
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

package jmix_font_icon;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JmixFontIconTest {

    @Test
    void values_startWithVaadinIconsInTheSameOrder() {
        List<String> vaadinIcons = Arrays.stream(VaadinIcon.values())
                .map(Enum::name)
                .toList();
        List<String> leadingJmixIcons = Arrays.stream(JmixFontIcon.values())
                .limit(vaadinIcons.size())
                .map(Enum::name)
                .toList();

        assertEquals(vaadinIcons, leadingJmixIcons);
    }

    @Test
    void deprecation_matchesVaadinIcon() {
        List<String> mismatches = new ArrayList<>();
        for (VaadinIcon vaadinIcon : VaadinIcon.values()) {
            String name = vaadinIcon.name();
            String expected = describeDeprecation(VaadinIcon.class, name);
            String actual = describeDeprecation(JmixFontIcon.class, name);
            if (!expected.equals(actual)) {
                mismatches.add(name + ": expected " + expected + ", actual " + actual);
            }
        }

        assertTrue(mismatches.isEmpty(), mismatches.size() + " mismatches: " + mismatches);
    }

    String describeDeprecation(Class<?> enumClass, String constantName) {
        try {
            Deprecated deprecated = enumClass.getField(constantName).getAnnotation(Deprecated.class);
            if (deprecated == null) {
                return "not deprecated";
            }
            return deprecated.forRemoval() ? "deprecated for removal" : "deprecated";
        } catch (NoSuchFieldException e) {
            return "missing";
        }
    }
}
