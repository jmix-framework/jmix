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

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioFormatterElements {

    @StudioElement(
            name = "CollectionFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CollectionFormatter",
            xmlElement = StudioXmlElements.COLLECTION,
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html#collection-formatter"
    )
    void collectionFormatter();

    @StudioElement(
            name = "CustomFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CustomFormatter",
            xmlElement = StudioXmlElements.CUSTOM,
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html#custom-formatter",
            propertyGroups = {
                    StudioPropertyGroups.Bean.class
            })
    void customFormatter();

    @StudioElement(
            name = "DateFormatter",
            classFqn = "io.jmix.flowui.component.formatter.DateFormatter",
            xmlElement = StudioXmlElements.DATE,
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html#date-time-formatter",
            propertyGroups = {
                    StudioPropertyGroups.StringFormat.class,
                    StudioPropertyGroups.DateFormatterType.class,
                    StudioPropertyGroups.UseUserTimezone.class
            })
    void dateFormatter();

    @StudioElement(
            name = "NumberFormatter",
            classFqn = "io.jmix.flowui.component.formatter.NumberFormatter",
            xmlElement = StudioXmlElements.NUMBER,
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html#number-formatter",
            propertyGroups = {
                    StudioPropertyGroups.StringFormat.class
            })
    void numberFormatter();
}
