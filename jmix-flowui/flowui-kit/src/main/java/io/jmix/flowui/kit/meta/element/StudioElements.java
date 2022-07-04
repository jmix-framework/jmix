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

package io.jmix.flowui.kit.meta.element;

import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioElements {

    @StudioElement(
            name = "CollectionFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CollectionFormatter",
            xmlElement = "collection",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg"
    )
    Formatter collectionFormatter();

    @StudioElement(
            name = "CustomFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CustomFormatter",
            xmlElement = "custom",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "bean", type = StudioPropertyType.STRING, required = true)
            }
    )
    Formatter customFormatter();

    @StudioElement(
            name = "DateFormatter",
            classFqn = "io.jmix.flowui.component.formatter.DateFormatter",
            xmlElement = "date",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "format", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"DATE", "DATETIME"}),
                    @StudioProperty(xmlAttribute = "useUserTimeZone", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    Formatter dateFormatter();

    @StudioElement(
            name = "NumberFormatter",
            classFqn = "io.jmix.flowui.component.formatter.NumberFormatter",
            xmlElement = "number",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "format", type = StudioPropertyType.STRING)
            }
    )
    Formatter numberFormatter();
}
