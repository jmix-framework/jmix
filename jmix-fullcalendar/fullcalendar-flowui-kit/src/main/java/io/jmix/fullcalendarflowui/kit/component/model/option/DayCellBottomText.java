/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils.DAY_CELL_BOTTOM_TEXT;

/**
 * INTERNAL.
 */
public class DayCellBottomText extends CalendarOption {

    protected boolean textGeneratorEnabled = false;
    protected boolean classNamesGeneratorEnabled = false;

    public DayCellBottomText() {
        super(DAY_CELL_BOTTOM_TEXT);
    }

    public boolean isTextGeneratorEnabled() {
        return textGeneratorEnabled;
    }

    public void setTextGeneratorEnabled(boolean textGeneratorEnabled) {
        this.textGeneratorEnabled = textGeneratorEnabled;

        markAsDirty();
    }

    public boolean isClassNamesGeneratorEnabled() {
        return classNamesGeneratorEnabled;
    }

    public void setClassNamesGeneratorEnabled(boolean classNamesGeneratorEnabled) {
        this.classNamesGeneratorEnabled = classNamesGeneratorEnabled;

        markAsDirty();
    }
}
