/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.component.formatter;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

@StudioElement(
        caption = "CustomFormatter",
        xmlElement = "custom",
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox", "io.jmix.ui.component.mainwindow.UserIndicator"},
        icon = "io/jmix/ui/icon/element/formatter.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "bean", type = PropertyType.BEAN_REF, required = true,
                        options = "io.jmix.ui.component.formatter.Formatter")
        }
)
@Internal
public interface CustomFormatter extends Formatter<Object> {
}
