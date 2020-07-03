/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components.formatters;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;

/**
 * @deprecated Use {@link io.jmix.ui.component.formatter.ClassNameFormatter} instead
 */
@Deprecated
public class ClassNameFormatter extends io.jmix.ui.component.formatter.ClassNameFormatter {

    @Override
    public String apply(String value) {
        messages = AppBeans.get(Messages.class);
        return super.apply(value);
    }
}
