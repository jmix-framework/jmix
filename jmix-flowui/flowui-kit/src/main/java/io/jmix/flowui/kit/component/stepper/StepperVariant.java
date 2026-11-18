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

package io.jmix.flowui.kit.component.stepper;

import com.vaadin.flow.component.shared.ThemeVariant;
import org.jspecify.annotations.NullMarked;

/**
 * Theme variants of {@link JmixStepper}.
 */
@NullMarked
public enum StepperVariant implements ThemeVariant {

    /**
     * Shows the indicators of completed steps and the connectors after them in the success color.
     */
    COMPLETED_SUCCESS("completed-success"),

    /**
     * Uses a smaller indicator and header font.
     */
    SMALL("small"),

    /**
     * Removes the padding of the content area.
     */
    NO_PADDING("no-padding");

    private final String variant;

    StepperVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
