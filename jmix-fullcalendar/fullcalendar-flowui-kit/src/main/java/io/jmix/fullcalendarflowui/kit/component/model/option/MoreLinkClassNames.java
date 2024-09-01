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

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * INTERNAL.
 */
public class MoreLinkClassNames extends CalendarOption {
    public static final String NAME = "moreLinkClassNames";

    protected List<String> classNames = new ArrayList<>();

    protected boolean functionEnabled = false;

    public MoreLinkClassNames() {
        super(NAME);
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    public void setClassNames(@Nullable List<String> classNames) {
        this.classNames = classNames == null
                ? Collections.emptyList()
                : new ArrayList<>(classNames);

        markAsDirty();
    }

    public void addClassName(String className) {
        Objects.requireNonNull(className);

        classNames.add(className);

        markAsDirty();
    }

    public void addClassNames(String... classNames) {
        Objects.requireNonNull(classNames);

        this.classNames.addAll(List.of(classNames));

        markAsDirty();
    }

    public boolean isFunctionEnabled() {
        return functionEnabled;
    }

    public void setFunctionEnabled(boolean functionEnabled) {
        this.functionEnabled = functionEnabled;

        markAsDirty();
    }
}
